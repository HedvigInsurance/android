# Pet Purchase Flow - Design Spec

## Goal

Add in-app pet insurance purchase flow for `SE_PET_DOG` and `SE_PET_CAT`, reusing the existing `feature-purchase-common` module that was extracted during the car flow work. One `feature-purchase-pet` module handles both species, selecting fields and breed list by `productName`.

## Source of Truth

- Racoon templates: `apps/store/src/features/priceCalculator/priceTemplates/SE_PET_DOG.ts` and `SE_PET_CAT.ts` (form fields and validation).
- Storefront resolver: `src/main/kotlin/com/hedvig/storefront/shop/product/ProductName.kt::SE_PET_DOG` / `SE_PET_CAT` (UW payload, minimum start dates, exposure).
- Storefront schema: `priceIntentAvailableBreeds(animal: PriceIntentAnimal!)` returns `[PriceIntentAnimalBreed(id, displayName, isMixedBreed)]`.

## Module Structure

### New module: `feature-purchase-pet`

Mirrors `feature-purchase-car` structure. Build config: `hedvig.android.library`, `hedvig.gradle.plugin`, `apollo("octopus")`, `serialization()`, `compose()`.

```
feature-purchase-pet/
├── build.gradle.kts
├── src/main/graphql/
│   ├── PetShopSessionCreateMutation.graphql
│   ├── PetPriceIntentCreateMutation.graphql
│   ├── PetPriceIntentDataUpdateMutation.graphql
│   ├── PetPriceIntentConfirmMutation.graphql
│   ├── PetProductOfferFragment.graphql
│   ├── PetMemberContactInfoQuery.graphql
│   └── PetAvailableBreedsQuery.graphql            ← pet-specific
└── src/main/kotlin/com/hedvig/android/feature/purchase/pet/
    ├── data/
    │   ├── PetPurchaseModels.kt
    │   ├── CreatePetSessionAndPriceIntentUseCase.kt
    │   ├── GetPetBreedsUseCase.kt                 ← pet-specific
    │   └── SubmitPetFormAndGetOffersUseCase.kt
    ├── ui/form/
    │   ├── PetFormDestination.kt
    │   └── PetFormViewModel.kt
    ├── navigation/
    │   ├── PetPurchaseDestination.kt
    │   └── PetPurchaseNavGraph.kt
    └── di/
        └── PetPurchaseModule.kt
```

### Modified modules

- `feature-insurances` — cross-sell routing: add `onNavigateToPetPurchase` callback and two new URL branches (`dog-insurance`/`hundforsakring` → `SE_PET_DOG`; `cat-insurance`/`kattforsakring` → `SE_PET_CAT`). The actual cross-sell URL slugs are an open question; mark with `// TODO: verify against actual cross-sell URLs`.
- `app` — register `petPurchaseModule` in `ApplicationModule`; add `petPurchaseNavGraph(...)` call in `HedvigNavHost`; wire `onNavigateToPetPurchase` to navigate to `PetPurchaseGraphDestination(productName)`.

### Unchanged

- `feature-purchase-common` is consumed as-is. No changes.

## Module Dependency Graph

```
feature-purchase-apartment ──> feature-purchase-common
feature-purchase-car ──────> feature-purchase-common
feature-purchase-pet ──────> feature-purchase-common
app ──> {feature-purchase-apartment, feature-purchase-car, feature-purchase-pet, feature-purchase-common}
```

## GraphQL Operations

All mutations mirror the car ones with `Pet` prefix (Apollo codegen isolation, identical schemas to backend). One new pet-specific query.

