# In-app Vacation Home (Fritidshus) Purchase — Design Spec

## Goal

Add an in-app vacation home (`SE_VACATION_HOME`) insurance purchase flow, reusing the existing apartment/car purchase architecture. Introduce a forward-looking `feature-purchase-house` module that will later host the SE_HOUSE flow (added in a follow-up PR) as a sibling form composable.

The flow mirrors the apartment/car shape: a product-specific form screen, then handoff to the shared `purchase-common` screens for tier selection, summary, BankID signing, and success/failure.

## Base branch

This work depends on the `purchase-common` module and the apartment/car flow patterns, which currently live on `feat/in-app-car-purchase`. This PR must branch from `feat/in-app-car-purchase` (or whichever branch the common module has been merged into).

## Product names and tiers

- Storefront `ProductName.SE_VACATION_HOME` maps to two quote types: `SE_VACATION_HOME_BAS` and `SE_VACATION_HOME_STANDARD`.
- A single `productName = "SE_VACATION_HOME"` is passed to `priceIntentCreate`. The backend returns both tier offers after `priceIntentConfirm`. Users pick between BAS and STANDARD on the shared `SelectTierDestination`.

## Module structure

### New module: `feature-purchase-house`

```
app/feature/feature-purchase-house/
├── build.gradle.kts
└── src/main/
    ├── graphql/
    │   ├── HouseShopSessionCreateMutation.graphql
    │   ├── HousePriceIntentCreateMutation.graphql
    │   ├── HousePriceIntentDataUpdateMutation.graphql
    │   ├── HousePriceIntentConfirmMutation.graphql
    │   ├── HouseMemberContactInfoQuery.graphql
    │   └── HouseProductOfferFragment.graphql
    └── kotlin/com/hedvig/android/feature/purchase/house/
        ├── data/
        │   ├── HousePurchaseModels.kt
        │   ├── CreateHouseSessionAndPriceIntentUseCase.kt
        │   └── SubmitVacationHomeFormAndGetOffersUseCase.kt
        ├── ui/vacationhome/
        │   ├── VacationHomeFormDestination.kt
        │   └── VacationHomeFormViewModel.kt
        ├── navigation/
        │   ├── HousePurchaseDestination.kt
        │   └── HousePurchaseNavGraph.kt
        └── di/HousePurchaseModule.kt
```

**Why `feature-purchase-house` not `feature-purchase-vacation-home`:** SE_HOUSE and SE_VACATION_HOME share ~60% of their racoon form fields (street, zip, livingSpace, yearOfConstruction, numberOfBathrooms, isSubleted, extraBuildings, email, ssn) and use the same Apollo mutations (only `productName` and form-data keys differ). Hosting both in one module shares the Apollo operations, use cases, nav graph, and DI scaffolding (~500 LOC of structural code) instead of duplicating them. Two separate form composables keep product-specific UI focused (no `if (product == X)` branching).

**Apollo operation naming:** `House`-prefixed (e.g. `HousePriceIntentCreate`) to avoid Apollo Kotlin classpath conflicts with apartment's and car's identical operations. SDL is identical to the apartment/car versions; only the operation names differ.

**Build config (`build.gradle.kts`):**
- Plugins: `hedvig.android.library`, `hedvig.gradle.plugin`
- Hedvig DSL: `apollo("octopus")`, `serialization()`, `compose()`
- Dependencies: mirror `feature-purchase-car` (purchase-common, core-common-public, core-resources, design-system-hedvig, navigation-compose, koin, apollo, etc.)

### Modified modules

#### `feature-insurances`

`InsuranceGraph.kt` `onCrossSellClick`:

- Add `onNavigateToHousePurchase: (productName: String) -> Unit` callback parameter.
- Route URLs containing `fritidshusforsakring` (sv) or `vacation-home` (en) to `onNavigateToHousePurchase("SE_VACATION_HOME")`.
- The fritidshus branch must come **before** the apartment branches so future SE_HOUSE URLs (`hemforsakring/villaforsakring`) don't get stolen by apartment's generic `hemforsakring`/`home-insurance` match. For SE_VACATION_HOME alone there is no substring conflict, but the ordering is defensive.

#### `app` (main application module)

- Register `housePurchaseModule` in `ApplicationModule`.
- Add `housePurchaseNavGraph(navController, popBackStack, finishApp, crossSellAfterFlowRepository)` to `HedvigNavHost`.
- Wire `onNavigateToHousePurchase = { productName -> navController.navigate(HousePurchaseGraphDestination(productName)) }` at the insurances graph callsite.

## Module dependency graph

