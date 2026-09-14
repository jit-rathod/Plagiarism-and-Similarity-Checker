package com.myteam.plagiarismchecker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimilarityChecker 
{
    public double calculate(String textA, String textB) 
    {
        Map<String, Integer> freqA = wordFrequency(textA);
        Map<String, Integer> freqB = wordFrequency(textB);

        Set<String> allWords = new HashSet<>();
        allWords.addAll(freqA.keySet());
        allWords.addAll(freqB.keySet());

        double dotProduct = 0, magA = 0, magB = 0;

        for (String word : allWords) 
        {
            int a = freqA.getOrDefault(word, 0);
            int b = freqB.getOrDefault(word, 0);
            dotProduct += a * b;
            magA += a * a;
            magB += b * b;
        }

        if (magA == 0 || magB == 0) return 0.0;
        return dotProduct / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    private Map<String, Integer> wordFrequency(String text) 
    {
        Map<String, Integer> freq = new HashMap<>();
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");
        for (String word : words) 
        {
            if (!word.isBlank()) freq.merge(word, 1, Integer::sum);
        }
        return freq;
    }
}