package com.environment.quiz.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentalKnowledgeRetriever {

    private static final int TOP_K = 3;
    private static final double SIMILARITY_THRESHOLD = 0.5;

    private final VectorStore vectorStore;

    /**
     * Retrieves the most relevant environmental fact chunks for the given topic,
     * to be injected into the generation prompt as grounding context. Returns an
     * empty string (never throws) if retrieval fails or finds nothing — quiz
     * generation should always be able to proceed without RAG in the worst case.
     */
    public String retrieveContext(String topic) {
        try {
            List<Document> results = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(topic)
                            .topK(TOP_K)
                            .similarityThreshold(SIMILARITY_THRESHOLD)
                            .build());

            if (results == null || results.isEmpty()) {
                log.info("No RAG context found for topic '{}' — generating without grounding.", topic);
                return "";
            }

            return results.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n"));

        } catch (Exception ex) {
            log.warn("RAG retrieval failed for topic '{}', continuing without grounding context: {}",
                    topic, ex.getMessage());
            return "";
        }
    }
}