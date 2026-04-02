# The Halo - Backend Architecture (Code-Level Dictionary)

*This document is generated strictly by reading the source code, outlining the exact function signatures and layer integrations.*

## Configuration & Security Layer
`com.thehalo.halobackend.security` & `com.thehalo.halobackend.config`
- **SecurityConfig**: Defines `SecurityFilterChain`, disables CSRF, uses stateless session management, and applies `JwtAuthenticationFilter`. Also maps `/api/v1/auth/**`, `/api/v1/public/**`, and `/swagger-ui/**` as fully open endpoints.
- **JwtAuthenticationFilter**: Intercepts requests, extracts the JWT from the `Authorization: Bearer` header, and validates it using `JwtService`. If valid, it binds a `CustomUserDetails` object (containing `id`, `email`, and `roles`) to the `SecurityContextHolder`.
- **WebConfig**: Configures global CORS. *Note: Previously handled static file routes, but these were removed in favor of secure JWT streaming via controllers.*
- **GeminiConfig**: Binds application properties for the Gemini REST API integration (`gemini.url`, `gemini.key`, `gemini.tokens`).

## Controllers Layer (`com.thehalo.halobackend.controller`)
Controllers map incoming HTTP traffic and return standard wrapped `HaloApiResponse<T>` objects.

### Authentication & IAM
- **`AuthController`**:
  - `POST /api/v1/auth/login`: Authenticates an influencer user.
  - `POST /api/v1/auth/register`: Signs up a new influencer.
  - `POST /api/v1/auth/refresh`: Refreshes expired tokens.
  - `POST /api/v1/auth/staff-login`: Specific endpoint for admin/staff login routing.
- **`IamController`**:
  - `POST /api/v1/iam/staff`: Creates new staff users.
  - `GET /api/v1/iam/staff`: Retrieves all staff members.
  - `GET /api/v1/iam/staff/{id}`: Retrieves specific staff by ID.
  - `PUT /api/v1/iam/staff/{id}`: Updates staff profile.
  - `DELETE /api/v1/iam/staff/{id}`: Soft-deactivates staff.
  - `GET /api/v1/iam/staff/by-role/{role}`: Filters staff by specific `RoleName`.
  - `GET /api/v1/iam/sessions`: Fetches all active non-revoked refresh tokens.
- **`IamDashboardController`**:
  - `GET /api/v1/iam/dashboard/overview`: Returns an aggregated count of users, active policies, verified/unverified platforms, and active staff.

### Influencer Data
- **`UserPlatformController`**:
  - `GET /api/v1/platforms`: Returns all platforms owned by the authenticated influencer.
  - `GET /api/v1/platforms/{id}`: Returns a specific platform detail.
  - `POST /api/v1/platforms`: Adds a new social media platform URL/handle.
  - `PUT /api/v1/platforms/{id}`: Updates an existing platform.
  - `DELETE /api/v1/platforms/{id}`: Deletes a platform.
  - `GET /api/v1/platforms/verified` & `GET /api/v1/platforms/any`: Boolean checks for onboarding gates.

### Products & Underwriting
- **`ProductController`**: 
  - `GET .../public-products` & `GET .../public-products/{id}`: Unauthenticated access for the landing page.
  - `GET .../products` & `GET .../products/{id}`: Internal methods to retrieve product details.
  - `POST .../products`: Admin creation of an insurance product.
  - `PUT .../products/{id}`: Admin edit of a product.
- **`RiskParameterController`**:
  - `GET`, `POST`, `PUT` across `/api/v1/risk-parameters`: Manages specific modifiers (like `PLATFORM_TIKTOK` or `NICHE_FOOD`) and their numerical multipliers.
  - `POST .../risk-parameters/ai-suggest`: Connects to `AiRiskService` (via Gemini API) to suggest an optimal multiplier based on the key.
- **`QuotePricingController`**:
  - `POST /api/v1/pricing/calculate`: Computes an estimated base premium based on followers and engagement rate.
  - `POST /api/v1/pricing/quick-estimate`: Generates a fast estimate without requiring full authentication.
- **`QuoteController`**:
  - `GET /api/v1/quotes` & `GET /{id}`: Gets an influencer's quotes.
  - `POST /api/v1/quotes`: Submits a custom quote request, moving it to `PENDING` status.
  - `POST /api/v1/quotes/{id}/accept`: Influencer accepts an `APPROVED` quote.
  - `PUT /api/v1/quotes/{id}`: Influencer rejects/updates the quote.
- **`UnderwriterController`**:
  - `GET .../pending`, `GET .../assigned`, `GET .../priority-queue`: Returns paginated lists of quotes for underwriting review.
  - `POST .../{id}/assign`: Locks a quote to the current Underwriter.
  - `POST .../{id}/approve` & `POST .../{id}/reject`: Finalizes manually reviewed quotes.
  - `GET .../{id}/ai-narrative`: Triggers `AiRiskService` to generate a 2-3 paragraph textual risk profile via Gemini API.

