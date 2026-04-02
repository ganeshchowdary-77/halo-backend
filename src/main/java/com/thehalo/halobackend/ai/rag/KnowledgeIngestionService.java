package com.thehalo.halobackend.ai.rag;

import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;
import com.thehalo.halobackend.dto.product.response.ProductSummaryResponse;
import com.thehalo.halobackend.service.product.ProductService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads structured knowledge into the vector store on application startup.
 *
 * Product knowledge is fetched LIVE from the database via ProductService so the
 * RAG system always reflects the real, up-to-date product catalogue, terms, and
 * coverage limits — no stale hardcoded data.
 *
 * General insurance knowledge (claims process, risk scoring, FAQs) is also ingested
 * alongside the live product documents.
 *
 * Safe content only — no user PII or financial records are ever ingested.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeIngestionService implements CommandLineRunner {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel              embeddingModel;
    private final ProductService              productService;

    @Override
    public void run(String... args) {
        log.info("Halo RAG: Starting knowledge ingestion...");

        List<Document> documents = new ArrayList<>();

        // ── 1. Live Product Catalogue from DB ────────────────────────────────
        try {
            List<ProductSummaryResponse> summaries = productService.getActiveSummaries();
            for (ProductSummaryResponse summary : summaries) {
                try {
                    ProductDetailResponse p = productService.getDetail(summary.getId());
                    String content = buildProductDocument(p);
                    documents.add(Document.from(content));
                    log.debug("Halo RAG: Ingested product '{}'", p.getName());
                } catch (Exception e) {
                    log.warn("Halo RAG: Could not load detail for product id={}: {}", summary.getId(), e.getMessage());
                }
            }
            log.info("Halo RAG: {} live product(s) ingested from database", summaries.size());
        } catch (Exception e) {
            log.error("Halo RAG: Failed to load products from service — {}", e.getMessage());
        }

        // ── 2. Platform & Coverage General Knowledge ──────────────────────────
        documents.add(Document.from(
            "The Halo platform provides defamation, reputation, and cyber insurance exclusively for social media influencers. " +
            "Policies cover legal fees for lawsuits resulting from content posted on verified platforms including " +
            "Instagram, YouTube, TikTok, X (Twitter), LinkedIn, Facebook, Snapchat, and Podcasts. " +
            "We do NOT cover criminal acts, hate speech violations, or intentional copyright infringement."
        ));

        // ── 3. Risk Score Explanation ─────────────────────────────────────────
        documents.add(Document.from(
            "A Risk Score (0–100) is calculated for each influencer based on: platform type, follower count, " +
            "engagement rate, content niche, and security posture (2FA, password rotation, third-party management). " +
            "A score above 70 triggers mandatory underwriter review before an application is approved. " +
            "To lower your risk score: enable Two-Factor Authentication (2FA), rotate passwords regularly, " +
            "avoid third-party account management, and maintain an organic engagement rate above 1%."
        ));

        // ── 4. Premium Calculation ─────────────────────────────────────────────
        documents.add(Document.from(
            "The Halo premium is calculated using: Base Premium × Platform Multiplier × Follower Multiplier " +
            "× Engagement Multiplier × Niche Multiplier = Final Monthly Premium. " +
            "Higher-risk niches (Crypto, Finance, Politics) carry multipliers above 1.5x. " +
            "Lower-risk niches (Food, Travel, Lifestyle) carry multipliers near 0.9x. " +
            "Influencers with more than 1 million followers face a 1.3x follower multiplier."
        ));

        // ── 5. Defamation Insurance FAQ ────────────────────────────────────────
        documents.add(Document.from(
            "Defamation insurance protects influencers against legal claims that their content has damaged someone's " +
            "reputation. This includes libel (written statements) and slander (spoken statements). " +
            "Social media influencers are particularly vulnerable because content reaches large audiences and can go viral. " +
            "Defamation cases can result in legal fees exceeding $50,000 even without a final judgment."
        ));

        // ── 6. Claims Process ─────────────────────────────────────────────────
        documents.add(Document.from(
            "How the Halo claims process works: " +
            "Step 1: File a claim through the Halo Portal describing the incident and attaching any legal notices. " +
            "Step 2: Claim enters PENDING_REVIEW and is assigned to a Claims Officer within 48 business hours. " +
            "Step 3: The Claims Officer reviews the incident, verifies policy coverage, and may request documentation. " +
            "Step 4: Decision — APPROVED (payment released), DENIED (with reason), or ESCALATED for legal review. " +
            "Claims exceeding 30% of your Policy Exposure Value are automatically flagged for fraud scrutiny."
        ));

        // ── 7. Cyber Security & Account Takeover ──────────────────────────────
        documents.add(Document.from(
            "Cyber insurance for influencers covers losses from account takeovers, unauthorised content posting, " +
            "and digital identity theft. Coverage includes incident response costs, reputational damage assessment, " +
            "and legal consultation. " +
            "Key risk factors: no Two-Factor Authentication (2FA), shared credentials, third-party agency access, " +
            "and infrequent password rotation. " +
            "Best practices: Enable 2FA on all platforms, use unique passwords, limit third-party access."
        ));

        // ── 8. Application & Underwriting Process ──────────────────────────────
        documents.add(Document.from(
            "How the Halo application process works: " +
            "Step 1: Submit a policy application specifying your platform, product, and security settings. " +
            "Step 2: The system calculates a risk score and estimated premium instantly. " +
            "Step 3: Applications with risk score below 70 are approved automatically — you can accept immediately. " +
            "Step 4: Risk score above 70 triggers manual underwriter review within 5 business days. " +
            "Step 5: Upon acceptance, your policy activates after premium payment is confirmed."
        ));

        // ── Ingest all documents ──────────────────────────────────────────────
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(400, 50))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);
        log.info("Halo RAG: ingestion complete — {} total document(s) in vector store", documents.size());
    }

    /**
     * Builds a rich natural-language document from a ProductDetailResponse.
     * This is what the RAG retriever will find when visitors ask about product details.
     */
    private String buildProductDocument(ProductDetailResponse p) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSURANCE PRODUCT: ").append(p.getName()).append("\n");
        sb.append("Tagline: ").append(p.getTagline()).append("\n");

        if (p.getDescription() != null && !p.getDescription().isBlank())
            sb.append("Description: ").append(p.getDescription()).append("\n");

        sb.append("Base Monthly Premium: $").append(p.getBasePremium()).append("/month\n");

        if (p.getTotalCoverageLimit() != null)
            sb.append("Total Coverage Limit: $").append(p.getTotalCoverageLimit()).append("\n");

        if (Boolean.TRUE.equals(p.getCoverageLegal())) {
            sb.append("Legal Defence Coverage: Yes");
            if (p.getCoverageLimitLegal() != null)
                sb.append(" — up to $").append(p.getCoverageLimitLegal());
            sb.append("\n");
        }

        if (Boolean.TRUE.equals(p.getCoverageReputation())) {
            sb.append("PR & Reputation Crisis Coverage: Yes");
            if (p.getCoverageLimitReputation() != null)
                sb.append(" — up to $").append(p.getCoverageLimitReputation());
            sb.append("\n");
        }

        if (Boolean.TRUE.equals(p.getCoverageCyber())) {
            sb.append("Cyber & Account Takeover Coverage: Yes");
            if (p.getCoverageLimitCyber() != null)
                sb.append(" — up to $").append(p.getCoverageLimitCyber());
            sb.append("\n");
        }

        if (p.getKeyFeatures() != null && !p.getKeyFeatures().isEmpty()) {
            sb.append("Key Features:\n");
            p.getKeyFeatures().forEach(f -> sb.append("  - ").append(f).append("\n"));
        }

        return sb.toString().trim();
    }
}
