# Plagiarism / Similarity Checker for Code & Text Assignments

A Java/Spring Boot web application that helps instructors detect similarity between student submissions — both plain-text assignments and programming assignments. For code, it goes beyond naive text-diffing by comparing **structural similarity using Abstract Syntax Trees (ASTs)**, catching cases where variable names were renamed or code was reordered but the underlying logic was copied.

Built as a semester project for B.Tech CSE (Object-Oriented Programming).

## Problem Statement

Manually comparing dozens of submissions for plagiarism is impractical, and simple text-diff tools miss disguised code plagiarism — renamed variables, reordered functions, reformatted code. This tool gives instructors a structure-aware, automated first pass.

## Features

- **Batch submission handling** — upload a full class's submissions (text files or source code) in one run
- **Text similarity** — TF-IDF cosine similarity and Levenshtein distance for plain-text assignments
- **Code similarity** — AST-based structural comparison (via JavaParser) that ignores variable names, comments, and formatting
- **Configurable threshold** — flag pairs above a set similarity score for review
- **Instructor dashboard** — pairwise similarity heatmap, side-by-side diff viewer for flagged pairs
- **Reporting** — export flagged results as PDF; store past runs per assignment/batch

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Backend | Spring Boot |
| Code Parsing (AST) | JavaParser (MVP scope; ANTLR multi-language support is future scope) |
| Text Similarity | Apache Commons Text / custom cosine similarity & Levenshtein |
| Database | H2 (default, in-memory for dev) → MySQL switchover-ready for production |
| Frontend | Thymeleaf (server-side templating) |
| Visualization | Chart.js / D3.js for the similarity heatmap |
| PDF Export | iText |
| Build Tool | Maven |
| Version Control | Git & GitHub |

## Core Data Model

Three shared entities underpin both the similarity engine and the dashboard:

- `Submission` — an individual student's text/code file
- `Batch` — a group of submissions for one assignment run
- `SimilarityResult` — a computed pairwise score between two submissions

## Status

🚧 Early build phase — stack and module ownership are finalized; core entity field definitions (`Submission`, `Batch`, `SimilarityResult`) are being locked before implementation begins in earnest.

## Getting Started

```bash
# Clone the repo
git clone <repo-url>
cd <repo-name>

# Build with Maven
mvn clean install

# Run the Spring Boot app
mvn spring-boot:run
```

The app will be available at `http://localhost:8080` (H2 in-memory DB by default).

## Evaluation

Validated by seeding a test batch with deliberately plagiarized submissions (renamed variables, reordered code) alongside genuinely original ones, then measuring flagging accuracy against the known planted cases.

## Future Scope

- Additional language support (C++, JavaScript) via ANTLR grammars
- Cross-semester / cross-batch plagiarism detection
- LMS integration (Moodle, Google Classroom) for direct submission ingestion
- ML-based similarity scoring trained on labeled plagiarism examples