```graphql
mutation PetShopSessionCreate($countryCode: CountryCode!) {
  shopSessionCreate(input: { countryCode: $countryCode }) { id }
}

mutation PetPriceIntentCreate($shopSessionId: UUID!, $productName: String!) {
  priceIntentCreate(input: { shopSessionId: $shopSessionId, productName: $productName }) { id }
}

mutation PetPriceIntentDataUpdate($priceIntentId: UUID!, $data: PricingFormData!) {
  priceIntentDataUpdate(priceIntentId: $priceIntentId, data: $data) {
    priceIntent { id }
    userError { message }
  }
}

mutation PetPriceIntentConfirm($priceIntentId: UUID!) {
  priceIntentConfirm(priceIntentId: $priceIntentId) {
    priceIntent {
      id
      offers { ...PetProductOfferFragment }
    }
    userError { message }
  }
}

# PetProductOfferFragment is structurally identical to CarProductOfferFragment.

query PetMemberContactInfo {
  currentMember { id ssn email }
}

query PetAvailableBreeds($animal: PriceIntentAnimal!) {
  priceIntentAvailableBreeds(animal: $animal) {
    id
    displayName
    isMixedBreed
  }
}
```

**`productName` values:** `"SE_PET_DOG"` or `"SE_PET_CAT"` (full strings from storefront `ProductName` enum — not `"SE_DOG"` / `"SE_CAT"`, which are contract types).

**`PriceIntentAnimal` mapping:** derive in the use case — `if (productName == "SE_PET_CAT") CAT else DOG`. Never carry the animal as a separate nav arg.

## Form Specification

`PetFormDestination` is a single composable. Field 6 (the last species-specific question) renders differently based on `productName`. All other fields are identical for dog and cat.

### Fields (rendered in order)

