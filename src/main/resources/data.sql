-- ======================================================================
-- Halo Platform — Minimal Reference Seed Data
-- H2 MERGE INTO (idempotent — safe on every restart)
--
-- ⚠️  USERS ARE NOT SEEDED HERE.
--     IAM Admin and Policy Admin are created by DataInitializer.java
--     using Spring's PasswordEncoder — passwords are ALWAYS correct.
--
-- This file seeds ONLY:
--   1. Roles
--   2. Platforms
--   3. Insurance Products (6 tiers)
--   4. Risk Parameters
-- ======================================================================

-- 1. ROLES
MERGE INTO roles (id, name) KEY(id) VALUES (1, 'INFLUENCER');
MERGE INTO roles (id, name) KEY(id) VALUES (2, 'IAM_ADMIN');
MERGE INTO roles (id, name) KEY(id) VALUES (3, 'POLICY_ADMIN');
MERGE INTO roles (id, name) KEY(id) VALUES (4, 'CLAIMS_OFFICER');
MERGE INTO roles (id, name) KEY(id) VALUES (5, 'UNDERWRITER');

-- 2. PLATFORMS
MERGE INTO platforms (id, name, base_risk_factor, description) KEY(id)
  VALUES (1, 'INSTAGRAM', 1.00, 'Photo and video sharing. High visual brand risk.');
MERGE INTO platforms (id, name, base_risk_factor, description) KEY(id)
  VALUES (2, 'YOUTUBE',   0.90, 'Long-form video. Lower virality, strong monetisation.');
MERGE INTO platforms (id, name, base_risk_factor, description) KEY(id)
  VALUES (3, 'TIKTOK',    1.30, 'Short-form viral video. High reach and dispute risk.');
MERGE INTO platforms (id, name, base_risk_factor, description) KEY(id)
  VALUES (4, 'X',         1.40, 'Microblogging. High defamation and screenshot risk.');
MERGE INTO platforms (id, name, base_risk_factor, description) KEY(id)
  VALUES (5, 'PODCAST',   0.80, 'Audio content. Lower virality, moderate legal risk.');
MERGE INTO platforms (id, name, base_risk_factor, description) KEY(id)
  VALUES (6, 'LINKEDIN',  1.10, 'Professional network. IP and reputation exposure.');
MERGE INTO platforms (id, name, base_risk_factor, description) KEY(id)
  VALUES (7, 'FACEBOOK',  1.20, 'Broad social. Misinformation and community risk.');
MERGE INTO platforms (id, name, base_risk_factor, description) KEY(id)
  VALUES (8, 'SNAPCHAT',  1.15, 'Ephemeral media. Moderate brand and conduct risk.');

