package com.myteam.plagiarismchecker;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SimilarityChecker 
{
    private static final int SHORT_TEXT_WORD_THRESHOLD = 30;

    // ==================== TEXT / CODE SIMILARITY ====================

    public SimilarityResult compare(String textA, String textB) 
    {
        String[] wordsA = tokenize(textA);
        String[] wordsB = tokenize(textB);

        double cosine = cosineSimilarity(wordsA, wordsB);

        boolean isShort = wordsA.length < SHORT_TEXT_WORD_THRESHOLD
                || wordsB.length < SHORT_TEXT_WORD_THRESHOLD;

        Double levenshteinScore = null;
        if (isShort) 
        {
            levenshteinScore = normalizedLevenshteinSimilarity(textA, textB);
        }

        double overall = levenshteinScore != null
                ? (cosine + levenshteinScore) / 2.0
                : cosine;

        return new SimilarityResult(cosine, levenshteinScore, overall, isShort);
    }

    private double cosineSimilarity(String[] wordsA, String[] wordsB) 
    {
        Map<String, Integer> freqA = termFrequencies(wordsA);
        Map<String, Integer> freqB = termFrequencies(wordsB);

        Set<String> vocabulary = new HashSet<>();
        vocabulary.addAll(freqA.keySet());
        vocabulary.addAll(freqB.keySet());

        if (vocabulary.isEmpty()) return 0.0;

        long dotProduct = 0, magnitudeA = 0, magnitudeB = 0;
        for (String term : vocabulary) 
        {
            int a = freqA.getOrDefault(term, 0);
            int b = freqB.getOrDefault(term, 0);
            dotProduct += (long) a * b;
            magnitudeA += (long) a * a;
            magnitudeB += (long) b * b;
        }

        if (magnitudeA == 0 || magnitudeB == 0) return 0.0;
        return dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }

    private Map<String, Integer> termFrequencies(String[] words) 
    {
        Map<String, Integer> freq = new HashMap<>();
        for (String word : words) freq.merge(word, 1, Integer::sum);
        return freq;
    }

    private double normalizedLevenshteinSimilarity(String a, String b) 
    {
        int distance = levenshteinDistance(a, b);
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshteinDistance(String a, String b) 
    {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) 
        {
            for (int j = 1; j <= b.length(); j++) 
            {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    public record SimilarityResult(double cosineScore, Double levenshteinScore, double overallScore, boolean usedLevenshtein)
    {
        public double overallPercent() 
        { 
            return Math.round(overallScore * 1000) / 10.0; 
        }
        public double cosinePercent() 
        { 
            return Math.round(cosineScore * 1000) / 10.0; 
        }
        public Double levenshteinPercent() 
        {
            return levenshteinScore == null ? null : Math.round(levenshteinScore * 1000) / 10.0;
        }
    }

    // ==================== AI CONTENT DETECTION (heuristic) ====================

    public AiLikelihoodResult analyzeAiContent(String text) 
    {
        String[] sentences = splitSentences(text);
        String[] words = tokenize(text);

        double burstiness = burstiness(sentences);
        double lexicalDiversity = typeTokenRatio(words);

        double burstinessSignal = clamp(1.0 - burstiness, 0.0, 1.0);
        double diversitySignal = clamp(1.0 - lexicalDiversity, 0.0, 1.0);
        double aiLikelihood = (burstinessSignal * 0.5) + (diversitySignal * 0.5);

        return new AiLikelihoodResult(aiLikelihood, burstiness, lexicalDiversity, sentences.length, words.length);
    }

    private double burstiness(String[] sentences) 
    {
        if (sentences.length < 2) return 1.0;
        double[] lengths = Arrays.stream(sentences).mapToDouble(s -> tokenize(s).length).toArray();
        double mean = Arrays.stream(lengths).average().orElse(0);
        if (mean == 0) return 1.0;
        double variance = Arrays.stream(lengths).map(len -> Math.pow(len - mean, 2)).average().orElse(0);
        return clamp(Math.sqrt(variance) / mean, 0.0, 1.0);
    }

    private double typeTokenRatio(String[] words) 
    {
        if (words.length == 0) return 1.0;
        Set<String> unique = new HashSet<>(Arrays.asList(words));
        return (double) unique.size() / words.length;
    }

    private String[] splitSentences(String text) 
    {
        if (text == null || text.isBlank()) return new String[0];
        return Arrays.stream(text.split("[.!?]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private double clamp(double value, double min, double max) 
    {
        return Math.max(min, Math.min(max, value));
    }

    public record AiLikelihoodResult(double aiLikelihood, double burstiness, double lexicalDiversity, int sentenceCount, int wordCount) 
    {
        public double aiLikelihoodPercent() 
        { 
            return Math.round(aiLikelihood * 1000) / 10.0; 
        }
        public double burstinessPercent() 
        { 
            return Math.round(burstiness * 1000) / 10.0; 
        }
        public double lexicalDiversityPercent() 
        {
            return Math.round(lexicalDiversity * 1000) / 10.0; 
        }
    }

    // ==================== SHARED ====================

    private String[] tokenize(String text) 
    {
        if (text == null || text.isBlank()) return new String[0];
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .trim()
                .split("\\s+");
    }
}
