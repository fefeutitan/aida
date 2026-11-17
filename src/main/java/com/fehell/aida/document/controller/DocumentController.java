package com.fehell.aida.document.controller;

import com.fehell.aida.document.dto.DocumentResponse;
import com.fehell.aida.document.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

	private final DocumentService documentService;

	public DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@PostMapping(consumes = "multipart/form-data")
	public ResponseEntity<DocumentResponse> upload(@RequestParam("file") MultipartFile file) {
		DocumentResponse response = documentService.upload(file);
		return ResponseEntity.created(URI.create("/api/v1/documents/" + response.id())).body(response);
	}

	@GetMapping
	public Page<DocumentResponse> findAll(@PageableDefault(sort = "uploadedAt") Pageable pageable) {
		return documentService.findAll(pageable);
	}

	@GetMapping("/{id}")
	public DocumentResponse findById(@PathVariable UUID id) {
		return documentService.findById(id);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		documentService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
