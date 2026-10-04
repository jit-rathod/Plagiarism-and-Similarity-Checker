package com.myteam.plagiarismchecker;

import com.itextpdf.text.DocumentException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class SimilarityController 
{
    private static final double SIMILARITY_FLAG_THRESHOLD = 0.7;
    private static final double AI_LIKELY_THRESHOLD = 0.6;

    private final SimilarityChecker similarityChecker;
    private final PdfExtractionService pdfExtractionService;
    private final PdfReportGenerator pdfReportGenerator;

    public SimilarityController(
            SimilarityChecker similarityChecker,
            PdfExtractionService pdfExtractionService,
            PdfReportGenerator pdfReportGenerator
    ) 
    {
        this.similarityChecker = similarityChecker;
        this.pdfExtractionService = pdfExtractionService;
        this.pdfReportGenerator = pdfReportGenerator;
    }

    @GetMapping("/")
    public String index(@RequestParam(value = "mode", defaultValue = "similarity") String mode, Model model) 
    {
        model.addAttribute("mode", mode);
        return "index";
    }

    @PostMapping("/check")
    public String checkSimilarity(
            @RequestParam(value = "submissionA", required = false) String submissionA,
            @RequestParam(value = "submissionB", required = false) String submissionB,
            @RequestParam(value = "fileA", required = false) MultipartFile fileA,
            @RequestParam(value = "fileB", required = false) MultipartFile fileB,
            Model model
    ) throws IOException
    {
        String textA = resolveContent(submissionA, fileA);
        String textB = resolveContent(submissionB, fileB);

        SimilarityChecker.SimilarityResult result = similarityChecker.compare(textA, textB);
        boolean flagged = result.overallScore() >= SIMILARITY_FLAG_THRESHOLD;

        model.addAttribute("mode", "similarity");
        model.addAttribute("submissionA", textA);
        model.addAttribute("submissionB", textB);
        model.addAttribute("result", result);
        model.addAttribute("flagged", flagged);

        return "index";
    }

    @PostMapping("/check-ai")
    public String checkAiContent(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Model model
    ) throws IOException
    {
        String text = resolveContent(content, file);

        SimilarityChecker.AiLikelihoodResult result = similarityChecker.analyzeAiContent(text);
        boolean flagged = result.aiLikelihood() >= AI_LIKELY_THRESHOLD;

        model.addAttribute("mode", "ai");
        model.addAttribute("aiContent", text);
        model.addAttribute("aiResult", result);
        model.addAttribute("aiFlagged", flagged);

        return "index";
    }

    @PostMapping("/export-pdf")
    public ResponseEntity<byte[]> exportSimilarityPdf(
            @RequestParam("submissionA") String submissionA,
            @RequestParam("submissionB") String submissionB
    ) throws DocumentException
    {
        SimilarityChecker.SimilarityResult result = similarityChecker.compare(submissionA, submissionB);
        boolean flagged = result.overallScore() >= SIMILARITY_FLAG_THRESHOLD;

        byte[] pdf = pdfReportGenerator.generateSimilarityReport(submissionA, submissionB, result, flagged);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=similarity-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/export-pdf-ai")
    public ResponseEntity<byte[]> exportAiPdf(
            @RequestParam("content") String content
    ) throws DocumentException
    {
        SimilarityChecker.AiLikelihoodResult result = similarityChecker.analyzeAiContent(content);
        boolean flagged = result.aiLikelihood() >= AI_LIKELY_THRESHOLD;

        byte[] pdf = pdfReportGenerator.generateAiReport(content, result, flagged);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ai-content-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String resolveContent(String pastedText, MultipartFile file) throws IOException
    {
        if (file != null && !file.isEmpty()) 
        {
            return pdfExtractionService.extractText(file);
        }
        return pastedText;
    }
}