```
feature-purchase-apartment ──> purchase-common
feature-purchase-car ───────> purchase-common
feature-purchase-house ─────> purchase-common      [new]
app ──> feature-purchase-apartment
app ──> feature-purchase-car
app ──> feature-purchase-house                      [new]
app ──> purchase-common
```

Feature modules continue not to depend on each other; all share `purchase-common` (a library module).

## Data flow

```
1. User taps fritidshus cross-sell in insurances tab
2. InsuranceGraph routes "fritidshusforsakring" / "vacation-home" URL
   → onNavigateToHousePurchase("SE_VACATION_HOME")
3. Navigate to HousePurchaseGraphDestination(productName = "SE_VACATION_HOME")
4. VacationHomeFormDestination loads:
   a. CreateHouseSessionAndPriceIntentUseCase(productName):
      - HouseShopSessionCreate(CountryCode.SE) → shopSessionId
      - HousePriceIntentCreate(shopSessionId, productName) → priceIntentId
      - HouseMemberContactInfo() → ssn, email
   b. User fills 8 form fields (see below)
   c. SubmitVacationHomeFormAndGetOffersUseCase(priceIntentId, formMap):
      - HousePriceIntentDataUpdate(priceIntentId, data) — see form-data keys below
      - HousePriceIntentConfirm(priceIntentId) → list of ProductOffer
      - Map each offer to HouseTierOffer (uses HouseProductOfferFragment)
5. SelectTierDestination (purchase-common): user picks BAS vs STANDARD
6. PurchaseSummaryDestination (purchase-common): review selected tier
7. SigningDestination (purchase-common): BankID polling + QR fallback
8. PurchaseSuccessDestination (purchase-common): confirmation
```

## V1 form fields (single scrolling screen)

| # | Field | Compose component | Validation | Form-data key | Type sent |
|---|-------|-------------------|------------|---------------|-----------|
| 1 | Street | `HedvigTextField` | non-empty | `street` | string |
| 2 | Zip code | `HedvigTextField` (numeric, max length 5) | exactly 5 digits | `zipCode` | string |
| 3 | Multiple owners | Radio pair (`Ja`/`Nej`) | required selection | `multipleOwners` | boolean |
| 4 | Year of construction | `HedvigTextField` (numeric) | 1700–current year inclusive | `yearOfConstruction` | int |
| 5 | Living space (m²) | `HedvigTextField` (numeric) | > 0 | `livingSpace` | int |
| 6 | Water connected | Radio pair (`Ja`/`Nej`) | required selection | `hasWaterConnected` | boolean |
| 7 | Number of bathrooms | `HedvigStepper` (1–10, default 1) | stepper-bounded | `numberOfBathrooms` | int |
| 8 | Subleted | Radio pair (`Ja`/`Nej`) | required selection | `isSubleted` | boolean |

**Auto-injected (never shown in the form):**
- `ssn` — fetched from `currentMember.ssn` during session creation; fail-fast with `ErrorMessage()` if null
- `email` — fetched from `currentMember.email`
- `extraBuildings` — sent as empty array `[]` in V1 (UI deferred, see scope)

**Form errors** surface as field-level `errorState` on the input, or as a top-level `ErrorDialog` for submit-level errors (matches the car form pattern).

## Domain models

```kotlin
internal data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
  val ssn: String,
  val email: String,
)

internal data class HouseOffers(
  val productDisplayName: String,
  val offers: List<HouseTierOffer>,
)

internal data class HouseTierOffer(
  val offerId: String,
  val tierDisplayName: String,
  val tierDescription: String,
  val grossPrice: UiMoney,
  val netPrice: UiMoney,
  val usps: List<String>,
  val exposureDisplayName: String,
  val deductibleDisplayName: String?,
  val hasDiscount: Boolean,
)
```

