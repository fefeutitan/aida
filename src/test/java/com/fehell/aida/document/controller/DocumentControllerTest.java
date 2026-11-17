package com.fehell.aida.document.controller;

import com.fehell.aida.document.dto.DocumentResponse;
import com.fehell.aida.document.entity.DocumentStatus;
import com.fehell.aida.document.service.DocumentService;
import com.fehell.aida.shared.exception.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

	@Mock
	private DocumentService service;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new DocumentController(service))
				.setControllerAdvice(new ApiExceptionHandler())
				.build();
	}

	@Test
	void uploadsAndReturnsCreated() throws Exception {
		UUID id = UUID.randomUUID();
		when(service.upload(any())).thenReturn(new DocumentResponse(id, "sample.pdf", "application/pdf", 10L,
				DocumentStatus.PROCESSED, LocalDateTime.now()));

		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "%PDF-valid".getBytes());
		mockMvc.perform(multipart("/api/v1/documents").file(file))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.extractedText").doesNotExist());
	}

	@Test
	void listsDocumentsWithPagination() throws Exception {
		UUID id = UUID.randomUUID();
		DocumentResponse response = new DocumentResponse(id, "sample.pdf", "application/pdf", 10L,
				DocumentStatus.PROCESSED, LocalDateTime.now());
		when(service.findAll(any())).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

		mockMvc.perform(get("/api/v1/documents").param("page", "0").param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(id.toString()))
				.andExpect(jsonPath("$.totalElements").value(1));
	}
}