| # | Field | UI | Validation | Submitted as |
|---|-------|----|-----------|----|
| 1 | Pet name | `HedvigTextField` | Non-empty, trimmed | `"name": String` |
| 2 | Breed | `DropdownWithDialog`, single-select, scrollable list (no search) | Required | `"breeds": List<String>` — single-element list of breed `id`. If user picks the mixed-breed option, submit `"breeds": []` (matches racoon's `parseDogBreedsField` filter). |
| 3 | Birth date | `HedvigDatePicker` | Required, `< today`, `> 1990-01-01` | `"birthDate": String` ISO `YYYY-MM-DD` |
| 4 | Gender | radio (two options) | Required | `"gender": "MALE" \| "FEMALE"` |
| 5 | Is neutered | radio | Required | `"isNeutered": "true" \| "false"` (string, matches racoon) |
| 6 | **Dog:** Previous dog owner. **Cat:** Outside access. | radio | Required | Dog: `"isPreviousDogOwner": "true" \| "false"`. Cat: `"hasOutsideAccess": "true" \| "false"`. |
| 7 | Street | `HedvigTextField` | Non-empty | `"street": String` |
| 8 | Zip code | `HedvigTextField`, numeric only | Exactly 5 digits | `"zipCode": String` |

### Auto-fetched (not shown in form)

- `ssn` — from `currentMember.ssn`; SSN-missing case raises an error at session creation time (same as car flow).
- `email` — from `currentMember.email`; included in the submitted payload.

### Mixed-breed behavior

- Mixed-breed option is selectable in the dropdown.
- On submit, if the mixed-breed `id` is the chosen value, send `"breeds": []`.
- For non-mixed breeds, send `"breeds": [chosenId]`.
- (Cat templates also expose mixed-breed in racoon's selector but racoon's `parseCatBreedsField` does not filter it; the dog filter does. We treat both the same way on Android — empty list when mixed — because backend handles both shapes; deviation can be flagged in code review if storefront tests fail.)

### Backend-enforced validation (not in client)

- Pet too young / too old → backend returns `userError` from `PriceIntentDataUpdate` or `PriceIntentConfirm`. Show the message in `ErrorDialog`. Minimum start dates (dog +8 weeks, cat +12 weeks from `birthDate`) are enforced by storefront's `minimumStartDate` resolver.
- UW filters on breed / location → also backend-enforced via `userError`.

### Localization

All user-facing strings are hardcoded English with a `// TODO: Add "<English>" / "<Swedish>" to Lokalise` comment, per CLAUDE.md. Matches the convention used in the car form's validation messages (which are currently Swedish-only and should also be migrated).

## Use Cases

### `CreatePetSessionAndPriceIntentUseCase`

Identical shape to `CreateCarSessionAndPriceIntentUseCase`:
1. `PetShopSessionCreateMutation(CountryCode.SE)` → `shopSessionId`.
2. `PetPriceIntentCreateMutation(shopSessionId, productName)` → `priceIntentId`.
3. `PetMemberContactInfoQuery()` → `(ssn, email)`. Raise `ErrorMessage` if `ssn == null`.
4. Return `SessionAndIntent(shopSessionId, priceIntentId, ssn, email)`.

### `GetPetBreedsUseCase`

```kotlin
internal interface GetPetBreedsUseCase {
  suspend fun invoke(animal: PriceIntentAnimal): Either<ErrorMessage, List<Breed>>
}

internal data class Breed(val id: String, val displayName: String, val isMixedBreed: Boolean)
```

Executes `PetAvailableBreedsQuery(animal)` via `safeExecute`. Maps fragment to `Breed` domain model.

### `SubmitPetFormAndGetOffersUseCase`

Identical shape to `SubmitCarFormAndGetOffersUseCase`. Parameters:
- `priceIntentId: String`
- `productName: String` — used to map the species-specific question to the right key.
- `ssn: String`, `email: String` — auto-fetched at session creation, passed through.
- Typed form inputs: `name: String`, `breedId: String`, `isMixedBreed: Boolean`, `birthDate: LocalDate`, `gender: Gender` (enum `MALE`/`FEMALE`), `isNeutered: Boolean`, `speciesAnswer: Boolean`, `street: String`, `zipCode: String`.

Mapping rules inside the use case:
- `"breeds"` = `if (isMixedBreed) emptyList() else listOf(breedId)`.
- `speciesAnswer` is keyed as `"isPreviousDogOwner"` when `productName == "SE_PET_DOG"`, `"hasOutsideAccess"` when `productName == "SE_PET_CAT"`.
- `birthDate` serialized as ISO `YYYY-MM-DD`.
- Booleans serialized as the strings `"true"` / `"false"` (matches racoon).

Returns `PetOffers(productDisplayName, offers: List<PetTierOffer>)`. `PetTierOffer` is structurally identical to `CarTierOffer`.

## Navigation

```kotlin
// PetPurchaseDestination.kt
@Serializable
data class PetPurchaseGraphDestination(val productName: String) : Destination

internal sealed interface PetPurchaseDestination {
  @Serializable
  data object Form : PetPurchaseDestination, Destination
}
```

`PetPurchaseNavGraph` mirrors `carPurchaseNavGraph` exactly. The post-form destinations (`SelectTier`, `Summary`, `Signing`, `Success`, `Failure`) come from `feature-purchase-common`.

## Data Flow

```
1. User taps dog/cat cross-sell  →  onNavigateToPetPurchase("SE_PET_DOG" | "SE_PET_CAT")
2. Navigate to PetPurchaseGraphDestination(productName)
3. PetFormScreen LaunchedEffect (initial load):
   a. Parallel:
      - CreatePetSessionAndPriceIntentUseCase(productName) → SessionAndIntent
      - GetPetBreedsUseCase(animal derived from productName) → List<Breed>
   b. Both must succeed before form becomes interactive.
   c. User fills 8 fields.
   d. SubmitPetFormAndGetOffersUseCase(...) → PetOffers.
4. SelectTierScreen (shared) — pick tier + deductible.
5. PurchaseSummaryScreen (shared) — review.
6. SigningScreen (shared) — BankID with QR fallback.
7. PurchaseSuccessScreen (shared) — confirmation, returns to insurance tab.
```

## Error Paths

Match car behavior:

- Session creation or breeds query failure → full-screen `HedvigErrorSection` with retry. Retry refetches both.
- `PriceIntentDataUpdate` or `PriceIntentConfirm` `userError` → `ErrorDialog` with backend message (this is where "pet too young", invalid breed, etc. surface).
- Network failure during submit → `ErrorDialog` with generic message.
- Confirm returns empty offers list → generic `ErrorMessage`.

## DI

```kotlin
val petPurchaseModule = module {
  single<CreatePetSessionAndPriceIntentUseCase> { CreatePetSessionAndPriceIntentUseCaseImpl(apolloClient = get()) }
  single<GetPetBreedsUseCase> { GetPetBreedsUseCaseImpl(apolloClient = get()) }
  single<SubmitPetFormAndGetOffersUseCase> { SubmitPetFormAndGetOffersUseCaseImpl(apolloClient = get()) }

  viewModel<PetFormViewModel> { params ->
    PetFormViewModel(
      productName = params.get(),
      createPetSessionAndPriceIntentUseCase = get(),
      getPetBreedsUseCase = get(),
      submitPetFormAndGetOffersUseCase = get(),
    )
  }
}
```

## App Integration

1. `ApplicationModule.kt` — add `petPurchaseModule` to `includes(...)`.
2. `HedvigNavHost.kt` — call `petPurchaseNavGraph(navController, popBackStack, finishApp, crossSellAfterFlowRepository)` alongside `carPurchaseNavGraph` and `apartmentPurchaseNavGraph`.
3. Wire `onNavigateToPetPurchase = { productName -> navController.navigate(PetPurchaseGraphDestination(productName)) }` in the `insuranceGraph(...)` call.
4. `InsuranceGraph.kt` cross-sell `onCrossSellClick` — add `onNavigateToPetPurchase: (productName: String) -> Unit` parameter and URL branches:
   - `"dog-insurance" in lower || "hundforsakring" in lower` → `onNavigateToPetPurchase("SE_PET_DOG")`
   - `"cat-insurance" in lower || "kattforsakring" in lower` → `onNavigateToPetPurchase("SE_PET_CAT")`

## Tests

Two unit tests in `feature-purchase-pet/src/test/`:

1. `CreatePetSessionAndPriceIntentUseCaseTest` — mirrors apartment's `CreateSessionAndPriceIntentUseCaseTest`. Cases: happy path, member SSN missing.
2. `SubmitPetFormAndGetOffersUseCaseTest`. Cases:
   - Happy path returns `PetOffers` mapped from fragment.
   - `userError` from `PriceIntentDataUpdate` raises with backend message.
   - `userError` from `PriceIntentConfirm` raises with backend message.
   - Empty offers list raises generic `ErrorMessage`.
   - Mixed-breed selection submits `"breeds": []`.
   - Non-mixed breed submits `"breeds": [breedId]`.
   - Dog routes last question to `"isPreviousDogOwner"`; cat routes to `"hasOutsideAccess"`.

## Out of Scope (v1)

Listed to prevent scope drift:

- Multi-pet support (racoon's `addMultiple: true`). Single pet per session.
- Searchable breed dropdown. Plain scrollable list.
- Compose previews. Match car module (none).
- UI / integration tests. Match car module (none).
- Demo mode / `ProdOrDemoProvider`. No demo for purchase flows.
- Migrating existing hardcoded Swedish car-form strings to Lokalise (separate concern).

## Key Design Decisions

1. **Single module, both species** — dog/cat templates differ by one radio field and the breed query's animal arg. Two modules would be churn.
2. **Animal derived from `productName`** — no separate nav arg. Avoids the deep-link / discriminator hazard called out in `feedback_navigation_args.md`.
3. **Parallel session + breeds load** — breeds query is independent of session creation; running serially would double initial latency.
4. **Mixed-breed = empty breeds list** — matches racoon dog-side behavior; backend accepts both shapes for cats.
5. **Backend enforces age / start-date / UW rules** — client surfaces `userError.message` rather than duplicating validation logic.
6. **`Pet`-prefixed GraphQL operations** — same reason as car: Apollo generates per-operation classes; prefixed names avoid classpath conflicts across product modules.
