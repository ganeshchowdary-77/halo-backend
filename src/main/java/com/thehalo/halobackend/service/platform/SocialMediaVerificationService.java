package com.thehalo.halobackend.service.platform;

import com.thehalo.halobackend.enums.PlatformName;
import com.thehalo.halobackend.enums.Niche;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SocialMediaVerificationService {

    @Data
    @Builder
    public static class MockAccount {
        private PlatformName platform;
        private String handle;
        private String displayName;
        private Long followerCount;
        private BigDecimal engagementRate;
        private String profileUrl;
    }

    private final List<MockAccount> mockDatabase = new ArrayList<>();

    public SocialMediaVerificationService() {
        // Initialize mock database with realistic influencers
        
        // ═══════════════════════════════════════════════════════════
        // INSTAGRAM PROFILES
        // ═══════════════════════════════════════════════════════════
        
        // Sri Nayani - High engagement, moderate followers
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.INSTAGRAM)
                .handle("srinayani.official")
                .displayName("Sri Nayani")
                .followerCount(145_000L).engagementRate(new BigDecimal("6.8"))
                .profileUrl("https://instagram.com/srinayani.official")
                .build());
        
        // Nanda Kishor - Tech focused, good engagement
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.INSTAGRAM)
                .handle("nandakishor.tech")
                .displayName("Nanda Kishor")
                .followerCount(98_000L).engagementRate(new BigDecimal("5.4"))
                .profileUrl("https://instagram.com/nandakishor.tech")
                .build());
        
        // Spoorthi - Fashion & Beauty, high risk (lower engagement)
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.INSTAGRAM)
                .handle("spoorthi.style")
                .displayName("Spoorthi")
                .followerCount(67_000L).engagementRate(new BigDecimal("3.2"))
                .profileUrl("https://instagram.com/spoorthi.style")
                .build());
        
        // Other Instagram influencers
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.INSTAGRAM)
                .handle("priya.sharma.vlogs")
                .displayName("Priya Sharma")
                .followerCount(89_000L).engagementRate(new BigDecimal("5.1"))
                .profileUrl("https://instagram.com/priya.sharma.vlogs")
                .build());
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.INSTAGRAM)
                .handle("rahul.foodie")
                .displayName("Rahul Verma")
                .followerCount(156_000L).engagementRate(new BigDecimal("6.3"))
                .profileUrl("https://instagram.com/rahul.foodie")
                .build());
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.INSTAGRAM)
                .handle("anjali.fitness")
                .displayName("Anjali Reddy")
                .followerCount(203_000L).engagementRate(new BigDecimal("4.8"))
                .profileUrl("https://instagram.com/anjali.fitness")
                .build());

        // ═══════════════════════════════════════════════════════════
        // YOUTUBE PROFILES
        // ═══════════════════════════════════════════════════════════
        
        // Sri Nayani - Lifestyle vlogger, excellent engagement
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.YOUTUBE)
                .handle("srinayani.vlogs")
                .displayName("Sri Nayani")
                .followerCount(234_000L).engagementRate(new BigDecimal("8.2"))
                .profileUrl("https://youtube.com/@srinayani.vlogs")
                .build());
        
        // Nanda Kishor - Tech reviews, strong following
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.YOUTUBE)
                .handle("nandakishor.reviews")
                .displayName("Nanda Kishor")
                .followerCount(178_000L).engagementRate(new BigDecimal("7.1"))
                .profileUrl("https://youtube.com/@nandakishor.reviews")
                .build());
        
        // Spoorthi - Beauty tutorials, moderate risk
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.YOUTUBE)
                .handle("spoorthi.beauty")
                .displayName("Spoorthi")
                .followerCount(112_000L).engagementRate(new BigDecimal("4.5"))
                .profileUrl("https://youtube.com/@spoorthi.beauty")
                .build());
        
        // Other YouTube creators
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.YOUTUBE)
                .handle("kavya.cooking")
                .displayName("Kavya Iyer")
                .followerCount(142_000L).engagementRate(new BigDecimal("6.4"))
                .profileUrl("https://youtube.com/@kavya.cooking")
                .build());
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.YOUTUBE)
                .handle("rohan.comedy")
                .displayName("Rohan Desai")
                .followerCount(98_000L).engagementRate(new BigDecimal("8.1"))
                .profileUrl("https://youtube.com/@rohan.comedy")
                .build());

        // ═══════════════════════════════════════════════════════════
        // TIKTOK PROFILES
        // ═══════════════════════════════════════════════════════════
        
        // Sri Nayani - Dance & lifestyle, viral content
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.TIKTOK)
                .handle("srinayani.dance")
                .displayName("Sri Nayani")
                .followerCount(456_000L).engagementRate(new BigDecimal("12.5"))
                .profileUrl("https://tiktok.com/@srinayani.dance")
                .build());
        
        // Nanda Kishor - Tech tips, good reach
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.TIKTOK)
                .handle("nandakishor.tips")
                .displayName("Nanda Kishor")
                .followerCount(289_000L).engagementRate(new BigDecimal("9.8"))
                .profileUrl("https://tiktok.com/@nandakishor.tips")
                .build());
        
        // Spoorthi - Fashion trends, higher risk (inconsistent engagement)
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.TIKTOK)
                .handle("spoorthi.trends")
                .displayName("Spoorthi")
                .followerCount(198_000L).engagementRate(new BigDecimal("5.2"))
                .profileUrl("https://tiktok.com/@spoorthi.trends")
                .build());
        
        // Other TikTok creators
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.TIKTOK)
                .handle("aditya.dance")
                .displayName("Aditya Kumar")
                .followerCount(234_000L).engagementRate(new BigDecimal("9.2"))
                .profileUrl("https://tiktok.com/@aditya.dance")
                .build());

        // ═══════════════════════════════════════════════════════════
        // X (TWITTER) PROFILES
        // ═══════════════════════════════════════════════════════════
        
        // Sri Nayani - Lifestyle influencer
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.X)
                .handle("srinayani")
                .displayName("Sri Nayani")
                .followerCount(87_000L).engagementRate(new BigDecimal("4.3"))
                .profileUrl("https://x.com/srinayani")
                .build());
        
        // Nanda Kishor - Tech commentator
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.X)
                .handle("nandakishor_tech")
                .displayName("Nanda Kishor")
                .followerCount(65_000L).engagementRate(new BigDecimal("3.8"))
                .profileUrl("https://x.com/nandakishor_tech")
                .build());
        
        // Spoorthi - Fashion blogger
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.X)
                .handle("spoorthi_fashion")
                .displayName("Spoorthi")
                .followerCount(43_000L).engagementRate(new BigDecimal("2.9"))
                .profileUrl("https://x.com/spoorthi_fashion")
                .build());
        
        // Other X users
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.X)
                .handle("karthik.finance")
                .displayName("Karthik Menon")
                .followerCount(78_000L).engagementRate(new BigDecimal("3.4"))
                .profileUrl("https://x.com/karthik.finance")
                .build());

        // ═══════════════════════════════════════════════════════════
        // LINKEDIN PROFILES
        // ═══════════════════════════════════════════════════════════
        
        // Sri Nayani - Business & lifestyle
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.LINKEDIN)
                .handle("srinayani-professional")
                .displayName("Sri Nayani")
                .followerCount(34_000L).engagementRate(new BigDecimal("3.1"))
                .profileUrl("https://linkedin.com/in/srinayani-professional")
                .build());
        
        // Nanda Kishor - Tech professional
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.LINKEDIN)
                .handle("nandakishor-engineer")
                .displayName("Nanda Kishor")
                .followerCount(28_000L).engagementRate(new BigDecimal("2.7"))
                .profileUrl("https://linkedin.com/in/nandakishor-engineer")
                .build());
        
        // Spoorthi - Marketing professional
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.LINKEDIN)
                .handle("spoorthi-marketing")
                .displayName("Spoorthi")
                .followerCount(19_000L).engagementRate(new BigDecimal("2.1"))
                .profileUrl("https://linkedin.com/in/spoorthi-marketing")
                .build());
        
        // Other LinkedIn professionals
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.LINKEDIN)
                .handle("sanjay.entrepreneur")
                .displayName("Sanjay Malhotra")
                .followerCount(45_000L).engagementRate(new BigDecimal("2.8"))
                .profileUrl("https://linkedin.com/in/sanjay.entrepreneur")
                .build());

        // ═══════════════════════════════════════════════════════════
        // FACEBOOK PROFILES
        // ═══════════════════════════════════════════════════════════
        
        // Sri Nayani - Community builder
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.FACEBOOK)
                .handle("srinayani.page")
                .displayName("Sri Nayani")
                .followerCount(167_000L).engagementRate(new BigDecimal("5.6"))
                .profileUrl("https://facebook.com/srinayani.page")
                .build());
        
        // Nanda Kishor - Tech community
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.FACEBOOK)
                .handle("nandakishor.page")
                .displayName("Nanda Kishor")
                .followerCount(123_000L).engagementRate(new BigDecimal("4.9"))
                .profileUrl("https://facebook.com/nandakishor.page")
                .build());
        
        // Spoorthi - Fashion page
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.FACEBOOK)
                .handle("spoorthi.page")
                .displayName("Spoorthi")
                .followerCount(89_000L).engagementRate(new BigDecimal("3.7"))
                .profileUrl("https://facebook.com/spoorthi.page")
                .build());

        // ═══════════════════════════════════════════════════════════
        // SNAPCHAT PROFILES
        // ═══════════════════════════════════════════════════════════
        
        // Sri Nayani - Daily stories
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.SNAPCHAT)
                .handle("srinayani.snap")
                .displayName("Sri Nayani")
                .followerCount(78_000L).engagementRate(new BigDecimal("7.8"))
                .profileUrl("https://snapchat.com/add/srinayani.snap")
                .build());
        
        // Nanda Kishor - Tech snaps
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.SNAPCHAT)
                .handle("nandakishor.snap")
                .displayName("Nanda Kishor")
                .followerCount(56_000L).engagementRate(new BigDecimal("6.4"))
                .profileUrl("https://snapchat.com/add/nandakishor.snap")
                .build());
        
        // Spoorthi - Fashion snaps
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.SNAPCHAT)
                .handle("spoorthi.snap")
                .displayName("Spoorthi")
                .followerCount(41_000L).engagementRate(new BigDecimal("4.2"))
                .profileUrl("https://snapchat.com/add/spoorthi.snap")
                .build());

        // ═══════════════════════════════════════════════════════════
        // PODCAST PROFILES
        // ═══════════════════════════════════════════════════════════
        
        // Sri Nayani - Lifestyle podcast
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.PODCAST)
                .handle("srinayani.podcast")
                .displayName("Sri Nayani")
                .followerCount(23_000L).engagementRate(new BigDecimal("6.2"))
                .profileUrl("https://podcasts.apple.com/srinayani.podcast")
                .build());
        
        // Nanda Kishor - Tech talks
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.PODCAST)
                .handle("nandakishor.talks")
                .displayName("Nanda Kishor")
                .followerCount(18_000L).engagementRate(new BigDecimal("5.5"))
                .profileUrl("https://podcasts.apple.com/nandakishor.talks")
                .build());
        
        // Spoorthi - Fashion & style podcast
        mockDatabase.add(MockAccount.builder()
                .platform(PlatformName.PODCAST)
                .handle("spoorthi.podcast")
                .displayName("Spoorthi")
                .followerCount(12_000L).engagementRate(new BigDecimal("4.1"))
                .profileUrl("https://podcasts.apple.com/spoorthi.podcast")
                .build());
    }

    public List<MockAccount> searchMockAccounts(PlatformName platform, String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerQuery = query.toLowerCase();
        return mockDatabase.stream()
                .filter(account -> account.getPlatform() == platform &&
                       (account.getHandle().toLowerCase().contains(lowerQuery) || 
                        account.getDisplayName().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
    }

    public MockAccount getMockAccount(PlatformName platform, String handle) {
        return mockDatabase.stream()
                .filter(account -> account.getPlatform() == platform && 
                                   account.getHandle().equalsIgnoreCase(handle))
                .findFirst()
                .orElse(null);
    }
}
