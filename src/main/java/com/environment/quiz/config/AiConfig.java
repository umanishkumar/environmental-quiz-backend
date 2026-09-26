package com.environment.quiz.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${openai.fallback.api-key}")
    private String openAiApiKey;

    @Value("${openai.embedding.model}")
    private String embeddingModelName;

    /**
     * Chat client — Groq, via the autoconfigured OpenAiChatModel bean
     * (bound from spring.ai.openai.* properties in application.properties).
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Embedding model for RAG. Groq has no embeddings endpoint, so this uses
     * real OpenAI regardless — it's the only thing OPENAI_API_KEY is used for now.
     * Spring AI 2.x builds this straight from OpenAiEmbeddingOptions — no
     * separate OpenAiApi client object anymore (that class was removed in 2.0).
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .apiKey(openAiApiKey)
                .model(embeddingModelName)
                .build();

        return OpenAiEmbeddingModel.builder()
                .options(options)
                .build();
    }

    /**
     * In-memory vector store — no external DB/Docker needed. Rebuilt from the
     * knowledge base file on every startup by EnvironmentalKnowledgeLoader.
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}