package com.fehell.aida.shared.exception;

public class DocumentNotFoundException extends RuntimeException {

	public DocumentNotFoundException() {
		super("Documento não encontrado.");
	}
}