Mapping `HouseTierOffer` → `TierOfferData` (purchase-common's nav-passable model) happens at the nav-graph boundary, exactly as in `CarPurchaseNavGraph`.

## Navigation

```kotlin
@Serializable
data class HousePurchaseGraphDestination(val productName: String) : Destination

internal sealed interface HousePurchaseDestination : Destination {
  @Serializable
  data object Form : HousePurchaseDestination
}
```

`HousePurchaseNavGraph` wires:

```
HousePurchaseGraph(productName)
  startDestination = Form
    Form         → SelectTier(params)   [purchase-common]
    SelectTier   → Summary(params)      [purchase-common]
    Summary      → Signing(params)      [purchase-common]
    Signing      → Success(startDate)   [purchase-common]
                   (typedPopUpTo<HousePurchaseGraphDestination>(inclusive = true))
```

`SelectTier`, `Summary`, `Signing`, `Success`, `Failure` are imported from `purchase-common.navigation.PurchaseCommonDestination` — identical pattern to `CarPurchaseNavGraph`.

## DI module

```kotlin
val housePurchaseModule = module {
  single<CreateHouseSessionAndPriceIntentUseCase> {
    CreateHouseSessionAndPriceIntentUseCaseImpl(apolloClient = get())
  }
  single<SubmitVacationHomeFormAndGetOffersUseCase> {
    SubmitVacationHomeFormAndGetOffersUseCaseImpl(apolloClient = get())
  }
  viewModel<VacationHomeFormViewModel> { params ->
    VacationHomeFormViewModel(
      productName = params.get(),
      createHouseSessionAndPriceIntentUseCase = get(),
      submitVacationHomeFormAndGetOffersUseCase = get(),
    )
  }
}
```

`CreateHouseSessionAndPriceIntentUseCase` is product-agnostic (takes `productName: String`) so the future SE_HOUSE form composable can reuse it. `SubmitVacationHomeFormAndGetOffersUseCase` is vacation-home-specific because the form-data keys differ from SE_HOUSE; the future `SubmitHouseFormAndGetOffersUseCase` will be a sibling.

## Testing and verification

Following the apartment/car precedent (no JVM unit tests for the form layer). Verification gates:

- `./gradlew :feature-purchase-house:assemble` — module builds
- `./gradlew :app:assembleDevelopDebug` — full app builds
- `./gradlew ktlintFormat && ./gradlew ktlintCheck`
- Manual emulator verification per the `verifying-android-changes-in-emulator` skill — golden path: cross-sell → form → tier select → summary → BankID → success. Edge cases: form validation, navigate-up at each step, error states (network failure, userError on PriceIntentDataUpdate, missing offers).

**Compose previews required** (matches apartment): empty state, filled state, loading session state, error state.

## Lokalise / translations

Per CLAUDE.md, string resource XML files are managed by Lokalise and must not be edited directly. All new UI text is hardcoded in Swedish in the Kotlin source with a `// TODO: Add "<English>" / "<Swedish>" to Lokalise` comment, mirroring how the car form shipped.

New strings expected:
- Form title and subtitle
- 8 field labels and validation messages
- Radio Ja / Nej labels (reuse if existing strings cover them)
- Submit button text (likely reuse "Beräkna pris" from car/apartment)
- Bathrooms stepper value label

## Edge cases and error handling

- **Missing member SSN** → `CreateHouseSessionAndPriceIntentUseCase` raises `ErrorMessage()` (same as car).
- **Empty offers from confirm** → raises `ErrorMessage()`.
- **`userError` on `priceIntentDataUpdate` or `priceIntentConfirm`** → message surfaced in `ErrorDialog`.
- **Apollo `safeExecute` left-side failures** → generic error logged via `logcat(LogPriority.ERROR)` + `ErrorMessage()`.

## Key design decisions

1. **Single feature module for SE_VACATION_HOME and (future) SE_HOUSE.** Same Apollo mutations + 60% field overlap + shared form components make a single module cheaper than duplicating the apartment/car-style scaffolding. Two separate form composables keep product-specific UI focused.

2. **`House`-prefixed Apollo operations.** Required for Apollo Kotlin codegen isolation from apartment/car modules (operation names must be unique across the classpath). SDL is identical.

3. **Vacation home first, house second.** Smaller PR, mirrors how apartment shipped (one product per PR). The follow-up SE_HOUSE PR is then small: a new `HouseFormDestination` composable + a productName branch in the nav graph + a new `SubmitHouseFormAndGetOffersUseCase`.

4. **`extraBuildings` UI deferred to a separate PR.** V1 always sends `extraBuildings: []`. Pricing won't reflect extra buildings, but users can complete the flow. The follow-up PR adds the add/remove dialog and card list (~14 building types).

5. **`CreateHouseSessionAndPriceIntentUseCase` accepts `productName: String`.** Shared between SE_VACATION_HOME and (future) SE_HOUSE without changes.

6. **Cross-sell URL routing ordering.** Fritidshus check placed before apartment branches in `InsuranceGraph.kt` to defend against future SE_HOUSE URL conflicts with apartment's generic `hemforsakring`/`home-insurance` match.

## Out of scope

- `extraBuildings` dialog/list UI (deferred to a separate follow-up PR; v1 sends empty array)
- SE_HOUSE form composable (separate follow-up PR within the same module)
- Fixing the latent bug where apartment's `hemforsakring`/`home-insurance` keyword match would steal future SE_HOUSE URLs (`hemforsakring/villaforsakring`, `home-insurance/house`) — flag for SE_HOUSE PR
- Bundle discount UI surfacing for vacation-home offers (purchase-common already handles bundle discount fields generically)
- Tracking / Datadog events specific to vacation-home purchase
