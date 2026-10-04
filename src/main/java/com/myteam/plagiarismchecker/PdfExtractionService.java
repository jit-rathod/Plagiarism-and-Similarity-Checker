package com.myteam.plagiarismchecker;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Extracts plain text from an uploaded PDF, so the similarity checker and
 * AI-content checker can accept a PDF upload as an alternative to pasted text.
 *
 * Uses iText's PdfReader + PdfTextExtractor — the same library already
 * planned for PDF export, so this doesn't add a second PDF dependency
 * (e.g. PDFBox) just for reading.
 */
@Service
public class PdfExtractionService {

    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        StringBuilder text = new StringBuilder();
        PdfReader reader = new PdfReader(file.getInputStream());
        try {
            int pageCount = reader.getNumberOfPages();
            for (int page = 1; page <= pageCount; page++) {
                text.append(PdfTextExtractor.getTextFromPage(reader, page));
                text.append("\n");
            }
        } finally {
            reader.close();
        }

        return text.toString();
    }
}
