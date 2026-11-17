package com.fehell.aida.document.service;

import com.fehell.aida.document.entity.Document;
import com.fehell.aida.document.entity.DocumentStatus;
import com.fehell.aida.document.repository.DocumentRepository;
import com.fehell.aida.document.storage.LocalDocumentStorage;
import com.fehell.aida.ingestion.PdfTextExtractor;
import com.fehell.aida.shared.exception.DocumentNotFoundException;
import com.fehell.aida.shared.exception.InvalidDocumentException;
import com.fehell.aida.shared.exception.DocumentProcessingException;
import com.fehell.aida.shared.exception.PdfExtractionException;
import com.fehell.aida.shared.exception.UnsupportedDocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

	@Mock
	private DocumentRepository repository;

	@Mock
	private LocalDocumentStorage storage;

	@Mock
	private PdfTextExtractor extractor;

	private DocumentService service;

	@BeforeEach
	void setUp() {
		service = new DocumentService(repository, storage, extractor, DataSize.ofMegabytes(10));
	}

	@Test
	void uploadsValidPdfAndPersistsProcessedDocument() {
		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "%PDF-valid".getBytes());
		when(storage.store(any(), any())).thenReturn("stored.pdf");
		when(extractor.extract(any())).thenReturn("extracted text");
		when(repository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.upload(file);

		assertEquals("sample.pdf", response.originalFilename());
		assertEquals(DocumentStatus.PROCESSED, response.status());
		verify(repository, times(2)).save(any(Document.class));
	}

	@Test
	void rejectsEmptyFile() {
		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", new byte[0]);

		assertThrows(InvalidDocumentException.class, () -> service.upload(file));
	}

	@Test
	void rejectsNonPdfExtension() {
		MockMultipartFile file = new MockMultipartFile("file", "sample.txt", "text/plain", "%PDF-valid".getBytes());

		assertThrows(UnsupportedDocumentException.class, () -> service.upload(file));
	}

	@Test
	void rejectsDocumentThatDoesNotHavePdfSignature() {
		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "not pdf".getBytes());

		assertThrows(InvalidDocumentException.class, () -> service.upload(file));
	}

	@Test
	void findsMissingDocument() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThrows(DocumentNotFoundException.class, () -> service.findById(id));
	}

	@Test
	void deletesStoredFileAndDocument() {
		UUID id = UUID.randomUUID();
		Document document = new Document(id, "sample.pdf", "stored.pdf", "application/pdf", 10L,
				DocumentStatus.PROCESSED, "text", java.time.LocalDateTime.now());
		when(repository.findById(id)).thenReturn(Optional.of(document));

		service.delete(id);

		verify(storage).delete("stored.pdf");
		verify(repository).delete(document);
	}

	@Test
	void recordsFailedDocumentAndRemovesFileWhenExtractionFails() {
		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "%PDF-valid".getBytes());
		when(storage.store(any(), any())).thenReturn("stored.pdf");
		when(extractor.extract(any())).thenThrow(new PdfExtractionException("invalid", new IOException()));
		when(repository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertThrows(DocumentProcessingException.class, () -> service.upload(file));

		ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
		verify(repository, times(2)).save(captor.capture());
		assertEquals(DocumentStatus.FAILED, captor.getValue().getStatus());
		verify(storage).delete("stored.pdf");
	}
}
