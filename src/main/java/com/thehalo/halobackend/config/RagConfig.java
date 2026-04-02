package com.thehalo.halobackend.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure RAG (Retrieval-Augmented Generation) components.
 * <p>
 * <strong>Design:</strong> Uses a local in-process embedding model (no external API calls)
 * and an in-memory vector store. The {@link com.thehalo.halobackend.ai.rag.KnowledgeIngestionService}
 * populates this store on startup with structured insurance knowledge documents.
 * <p>
 * <strong>What is stored:</strong> ONLY public, non-sensitive insurance knowledge —
 * policy definitions, legal FAQs, risk score explanations, claims process guides.
 * NEVER user PII, claim data, or financial records.
 */
@Configuration
@Slf4j
public class RagConfig {

    /**
     * Local in-process embedding model — 384 dimensions, no external API calls, very fast.
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("RAG: Initialising AllMiniLmL6V2 local embedding model (384-dim)");
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * In-memory vector store. Populated by {@code KnowledgeIngestionService} on startup.
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * Content retriever — fetches top-3 semantically relevant chunks
     * with a minimum similarity score of 0.6.
     */
    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .build();
    }
}
