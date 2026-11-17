package com.fehell.aida.document.storage;

import com.fehell.aida.shared.exception.DocumentStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Component
public class LocalDocumentStorage {

	private final Path rootLocation;

	public LocalDocumentStorage(@Value("${aida.storage.location:./data/documents}") String location) {
		this.rootLocation = Path.of(location).toAbsolutePath().normalize();
	}

	public String store(UUID documentId, byte[] content) {
		String storageFilename = documentId + ".pdf";
		Path target = resolve(storageFilename);
		try {
			Files.createDirectories(rootLocation);
			Files.write(target, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
			return storageFilename;
		}
		catch (IOException exception) {
			throw new DocumentStorageException("Não foi possível armazenar o documento.", exception);
		}
	}

	public void delete(String storageFilename) {
		try {
			Files.deleteIfExists(resolve(storageFilename));
		}
		catch (IOException exception) {
			throw new DocumentStorageException("Não foi possível excluir o arquivo do documento.", exception);
		}
	}

	private Path resolve(String filename) {
		Path resolved = rootLocation.resolve(filename).normalize();
		if (!resolved.startsWith(rootLocation)) {
			throw new DocumentStorageException("Caminho de armazenamento inválido.");
		}
		return resolved;
	}
}
