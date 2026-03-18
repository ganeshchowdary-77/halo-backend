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

-- 3. INSURANCE PRODUCTS (7 tiers with taglines)
--    Comprehensive coverage options for influencers at every stage

MERGE INTO products (id, name, tagline, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, created_at, updated_at) KEY(id)
  VALUES (1,
    'Halo Starter',
    'Your first step to protection',
    'Perfect for emerging creators (under 10k followers). Essential legal defense and PR crisis support to protect your growing brand from unexpected challenges.',
    true, true, false,
    50000.00, 25000.00, 0.00,
    49.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, tagline, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, created_at, updated_at) KEY(id)
  VALUES (2,
    'Halo Growth',
    'Scale with confidence',
    'Designed for rising creators (10k–100k followers). Comprehensive legal protection, PR crisis management, and proactive brand monitoring to support your growth journey.',
    true, true, true,
    150000.00, 75000.00, 25000.00,
    99.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, tagline, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, created_at, updated_at) KEY(id)
  VALUES (3,
    'Halo Professional',
    'Elevate your influence',
    'Built for established creators (100k–500k followers). Enhanced legal coverage, dedicated PR team, and 24/7 monitoring to safeguard your professional reputation.',
    true, true, true,
    500000.00, 250000.00, 100000.00,
    199.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, tagline, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, created_at, updated_at) KEY(id)
  VALUES (4,
    'Halo Elite',
    'Premium protection for top creators',
    'Tailored for influential creators (500k–2M followers). Maximum legal defense, priority PR response, and advanced monitoring suite with real-time alerts.',
    true, true, true,
    1000000.00, 500000.00, 200000.00,
    349.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, tagline, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, created_at, updated_at) KEY(id)
  VALUES (5,
    'Halo Prestige',
    'Celebrity-grade coverage',
    'Exclusive protection for mega-influencers (2M–10M followers). Unlimited legal support, dedicated crisis team, global monitoring, and white-glove service.',
    true, true, true,
    2500000.00, 1500000.00, 500000.00,
    699.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, tagline, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, created_at, updated_at) KEY(id)
  VALUES (6,
    'Halo Icon',
    'Ultimate protection for digital legends',
    'Bespoke coverage for celebrity influencers (10M+ followers). Comprehensive legal fortress, 24/7 crisis management, global reputation monitoring, and personal account manager.',
    true, true, true,
    5000000.00, 3000000.00, 1000000.00,
    1299.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO products (id, name, tagline, description,
    covered_legal, covered_pr, covered_monitoring,
    coverage_limit_legal, coverage_limit_pr, coverage_limit_monitoring,
    base_premium, active, created_at, updated_at) KEY(id)
  VALUES (7,
    'Halo Enterprise',
    'Protect your entire talent roster',
    'Comprehensive solution for agencies and multi-creator organizations. Bulk coverage, centralized dashboard, legal team access, and enterprise-grade monitoring for all your talent.',
    true, true, true,
    10000000.00, 5000000.00, 2000000.00,
    1999.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sync sequence for IDENTITY generation
ALTER TABLE products ALTER COLUMN id RESTART WITH 8;



-- 4. RISK PARAMETERS
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (1,  'NICHE_POLITICS',          1.80, true, '80% surcharge for political/activist content',            'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (2,  'NICHE_FINANCE',           2.50, true, '150% surcharge for financial advice content',             'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (3,  'NICHE_CRYPTO',            2.20, true, '120% surcharge for crypto-related content',               'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (4,  'NICHE_COMEDY',            1.40, true, '40% surcharge for parody and satire creators',            'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (5,  'NICHE_FASHION',           1.10, true, '10% surcharge for fashion and lifestyle content',         'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (6,  'NICHE_GAMING',            1.15, true, '15% surcharge for gaming content',                        'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (7,  'PLATFORM_TIKTOK',         1.30, true, '30% surcharge for TikTok accounts due to virality',          'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (8,  'PLATFORM_X',              1.40, true, '40% surcharge for X due to high public discourse risk',      'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (9,  'PLATFORM_INSTAGRAM',      1.00, true, 'Baseline risk for Instagram content',                        'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (10, 'PLATFORM_YOUTUBE',        0.95, true, '5% discount for YouTube long-form content',                  'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (11, 'FOLLOWER_TIER_NANO',      1.00, true, 'Base tier — no surcharge for nano-influencers',              'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (12, 'FOLLOWER_TIER_MICRO',     1.05, true, '5% surcharge for micro-influencers',                         'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (13, 'FOLLOWER_TIER_MID',       1.10, true, '10% surcharge for mid-tier influencers',                     'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (14, 'FOLLOWER_TIER_MACRO',     1.20, true, '20% surcharge for macro-influencers',                        'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (15, 'FOLLOWER_TIER_MEGA',      1.35, true, '35% surcharge for mega-influencers',                         'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO risk_parameters (id, param_key, multiplier, active, description, update_note, last_modified_by, last_modified_date, created_at, updated_at) KEY(id)
  VALUES (16, 'FOLLOWER_TIER_CELEBRITY', 1.55, true, '55% surcharge for celebrity-level accounts',                 'Initial setup', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