### Claims & Files
- **`ClaimController`**:
  - `GET /api/v1/claims` & `GET /{id}`: Retrieves claims for the authenticated user.
  - `GET /api/v1/claims/officer/...`: Retrieves claims assigned to or pending for a Claims Officer.
  - `POST /api/v1/claims`: Files a new claim with initial form data.
  - `POST /api/v1/claims/{id}/officer-approve` & `deny`: Concludes the claim review.
  - `POST /api/v1/claims/search`: Paginated search portal.
- **`ClaimDocumentController`**:
  - `POST /api/v1/claims/{id}/documents`: Accepts a `MultipartFile` and permanently saves it to the `FileStorageService` mapped to a claim.
- **`FileController`**:
  - `GET /api/v1/files/download`: Requires a valid JWT, parses `filePath` param, and returns the binary data of the file via an `Octet-Stream`.

### AI Integrations
- **`AiChatController`**:
  - `POST /api/v1/ai/chat`: Secured chat endpoint integrating with the context-aware `AiChatService` and Gemini backend.
  - `POST /api/v1/ai/chat/public`: Unauthenticated chat endpoint.

## Services Layer (`com.thehalo.halobackend.service`)
Core business logic, annotated with `@Service`. All significant state manipulation methods are wrapped in `@Transactional` for atomicity.

### 1. Registration & Auth (`auth`, `user`)
- **`AuthService.java`**: Handles actual `AppUser` creation, stores passwords securely via `PasswordEncoder`, generates JWT payload, and establishes the initial user profile.
- **`CustomUserDetailsService.java`**: Implements Spring Security's `UserDetailsService`. Queries the DB by email and returns a highly customized user object mapping all internal roles.

### 2. Underwriting Engine (`quote`, `underwriter`, `underwriting`)
- **`QuotePricingService.java`**: The actuarial engine. Contains `calculatePersonalizedPremium` which fetches the `Product`'s base rate, iterates over matching `RiskParameter`s (like `NICHE_FOOD` or `PLATFORM_TIKTOK`), and applies their multipliers.
- **`UnderwriterAssignmentService.java`**: Runs scheduled tasks or handles API calls to queue unassigned high-risk quotes to active Underwriters using a robust optimistic locking scheme.

### 3. Claims Administration (`claim`)
- **`ClaimServiceImpl.java`**: Logic for submitting an initial claim (verifying active policy status), fetching details, and authorizing Claim Officer reviews.
- **`ClaimDocumentService.java`**: Works closely with `FileStorageStrategy` to stream multiparts up to the local disk and track their paths inside the `Claim_Document` JOIN table.

### 4. Third-Party Integrations (`platform`, `ai`)
- **`SocialMediaVerificationService.java`**: Communicates with external Mock APIs (e.g. `SocialMediaMockController` behaving as Instagram/TikTok endpoints) to fetch live subscriber and engagement metrics.
- **`AiRiskService.java` & `GeminiService.java`**: Formulates risk prompts mapping the user's platform stats, and sends them via `RestTemplate` POST to `https://generativelanguage.googleapis.com`. Parses the JSON utilizing Google's precise v1beta schema returning risk narratives and parameter suggestions.

## Repository Layer (`com.thehalo.halobackend.repository`)
Interfaces extending `JpaRepository<T, ID>`. Contain highly specialized HQL (Hibernate Query Language) for complex joins.

- **`ClaimRepository`**: Features custom `@Query` returning claims with eager-fetched `profile`, `platform`, and `policy` objects to avoid `N+1` selection issues.
- **`PolicyRepository`**: Queries for `findByStatus` tracking active vs. lapsed coverage.
- **`AppUserRepository`**: Standard derived queries `findByEmail(String email)`.
- **`UserPlatformRepository`**: Retrieves combined User/Platform state ensuring security checks (a user can only see platforms they own via `findByIdAndUserId`).

## Models Layer (`com.thehalo.halobackend.model`)
Standard JPA Entities.
- **`BaseEntity.java`**: An `@MappedSuperclass` defining `createdAt`, `updatedAt`, and `createdBy`. All major tables extend this for automatic auditing.
- **`AppUser.java`**: One-to-Many mapping with `UserPlatform` and `QuoteRequest`.
- **`UserPlatform.java`**: Combines the static list of `Platform` (Instagram, etc.) with the User's specific `handle` and dynamic `followerCount`.
- **`QuoteRequest.java`**: Represents a mutable path through the Underwriting process ending in `QuoteStatus.APPROVED`.
- **`Policy.java`**: The immutable coverage contract spawned once a Quote is approved and Payment is completed via `PaymentServiceImpl`.

---
*End of Backend Architecture Documentation.*
