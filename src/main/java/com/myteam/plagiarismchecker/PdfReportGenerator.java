package com.myteam.plagiarismchecker;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * Builds a one-page PDF report summarizing a flagged similarity or
 * AI-content result, for the instructor to download and keep.
 */
@Service
public class PdfReportGenerator {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font SECTION_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font BODY_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final int MAX_PREVIEW_CHARS = 2000;

    public byte[] generateSimilarityReport(
            String submissionA,
            String submissionB,
            SimilarityChecker.SimilarityResult result,
            boolean flagged
    ) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("Similarity Check Report", TITLE_FONT));
        document.add(blank());

        document.add(new Paragraph("Overall similarity: " + result.overallPercent() + "%", SECTION_FONT));
        document.add(new Paragraph("Status: " + (flagged ? "FLAGGED" : "Not flagged"), SECTION_FONT));
        document.add(new Paragraph("Cosine similarity: " + result.cosinePercent() + "%", BODY_FONT));
        if (result.usedLevenshtein()) {
            document.add(new Paragraph("Levenshtein similarity: " + result.levenshteinPercent() + "%", BODY_FONT));
        }
        document.add(blank());

        document.add(new Paragraph("Submission A", SECTION_FONT));
        document.add(new Paragraph(preview(submissionA), BODY_FONT));
        document.add(blank());

        document.add(new Paragraph("Submission B", SECTION_FONT));
        document.add(new Paragraph(preview(submissionB), BODY_FONT));

        document.close();
        return out.toByteArray();
    }

    public byte[] generateAiReport(
            String content,
            SimilarityChecker.AiLikelihoodResult result,
            boolean flagged
    ) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("AI Content Check Report", TITLE_FONT));
        document.add(blank());

        document.add(new Paragraph("AI-content likelihood: " + result.aiLikelihoodPercent() + "%", SECTION_FONT));
        document.add(new Paragraph("Status: " + (flagged ? "LIKELY AI" : "Not flagged"), SECTION_FONT));
        document.add(new Paragraph("Sentence-length burstiness: " + result.burstinessPercent() + "%", BODY_FONT));
        document.add(new Paragraph("Lexical diversity: " + result.lexicalDiversityPercent() + "%", BODY_FONT));
        document.add(blank());

        document.add(new Paragraph("Submission", SECTION_FONT));
        document.add(new Paragraph(preview(content), BODY_FONT));

        document.close();
        return out.toByteArray();
    }

    private Paragraph blank() {
        return new Paragraph(" ");
    }

    private String preview(String text) {
        if (text == null) return "";
        return text.length() > MAX_PREVIEW_CHARS
                ? text.substring(0, MAX_PREVIEW_CHARS) + "..."
                : text;
    }
}
