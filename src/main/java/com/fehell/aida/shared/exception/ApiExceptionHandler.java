package com.fehell.aida.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(DocumentNotFoundException.class)
	ProblemDetail handleNotFound(DocumentNotFoundException exception) {
		return problem(HttpStatus.NOT_FOUND, "Documento não encontrado", exception.getMessage());
	}

	@ExceptionHandler(UnsupportedDocumentException.class)
	ProblemDetail handleUnsupported(UnsupportedDocumentException exception) {
		return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato não suportado", exception.getMessage());
	}

	@ExceptionHandler(DocumentTooLargeException.class)
	ProblemDetail handleTooLarge(DocumentTooLargeException exception) {
		return problem(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo muito grande", exception.getMessage());
	}

	@ExceptionHandler({InvalidDocumentException.class, DocumentProcessingException.class,
			MethodArgumentTypeMismatchException.class, MultipartException.class})
	ProblemDetail handleBadRequest(Exception exception) {
		return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", exception.getMessage());
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	ProblemDetail handleMaxUploadSize(MaxUploadSizeExceededException exception) {
		return problem(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo muito grande",
				"O arquivo excede o limite permitido.");
	}

	@ExceptionHandler(DocumentStorageException.class)
	ProblemDetail handleStorage(DocumentStorageException exception) {
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Falha de armazenamento",
				"Não foi possível concluir a operação do documento.");
	}

	private ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		return problem;
	}
}
