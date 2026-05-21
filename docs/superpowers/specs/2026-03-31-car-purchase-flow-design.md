# Car Purchase Flow - Design Spec

## Goal

Add in-app car insurance purchase flow, reusing the existing apartment purchase architecture. Extract shared purchase screens into a common module so both apartment and car flows share tier selection, summary, signing, success, and failure screens.

## Module Structure

### New modules

#### `feature-purchase-common`

Shared purchase screens, use cases, models, and GraphQL operations that are product-agnostic.

**Screens:**
- `SelectTierDestination` + `SelectTierViewModel` — tier/deductible selection with grouped offers
- `PurchaseSummaryDestination` + `PurchaseSummaryViewModel` — selected offer details, triggers signing
- `SigningDestination` + `SigningViewModel` — BankID polling, QR code fallback
- `PurchaseSuccessDestination` — confirmation with optional start date
- `PurchaseFailureDestination` — error display with retry

**Use cases:**
- `AddToCartAndStartSignUseCase` — calls `ShopSessionCartEntriesAdd` + `ShopSessionStartSign`
- `PollSigningStatusUseCase` — polls `ShopSessionSigning` with `NetworkOnly` fetch policy

**Models:**
- `TierOfferData` — serializable offer data passed between screens (offerId, tier name/description, gross/net pricing, USPs, exposure, deductible, discount flag)
- `SelectTierParameters` — shopSessionId, offers list, productDisplayName
- `SummaryParameters` — shopSessionId, selectedOffer, productDisplayName
- `SigningParameters` — signingId, autoStartToken, startDate
- `SigningStart` — signingId, autoStartToken
- `SigningPollResult` — status, liveQrCodeData
- `SigningStatus` — enum: PENDING, SIGNED, FAILED

**GraphQL operations (moved from apartment, drop `Apartment` prefix):**
- `ShopSessionCartEntriesAddMutation.graphql`
- `ShopSessionStartSignMutation.graphql`
- `ShopSessionSigningQuery.graphql`
- `ProductOfferFragment.graphql`

**Build config:**
- Plugins: `hedvig.android.library`, `hedvig.gradle.plugin`
- Hedvig DSL: `apollo("octopus")`, `serialization()`, `compose()`
- Dependencies: same as current apartment module minus form-specific deps

#### `feature-purchase-car`

Car-specific form, use cases, navigation, and DI.

**Screens:**
- `CarFormDestination` + `CarFormViewModel` — car insurance form

**Form fields (matching racoon SE_CAR template):**
| Field | Type | Validation | Notes |
|-------|------|-----------|-------|
| SSN (personnummer) | Text, numeric | 12 digits (YYYYMMDDXXXX) | Swedish personal number |
| Registration number | Text | 3 letters + 2 digits + 1 alphanumeric (e.g. ABC 123) | Auto-uppercase, auto-space after 3rd char |
| Mileage | Dropdown | Required selection | Options: 1000, 1500, 2000, 2500, 2500+ (Scandinavian miles) |
| Street address | Text | Non-empty | |
| Zip code | Text, numeric | 5 digits | |
| Email | Text | Valid email format | |

**Use cases:**
- `CreateCarSessionAndPriceIntentUseCase` — creates ShopSession + PriceIntent for `SE_CAR`
- `SubmitCarFormAndGetOffersUseCase` — submits form data map (`ssn`, `registrationNumber`, `mileage`, `street`, `zipCode`, `email`) via `PriceIntentDataUpdate`, then confirms via `PriceIntentConfirm`

**GraphQL operations (car-prefixed for Apollo codegen isolation):**
- `CarShopSessionCreateMutation.graphql` — same schema as apartment, different operation name
- `CarPriceIntentCreateMutation.graphql`
- `CarPriceIntentDataUpdateMutation.graphql`
- `CarPriceIntentConfirmMutation.graphql`

**Navigation:**
- `CarPurchaseGraphDestination(productName: String)` — entry point
- `CarPurchaseNavGraph` — wires: CarForm -> SelectTier -> Summary -> Signing -> Success/Failure
- All post-form destinations come from `feature-purchase-common`

