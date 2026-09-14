package com.myteam.plagiarismchecker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SimilarityController 
{
    private final SimilarityChecker checker = new SimilarityChecker();

    @GetMapping("/")
    public String form() 
    {
        return "index";
    }

    @PostMapping("/check")
    public String check(@RequestParam String textA, @RequestParam String textB, Model model) 
    {
        double score = checker.calculate(textA, textB) * 100;
        model.addAttribute("score", Math.round(score * 100.0) / 100.0);
        return "index";
    }
}
