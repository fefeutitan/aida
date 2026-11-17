package com.fehell.aida.document.dto;

import com.fehell.aida.document.entity.Document;
import com.fehell.aida.document.entity.DocumentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
		UUID id,
		String originalFilename,
		String contentType,
		Long fileSize,
		DocumentStatus status,
		LocalDateTime uploadedAt) {

	public static DocumentResponse from(Document document) {
		return new DocumentResponse(
				document.getId(),
				document.getOriginalFilename(),
				document.getContentType(),
				document.getFileSize(),
				document.getStatus(),
				document.getUploadedAt());
	}
}