**DI:**
- `carPurchaseModule` — Koin module registering car-specific use cases and ViewModels

### Modified modules

#### `feature-purchase-apartment`

Slimmed down — shared code extracted to common.

**Keeps:**
- `ApartmentFormDestination` + `ApartmentFormViewModel`
- `CreateSessionAndPriceIntentUseCase`
- `SubmitFormAndGetOffersUseCase`
- `ApartmentPurchaseNavGraph`
- Apartment-specific GraphQL: `ShopSessionCreate`, `PriceIntentCreate`, `PriceIntentDataUpdate`, `PriceIntentConfirm`

**Removes (moved to common):**
- `SelectTierDestination`, `SelectTierViewModel`
- `PurchaseSummaryDestination`, `PurchaseSummaryViewModel`
- `SigningDestination`, `SigningViewModel`
- `PurchaseSuccessDestination`, `PurchaseFailureDestination`
- `AddToCartAndStartSignUseCase`, `PollSigningStatusUseCase`
- Shared models (`TierOfferData`, `SigningParameters`, etc.)
- `ShopSessionCartEntriesAdd`, `ShopSessionStartSign`, `ShopSessionSigning`, `ProductOfferFragment` GraphQL files

**Adds dependency on:** `feature-purchase-common`

#### `feature-insurances`

Update cross-sell routing to support both products:
- Currently hardcoded: `onNavigateToApartmentPurchase("SE_APARTMENT_RENT")`
- Add `onNavigateToCarPurchase: (productName: String) -> Unit` callback
- Route based on cross-sell product name prefix (`SE_APARTMENT_*` vs `SE_CAR*`)

#### `app` (main application module)

- Register `carPurchaseModule` in `ApplicationModule`
- Add `carPurchaseNavGraph()` call in `HedvigNavHost`
- Wire `onNavigateToCarPurchase` in insurances graph to navigate to `CarPurchaseGraphDestination`

## Module Dependency Graph

```
feature-purchase-apartment ──> feature-purchase-common
feature-purchase-car ──────> feature-purchase-common
app ──> feature-purchase-apartment
app ──> feature-purchase-car
app ──> feature-purchase-common
```

Feature modules do not depend on each other — only on common (a library module).

## Data Flow

### Car Purchase Flow

```
1. User taps car cross-sell in insurances tab
2. Navigate to CarPurchaseGraphDestination(productName = "SE_CAR")
3. CarFormScreen loads:
   a. CreateCarSessionAndPriceIntentUseCase creates ShopSession + PriceIntent
   b. User fills: SSN, registration number, mileage, street, zip, email
   c. SubmitCarFormAndGetOffersUseCase sends PriceIntentDataUpdate + PriceIntentConfirm
   d. Returns list of TierOffer (mapped from ProductOfferFragment)
4. SelectTierScreen (shared): user picks coverage tier + deductible
5. PurchaseSummaryScreen (shared): user reviews and confirms
6. SigningScreen (shared): BankID signing with polling
7. PurchaseSuccessScreen (shared): confirmation
```

## Key Design Decisions

1. **Separate GraphQL operation names per product** — Apollo generates Kotlin classes per operation name. Even though the mutations are identical in schema, each product module gets its own prefixed operations to avoid classpath conflicts.

2. **Common module is a library, not a feature** — This lets both feature modules depend on it without violating the "features can't depend on features" build rule.

3. **Form data is a `Map<String, Any>`** — The `PriceIntentDataUpdate` mutation accepts `PricingFormData!` which is a JSON scalar. Each product builds its own map with different keys. No shared form abstraction needed.

4. **Shared screens are product-agnostic** — Tier selection, summary, signing, success, and failure screens operate purely on `TierOfferData`, `SummaryParameters`, and `SigningParameters`. They have no knowledge of apartment vs car.

5. **Mileage as dropdown** — Matches racoon's CarMileageField implementation with fixed options (1000, 1500, 2000, 2500, 2500+) in Scandinavian miles.
