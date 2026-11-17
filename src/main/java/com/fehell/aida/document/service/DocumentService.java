package com.fehell.aida.document.service;

import com.fehell.aida.document.dto.DocumentResponse;
import com.fehell.aida.document.entity.Document;
import com.fehell.aida.document.entity.DocumentStatus;
import com.fehell.aida.document.repository.DocumentRepository;
import com.fehell.aida.document.storage.LocalDocumentStorage;
import com.fehell.aida.ingestion.PdfTextExtractor;
import com.fehell.aida.shared.exception.DocumentNotFoundException;
import com.fehell.aida.shared.exception.DocumentProcessingException;
import com.fehell.aida.shared.exception.DocumentTooLargeException;
import com.fehell.aida.shared.exception.InvalidDocumentException;
import com.fehell.aida.shared.exception.PdfExtractionException;
import com.fehell.aida.shared.exception.UnsupportedDocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DocumentService {

	private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII);

	private final DocumentRepository documentRepository;
	private final LocalDocumentStorage documentStorage;
	private final PdfTextExtractor pdfTextExtractor;
	private final long maxFileSize;

	public DocumentService(DocumentRepository documentRepository,
			LocalDocumentStorage documentStorage,
			PdfTextExtractor pdfTextExtractor,
			@Value("${aida.storage.max-file-size:10MB}") org.springframework.util.unit.DataSize maxFileSize) {
		this.documentRepository = documentRepository;
		this.documentStorage = documentStorage;
		this.pdfTextExtractor = pdfTextExtractor;
		this.maxFileSize = maxFileSize.toBytes();
	}

	@Transactional(noRollbackFor = DocumentProcessingException.class)
	public DocumentResponse upload(MultipartFile file) {
		byte[] content = readAndValidate(file);
		UUID id = UUID.randomUUID();
		String storageFilename = documentStorage.store(id, content);
		LocalDateTime uploadedAt = LocalDateTime.now();
		Document document = new Document(id, originalFilename(file), storageFilename,
				MediaType.APPLICATION_PDF_VALUE, (long) content.length, DocumentStatus.UPLOADED,
				null, uploadedAt);
		try {
			documentRepository.save(document);
		}
		catch (RuntimeException exception) {
			documentStorage.delete(storageFilename);
			throw exception;
		}

		String extractedText;
		try {
			extractedText = pdfTextExtractor.extract(content);
		}
		catch (PdfExtractionException exception) {
			documentStorage.delete(storageFilename);
			document.markFailed();
			documentRepository.save(document);
			throw new DocumentProcessingException("O PDF não pôde ser processado.", exception);
		}

		document.markProcessed(extractedText);
		try {
			return DocumentResponse.from(documentRepository.save(document));
		}
		catch (RuntimeException exception) {
			documentStorage.delete(storageFilename);
			throw exception;
		}
	}

	@Transactional(readOnly = true)
	public Page<DocumentResponse> findAll(Pageable pageable) {
		return documentRepository.findAll(pageable).map(DocumentResponse::from);
	}

	@Transactional(readOnly = true)
	public DocumentResponse findById(UUID id) {
		return documentRepository.findById(id)
				.map(DocumentResponse::from)
				.orElseThrow(DocumentNotFoundException::new);
	}

	@Transactional
	public void delete(UUID id) {
		Document document = documentRepository.findById(id).orElseThrow(DocumentNotFoundException::new);
		documentStorage.delete(document.getStorageFilename());
		documentRepository.delete(document);
	}

	private byte[] readAndValidate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new InvalidDocumentException("O arquivo PDF é obrigatório e não pode estar vazio.");
		}
		if (file.getSize() > maxFileSize) {
			throw new DocumentTooLargeException("O arquivo excede o limite permitido.");
		}
		String filename = originalFilename(file);
		if (!StringUtils.hasText(filename) || !filename.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
			throw new UnsupportedDocumentException("Apenas arquivos PDF são aceitos.");
		}
		try {
			byte[] content = file.getBytes();
			if (!hasPdfSignature(content)) {
				throw new InvalidDocumentException("O arquivo não possui uma assinatura PDF válida.");
			}
			return content;
		}
		catch (IOException exception) {
			throw new InvalidDocumentException("Não foi possível ler o arquivo enviado.");
		}
	}

	private String originalFilename(MultipartFile file) {
		String filename = file == null ? null : StringUtils.getFilename(file.getOriginalFilename());
		return StringUtils.hasText(filename) ? filename : "document.pdf";
	}

	private boolean hasPdfSignature(byte[] content) {
		if (content.length < PDF_SIGNATURE.length) {
			return false;
		}
		for (int index = 0; index < PDF_SIGNATURE.length; index++) {
			if (content[index] != PDF_SIGNATURE[index]) {
				return false;
			}
		}
		return true;
	}
}
