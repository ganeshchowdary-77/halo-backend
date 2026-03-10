-- =====================================================================
-- H2 Seed Data for The Halo Platform (Dev / H2 only)
-- Loaded automatically by Spring Boot on startup
-- 
-- DEFAULT CREDENTIALS (All users have password: admin123):
-- - IAM Admin: iamadmin@thehalo.com / admin123
-- - Underwriter: underwriter@thehalo.com / admin123  
-- - Claims Officer: claims@thehalo.com / admin123
-- - Policy Admin: policy@thehalo.com / admin123
--
-- Data persists in ./data/halodb.mv.db file
-- =====================================================================

-- Clear existing data to avoid conflicts on restart
DELETE FROM risk_parameters WHERE id > 0;
DELETE FROM users WHERE id > 0;
DELETE FROM roles WHERE id > 0;
DELETE FROM products WHERE id > 0;
DELETE FROM platforms WHERE id > 0;

-- Reset auto-increment sequences
ALTER SEQUENCE roles_seq RESTART WITH 1;
ALTER SEQUENCE users_seq RESTART WITH 1;
ALTER SEQUENCE platforms_seq RESTART WITH 1;
ALTER SEQUENCE products_seq RESTART WITH 1;
ALTER SEQUENCE risk_parameters_seq RESTART WITH 1;

-- 1. ROLES
INSERT INTO roles (id, name) VALUES
  (1, 'INFLUENCER'),
  (2, 'IAM_ADMIN'),
  (3, 'POLICY_ADMIN'),
  (4, 'CLAIMS_OFFICER'),
  (5, 'UNDERWRITER')
;

-- 2. USERS
-- Default IAM Admin credentials:
--   Email: iamadmin@thehalo.com
--   Password: admin123
-- Use this account to create additional users via the IAM system
-- Influencers can self-register via /api/v1/auth/register
INSERT INTO users (id, email, full_name, first_name, last_name, password, role_id, created_at, updated_at) VALUES
  (1, 'iamadmin@thehalo.com', 'IAM Administrator', 'IAM', 'Administrator', '$2a$10$N.zmdr9k7uOCQb07YxWpNOT8LJda6.lBiZIiTrKxdIn7D6VpHyaGy', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. PLATFORMS
INSERT INTO platforms (id, name, base_risk_factor, description) VALUES
  (1, 'INSTAGRAM',  1.0, 'Photo and video sharing platform'),
  (2, 'YOUTUBE',    0.9, 'Video streaming and community platform'),
  (3, 'TIKTOK',     1.3, 'Short-form video with high virality risk'),
  (4, 'X',          1.4, 'Microblogging with high defamation exposure'),
  (5, 'PODCAST',    0.8, 'Audio content — lower virality, lower risk');

-- 4. PRODUCTS
INSERT INTO products (id, name, description, covered_legal, covered_p_r, covered_monitoring,
                      coverage_limit_legal, coverage_limit_p_r, coverage_limit_monitoring,
                      base_premium, active, created_at, updated_at) VALUES
  (1, 'Halo Starter',
     'Entry-level protection for micro-influencers (10k–500k followers).',
     true,  true,  false,
     250000.00, 100000.00, 0.00,
     99.00,  true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'Halo Shield',
     'Mid-tier coverage for growing creators (500k–2M followers).',
     true,  true,  true,
     500000.00, 300000.00, 100000.00,
     299.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'Halo Elite',
     'Premium all-inclusive plan for top-tier influencers (2M+ followers).',
     true,  true,  true,
     1000000.00, 750000.00, 250000.00,
     599.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 5. RISK PARAMETERS (IAM Admin initial config)
INSERT INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) VALUES
  (1, 'NICHE_POLITICS',       'Political Content',         1.80, 'POLITICS',       true, '80% premium surcharge for political/activist content', '2024-01-01', 'Initial setup based on industry risk analysis', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'NICHE_FINANCE',        'Finance Content',           2.50, 'FINANCE',        true, '150% surcharge for financial advice content',          '2024-01-01', 'High liability risk for financial recommendations', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'NICHE_CRYPTO',         'Cryptocurrency Content',    2.20, 'CRYPTO',         true, '120% surcharge for crypto-related content',            '2024-01-01', 'Volatile market with high regulatory risk', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 'NICHE_COMEDY',         'Comedy / Satire',           1.40, 'COMEDY',         true, '40% surcharge for parody and satire creators',         '2024-01-01', 'Moderate risk due to potential misinterpretation', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 'NICHE_FASHION',        'Fashion Content',           1.10, 'FASHION',        true, '10% surcharge for fashion and lifestyle content',      '2024-01-01', 'Low risk category with minimal liability exposure', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, 'NICHE_GAMING',         'Gaming Content',            1.15, 'GAMING',         true, '15% surcharge for gaming content',                     '2024-01-01', 'Moderate risk due to competitive gaming disputes', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (7, 'PLATFORM_TIKTOK',      'TikTok Viral Risk',         1.30, null,             true, '30% surcharge for TikTok accounts due to virality',    '2024-01-01', 'Platform-specific risk due to algorithm amplification', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (8, 'PLATFORM_X',           'X (Twitter) Exposure',      1.40, null,             true, '40% surcharge for X due to high public discourse risk', '2024-01-01', 'High engagement platform with rapid content spread', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (9, 'PLATFORM_INSTAGRAM',   'Instagram Standard',        1.00, null,             true, 'Baseline risk for Instagram content',                  '2024-01-01', 'Standard platform risk with moderate exposure', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (10, 'PLATFORM_YOUTUBE',    'YouTube Content',           0.95, null,             true, '5% discount for YouTube long-form content',            '2024-01-01', 'Lower risk due to content review processes', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (11, 'FOLLOWER_TIER_10K_100K', '10K-100K Followers',     1.05, null,             true, '5% surcharge for micro-influencers',                   '2024-01-01', 'Minimal risk increase for growing accounts', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (12, 'FOLLOWER_TIER_100K_500K', '100K-500K Followers',   1.10, null,             true, '10% surcharge for mid-tier influencers',               '2024-01-01', 'Moderate exposure risk with growing audience', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (13, 'FOLLOWER_TIER_500K_1M', '500K-1M Followers',       1.15, null,             true, '15% surcharge for large influencers',                  '2024-01-01', 'Increased exposure with substantial following', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (14, 'FOLLOWER_TIER_1M_5M',  '1M–5M Followers',          1.25, null,             true, '25% surcharge for mega-influencers',                   '2024-01-01', 'High exposure risk with large follower base', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (15, 'FOLLOWER_TIER_5M_PLUS','5M+ Followers',            1.50, null,             true, '50% surcharge for celebrity-level accounts',           '2024-01-01', 'Maximum exposure risk for celebrity-level accounts', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
