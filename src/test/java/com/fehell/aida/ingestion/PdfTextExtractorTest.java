package com.fehell.aida.ingestion;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfTextExtractorTest {

	private final PdfTextExtractor extractor = new PdfTextExtractor();

	@Test
	void extractsTextFromAllPagesInOrder() throws Exception {
		byte[] pdf = createPdf("Page one", "Page two");

		String text = extractor.extract(pdf);

		assertTrue(text.indexOf("Page one") < text.indexOf("Page two"));
	}

	@Test
	void returnsEmptyTextForPdfWithoutText() throws Exception {
		try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			document.addPage(new PDPage());
			document.save(output);
			assertEquals("", extractor.extract(output.toByteArray()).trim());
		}
	}

	@Test
	void rejectsInvalidPdf() {
		assertThrows(RuntimeException.class, () -> extractor.extract("not a pdf".getBytes()));
	}

	private byte[] createPdf(String... pages) throws Exception {
		try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			for (String pageText : pages) {
				PDPage page = new PDPage();
				document.addPage(page);
				try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
					stream.beginText();
					stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
					stream.newLineAtOffset(50, 700);
					stream.showText(pageText);
					stream.endText();
				}
			}
			document.save(output);
			return output.toByteArray();
		}
	}
}