-- 3. INSURANCE PRODUCTS (6 tiers, created by Policy Admin — id=2 set by DataInitializer)
--    created_by is a string audit column (email)
MERGE INTO products (id, name, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, maturity_term_months, late_payment_daily_interest_rate, surrender_value_multiplier, created_at, updated_at) KEY(id)
  VALUES (1,
    'Halo Micro',
    'Entry protection for nano-influencers (under 10k followers). Covers basic legal disputes and PR crisis support.',
    true, true, false,
    100000.00, 50000.00, 0.00,
    49.00, true, 12, 0.0005, 0.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, maturity_term_months, late_payment_daily_interest_rate, surrender_value_multiplier, created_at, updated_at) KEY(id)
  VALUES (2,
    'Halo Starter',
    'Ideal for micro-influencers (10k–100k followers). Legal defence, PR crisis response and managed monitoring.',
    true, true, false,
    250000.00, 100000.00, 0.00,
    99.00, true, 12, 0.0005, 0.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, maturity_term_months, late_payment_daily_interest_rate, surrender_value_multiplier, created_at, updated_at) KEY(id)
  VALUES (3,
    'Halo Growth',
    'For rising creators (100k–500k followers). Full legal, PR and 24/7 brand monitoring included.',
    true, true, true,
    500000.00, 250000.00, 75000.00,
    199.00, true, 12, 0.0005, 0.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, maturity_term_months, late_payment_daily_interest_rate, surrender_value_multiplier, created_at, updated_at) KEY(id)
  VALUES (4,
    'Halo Shield',
    'Mid-tier for established creators (500k–2M followers). Enhanced legal limits, PR and full monitoring suite.',
    true, true, true,
    750000.00, 400000.00, 150000.00,
    299.00, true, 12, 0.0005, 0.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, maturity_term_months, late_payment_daily_interest_rate, surrender_value_multiplier, created_at, updated_at) KEY(id)
  VALUES (5,
    'Halo Elite',
    'Premium all-inclusive for top-tier influencers (2M–10M followers). Maximum coverage across all risk categories.',
    true, true, true,
    1500000.00, 750000.00, 300000.00,
    599.00, true, 12, 0.0005, 0.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, maturity_term_months, late_payment_daily_interest_rate, surrender_value_multiplier, created_at, updated_at) KEY(id)
  VALUES (6,
    'Halo Prestige',
    'Celebrity-grade protection for mega-influencers (10M+ followers). Bespoke coverage, dedicated crisis team and global monitoring.',
    true, true, true,
    5000000.00, 2000000.00, 75000.00,
    1499.00, true, 12, 0.0005, 0.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, maturity_term_months, late_payment_daily_interest_rate, surrender_value_multiplier, created_at, updated_at) KEY(id)
  VALUES (7,
    'Custom Tailored Policy',
    'Specify your own coverage amounts and requirements. Our underwriters will tailor a plan specifically for your needs.',
    true, true, true,
    0.00, 0.00, 0.00,
    0.00, true, 12, 0.0005, 0.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- 4. RISK PARAMETERS
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (1,  'NICHE_POLITICS',          'Political Content',        1.80, 'POLITICS', true, '80% surcharge for political/activist content',            '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (2,  'NICHE_FINANCE',           'Finance Content',          2.50, 'FINANCE',  true, '150% surcharge for financial advice content',             '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (3,  'NICHE_CRYPTO',            'Crypto Content',           2.20, 'CRYPTO',   true, '120% surcharge for crypto-related content',               '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (4,  'NICHE_COMEDY',            'Comedy / Satire',          1.40, 'COMEDY',   true, '40% surcharge for parody and satire creators',            '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (5,  'NICHE_FASHION',           'Fashion & Lifestyle',      1.10, 'FASHION',  true, '10% surcharge for fashion and lifestyle content',         '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (6,  'NICHE_GAMING',            'Gaming Content',           1.15, 'GAMING',   true, '15% surcharge for gaming content',                        '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (7,  'PLATFORM_TIKTOK',         'TikTok Viral Risk',        1.30, null, true, '30% surcharge for TikTok accounts due to virality',          '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (8,  'PLATFORM_X',              'X (Twitter) Exposure',     1.40, null, true, '40% surcharge for X due to high public discourse risk',      '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (9,  'PLATFORM_INSTAGRAM',      'Instagram Standard',       1.00, null, true, 'Baseline risk for Instagram content',                        '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (10, 'PLATFORM_YOUTUBE',        'YouTube Long-form',        0.95, null, true, '5% discount for YouTube long-form content',                  '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (11, 'FOLLOWER_TIER_NANO',      'Nano < 10K Followers',     1.00, null, true, 'Base tier — no surcharge for nano-influencers',              '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (12, 'FOLLOWER_TIER_MICRO',     '10K–100K Followers',       1.05, null, true, '5% surcharge for micro-influencers',                         '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (13, 'FOLLOWER_TIER_MID',       '100K–500K Followers',      1.10, null, true, '10% surcharge for mid-tier influencers',                     '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (14, 'FOLLOWER_TIER_MACRO',     '500K–2M Followers',        1.20, null, true, '20% surcharge for macro-influencers',                        '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (15, 'FOLLOWER_TIER_MEGA',      '2M–10M Followers',         1.35, null, true, '35% surcharge for mega-influencers',                         '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, label, multiplier, applicable_niche, active, description, effective_from, update_note, updated_by_user_id, created_at, updated_at) KEY(id)
  VALUES (16, 'FOLLOWER_TIER_CELEBRITY', '10M+ Followers',           1.55, null, true, '55% surcharge for celebrity-level accounts',                 '2024-01-01', 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
