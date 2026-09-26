package com.environment.quiz.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads the environmental facts knowledge base into the (in-memory) VectorStore
 * once at application startup, so QuizAIService/EnvironmentalKnowledgeRetriever
 * can perform RAG similarity search against it. The store is rebuilt fresh on
 * every restart — acceptable for an academic project; swapping in a persistent
 * VectorStore implementation later would remove that limitation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnvironmentalKnowledgeLoader implements ApplicationRunner {

    private static final String KNOWLEDGE_BASE_PATH = "rag/environmental-facts.md";

    private final VectorStore vectorStore;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Resource resource = new PathMatchingResourcePatternResolver()
                .getResource("classpath:" + KNOWLEDGE_BASE_PATH);

        if (!resource.exists()) {
            log.warn("RAG knowledge base file not found at {}, quiz generation will run without grounding context.",
                    KNOWLEDGE_BASE_PATH);
            return;
        }

        List<Document> documents = splitIntoSections(resource);

        if (documents.isEmpty()) {
            log.warn("RAG knowledge base file was empty — no documents to embed.");
            return;
        }

        try {
            vectorStore.add(documents);
            log.info("Loaded {} environmental knowledge sections into the vector store for RAG.", documents.size());
        } catch (Exception ex) {
            // Most likely cause: missing/invalid OPENAI_API_KEY for the embedding model.
            // Don't crash the whole app over this — quiz generation just runs without RAG grounding.
            log.error("Failed to embed RAG knowledge base — quiz generation will continue without grounding "
                    + "context. Check that OPENAI_API_KEY is set. Cause: {}", ex.getMessage());
        }
    }

    /**
     * Splits the markdown file into one Document per "## Heading" section,
     * tagging each with its topic name as metadata.
     */
    private List<Document> splitIntoSections(Resource resource) throws Exception {
        List<Document> documents = new ArrayList<>();

        StringBuilder currentBody = new StringBuilder();
        String currentTopic = null;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("## ")) {
                    flushSection(documents, currentTopic, currentBody);
                    currentTopic = line.substring(3).trim();
                    currentBody = new StringBuilder();
                } else {
                    currentBody.append(line).append("\n");
                }
            }
            flushSection(documents, currentTopic, currentBody);
        }

        return documents;
    }

    private void flushSection(List<Document> documents, String topic, StringBuilder body) {
        if (topic == null || body.toString().isBlank()) {
            return;
        }
        String content = topic + ": " + body.toString().trim();
        documents.add(new Document(content, Map.of("topic", topic)));
    }
}