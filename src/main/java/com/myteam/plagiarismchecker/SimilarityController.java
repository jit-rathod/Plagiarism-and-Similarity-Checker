package com.myteam.plagiarismchecker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SimilarityController 
{
    private static final double SIMILARITY_FLAG_THRESHOLD = 0.7;
    private static final double AI_LIKELY_THRESHOLD = 0.6;

    private final SimilarityChecker similarityChecker;

    public SimilarityController(SimilarityChecker similarityChecker) 
    {
        this.similarityChecker = similarityChecker;
    }

    @GetMapping("/")
    public String index(@RequestParam(value = "mode", defaultValue = "similarity") String mode, Model model) 
    {
        model.addAttribute("mode", mode);
        return "index";
    }

    @PostMapping("/check")
    public String checkSimilarity(@RequestParam("submissionA") String submissionA, @RequestParam("submissionB") String submissionB, Model model) 
    {
        SimilarityChecker.SimilarityResult result = similarityChecker.compare(submissionA, submissionB);
        model.addAttribute("mode", "similarity");
        model.addAttribute("submissionA", submissionA);
        model.addAttribute("submissionB", submissionB);
        model.addAttribute("result", result);
        model.addAttribute("flagged", result.overallScore() >= SIMILARITY_FLAG_THRESHOLD);

        return "index";
    }

    @PostMapping("/check-ai")
    public String checkAiContent(@RequestParam("content") String content, Model model) 
    {
        SimilarityChecker.AiLikelihoodResult result = similarityChecker.analyzeAiContent(content);
        model.addAttribute("mode", "ai");
        model.addAttribute("aiContent", content);
        model.addAttribute("aiResult", result);
        model.addAttribute("aiFlagged", result.aiLikelihood() >= AI_LIKELY_THRESHOLD);

        return "index";
    }
}
