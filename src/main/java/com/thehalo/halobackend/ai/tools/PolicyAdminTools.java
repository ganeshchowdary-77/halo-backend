package com.thehalo.halobackend.ai.tools;

import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.enums.PolicyStatus;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LangChain4j tools for the POLICY_ADMIN role chatbot.
 * <p>
 * Provides executive-level, portfolio-wide analytics.
 * All tools are strictly read-only and return only aggregated data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyAdminTools {

    private final PolicyRepository  policyRepository;
    private final ProductRepository productRepository;

    @Tool("Get high-level portfolio statistics: total policies, total revenue, pending payments count")
    public String getPortfolioStats() {
        List<Policy> active = policyRepository.findByStatus(PolicyStatus.ACTIVE);
        List<Policy> pending = policyRepository.findByStatus(PolicyStatus.PENDING_PAYMENT);

        BigDecimal totalRevenue = active.stream()
                .filter(p -> p.getPremiumAmount() != null)
                .map(Policy::getPremiumAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return "Portfolio Overview:\n"
                + "  Active Policies    : " + active.size() + "\n"
                + "  Pending Payment    : " + pending.size() + "\n"
                + "  Monthly Revenue    : $" + totalRevenue + "\n"
                + "  Total Policies     : " + (active.size() + pending.size());
    }

    @Tool("Get a ranked breakdown of all insurance products by number of active policies sold")
    public String getProductPerformanceRanking() {
        List<Product> products = productRepository.findAllByActiveTrue();
        if (products.isEmpty()) return "No active products found.";

        Map<Long, Long> policyCounts = policyRepository.findAll().stream()
                .filter(p -> p.getProduct() != null)
                .collect(Collectors.groupingBy(p -> p.getProduct().getId(), Collectors.counting()));

        return "Product Performance Ranking:\n"
                + products.stream()
                        .sorted((a, b) -> Long.compare(
                                policyCounts.getOrDefault(b.getId(), 0L),
                                policyCounts.getOrDefault(a.getId(), 0L)))
                        .map(p -> "  " + p.getName()
                                + " — " + policyCounts.getOrDefault(p.getId(), 0L) + " policies"
                                + " — Base: $" + p.getBasePremium() + "/mo")
                        .collect(Collectors.joining("\n"));
    }
}
