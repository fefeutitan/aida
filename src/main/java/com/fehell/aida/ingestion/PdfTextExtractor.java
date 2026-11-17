package com.fehell.aida.ingestion;

import com.fehell.aida.shared.exception.PdfExtractionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class PdfTextExtractor {

	public String extract(byte[] pdfContent) {
		try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(pdfContent))) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setStartPage(1);
			stripper.setEndPage(document.getNumberOfPages());
			return stripper.getText(document);
		}
		catch (Exception exception) {
			throw new PdfExtractionException("Não foi possível extrair o texto do PDF.", exception);
		}
	}
}
