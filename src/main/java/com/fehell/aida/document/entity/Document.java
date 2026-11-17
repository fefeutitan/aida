package com.fehell.aida.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents", indexes = @Index(name = "idx_documents_uploaded_at", columnList = "uploaded_at"))
public class Document {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "original_filename", nullable = false, length = 255)
	private String originalFilename;

	@Column(name = "storage_filename", nullable = false, length = 255)
	private String storageFilename;

	@Column(name = "content_type", nullable = false, length = 100)
	private String contentType;

	@Column(name = "file_size", nullable = false)
	private Long fileSize;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DocumentStatus status;

	@Column(name = "extracted_text", columnDefinition = "TEXT")
	private String extractedText;

	@Column(name = "uploaded_at", nullable = false)
	private LocalDateTime uploadedAt;

	protected Document() {
	}

	public Document(UUID id, String originalFilename, String storageFilename, String contentType,
			Long fileSize, DocumentStatus status, String extractedText, LocalDateTime uploadedAt) {
		this.id = id;
		this.originalFilename = originalFilename;
		this.storageFilename = storageFilename;
		this.contentType = contentType;
		this.fileSize = fileSize;
		this.status = status;
		this.extractedText = extractedText;
		this.uploadedAt = uploadedAt;
	}

	public UUID getId() {
		return id;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public String getStorageFilename() {
		return storageFilename;
	}

	public String getContentType() {
		return contentType;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public DocumentStatus getStatus() {
		return status;
	}

	public String getExtractedText() {
		return extractedText;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void markProcessed(String extractedText) {
		this.extractedText = extractedText;
		this.status = DocumentStatus.PROCESSED;
	}

	public void markFailed() {
		this.extractedText = null;
		this.status = DocumentStatus.FAILED;
	}
}
