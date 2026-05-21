# In-app House (Villa) Purchase + "Home" Picker Dialog — Design Spec

## Goal

Add an in-app villa insurance (`SE_HOUSE`) purchase flow as a sibling form inside the existing `feature-purchase-house` module, and introduce a "Home" picker dialog so the generic `hemforsakring` / `home-insurance` cross-sell card lets the user choose between Hyresrätt (apartment-rent), Bostadsrätt (apartment-BRF), and Villa (house).

Builds on the vacation home PR; depends on `feat/in-app-vacation-home-purchase` for the `feature-purchase-house` module skeleton and the Apollo operations.

## Why now

On the Hedvig marketing site, hemförsäkring is a product family with 5 variants (Hyresrätt, Bostadsrätt, Studentförsäkring, Villaförsäkring, Fritidshusförsäkring). The Android app only surfaces one cross-sell entry point per family — either "Vacation home" or "Home". Today the generic "Home" URL silently routes to apartment-RENT, which is incorrect for users who want Villa. A picker dialog after tapping "Home" lets the user disambiguate, and we add the missing Villa form behind the Villa option.

## Module structure

### Modified module: `feature-purchase-house`

The module already exists (from the vacation home PR). House lives as a sibling to vacation home:

```
app/feature/feature-purchase-house/
└── src/main/kotlin/com/hedvig/android/feature/purchase/house/
    ├── data/
    │   ├── HousePurchaseModels.kt                  (existing — no changes)
    │   ├── CreateHouseSessionAndPriceIntentUseCase.kt  (existing — reused as-is)
    │   ├── SubmitVacationHomeFormAndGetOffersUseCase.kt (existing)
    │   └── SubmitHouseFormAndGetOffersUseCase.kt   (NEW — house-specific form data)
    ├── ui/
    │   ├── vacationhome/                            (existing)
    │   └── house/                                   (NEW)
    │       ├── HouseFormDestination.kt
    │       └── HouseFormViewModel.kt
    ├── navigation/
    │   ├── HousePurchaseDestination.kt              (existing — no changes)
    │   └── HousePurchaseNavGraph.kt                 (MODIFIED — Form destination branches on productName)
    └── di/HousePurchaseModule.kt                    (MODIFIED — register HouseFormViewModel + new use case)
```

**Apollo operations:** Reused. `HousePriceIntentDataUpdate` accepts `PricingFormData` (JSON scalar), so the same mutation works with different field keys per product. No new `.graphql` files.

**Why same module, separate form composable:** House and vacation home are different products with different field sets (only ~60% overlap). Two product-specific composables is cleaner than one branching composable. The module name was forward-chosen for this in the vacation home spec.

### Modified module: `feature-insurances`

`InsuranceDestination.kt` gets the picker-dialog state and Composable. The dialog opens when the user clicks a generic hemförsäkring cross-sell URL. Selection invokes one of the three nav callbacks already on `InsuranceGraph`.

### Modified module: `app`

Add a second navigation lambda that routes `productName = "SE_HOUSE"` into `HousePurchaseGraphDestination` (already accepts an arbitrary `productName`). The existing `onNavigateToHousePurchase` callback handles both vacation-home and house.

## Routing rules (after this PR)

```
fritidshusforsakring / vacation-home                    → vacation home form (existing)
hemforsakring/villaforsakring / home-insurance/house    → SE_HOUSE form (NEW shortcut)
hemforsakring/bostadsratt / home-insurance/homeowner    → SE_APARTMENT_BRF (existing shortcut)
hemforsakring/hyresratt / home-insurance/rental         → SE_APARTMENT_RENT (existing shortcut — URL slug was hyresratt before, also matches home-insurance/rental now)
hemforsakring / home-insurance (bare)                   → "Home" picker dialog (NEW; today silently routes to RENT)
car-insurance / bilforsakring                           → car form (existing)
pet-insurance / djurforsakring                          → pet form (existing)
```

URL matching order in `InsuranceGraph.kt`'s `onCrossSellClick` `when {}` block:

1. fritidshus (must come before any other to defend against future shared prefixes)
2. villaforsakring / home-insurance/house (must come before generic hemförsäkring)
3. bostadsratt / home-insurance/homeowner (must come before generic hemförsäkring)
4. hyresratt / home-insurance/rental (must come before generic hemförsäkring)
5. bare hemforsakring / home-insurance → open picker dialog
6. car
7. pet
8. else: `openUrl(url)`

## "Home" picker dialog

Uses `HedvigDialog` (modal, scrim, dismissible). Lives as a Composable inside `feature-insurances`, opened from `InsuranceDestination` when the cross-sell click matches the generic home URL.

**State:**
- `var showHomePicker by rememberSaveable { mutableStateOf(false) }` in `InsuranceDestination`.
- When `onCrossSellClick` resolves to "open picker" → set `showHomePicker = true`.
- Dialog dismisses on selection or outside-tap.

**UI:**
- Title: `"Vilken typ av hemförsäkring?"` (Lokalise TODO: "Which type of home insurance?")
- Three vertically stacked rows/cards (full-width tappable):
  - `"Hyresrätt"` → calls `onNavigateToApartmentPurchase("SE_APARTMENT_RENT")`
  - `"Bostadsrätt"` → calls `onNavigateToApartmentPurchase("SE_APARTMENT_BRF")`
  - `"Villa"` → calls `onNavigateToHousePurchase("SE_HOUSE")`
- Cancel button (or rely on outside-tap / system back).

Optional sub-titles per row are nice-to-have but not in v1.

## V1 SE_HOUSE form fields (single scrolling screen)

| # | Field | Compose component | Validation | Form-data key | Type sent |
|---|-------|-------------------|------------|---------------|-----------|
| 1 | Street | `HedvigTextField` | non-empty | `street` | string |
| 2 | Zip code | `HedvigTextField` (numeric, max 5) | exactly 5 digits | `zipCode` | string |
| 3 | Living space (m²) | `HedvigTextField` (numeric) | > 0 | `livingSpace` | int |
| 4 | Ancillary area / biarea (m²) | `HedvigTextField` (numeric) | ≥ 0 | `ancillaryArea` | int |
| 5 | Household size | `HedvigStepper` (0–5) | always valid | `numberCoInsured` | int |
| 6 | Year of construction | `HedvigTextField` (numeric) | 1700–current year | `yearOfConstruction` | int |
| 7 | Number of bathrooms | `HedvigStepper` (1–10, default 1) | always valid | `numberOfBathrooms` | int |
| 8 | Subleted | `RadioChoiceRow` (yes/no) | required | `isSubleted` | boolean |

**Auto-injected (not shown):**
- `ssn` — from `currentMember.ssn` (existing use case)
- `email` — from `currentMember.email` (existing use case)
- `extraBuildings: []` — deferred UI (same as vacation home)
- `currentInsurance: null` — deferred UI (cancellation flow)
- `useRegistrationAddress: null` — deferred UI (needs a member-address query we don't have)

Form layout, ViewModel/presenter, and `RadioChoiceRow`/`yesNoOptions` pattern mirror `VacationHomeFormDestination` from the vacation home PR.

## Data flow

```
1. User taps "Home" cross-sell (URL = "hemforsakring" or "home-insurance", no sub-path)
2. InsuranceDestination opens HomePickerDialog
3. User picks "Villa" → onNavigateToHousePurchase("SE_HOUSE")
4. Navigate to HousePurchaseGraphDestination(productName = "SE_HOUSE")
5. HousePurchaseNavGraph branches Form destination on productName:
   - "SE_VACATION_HOME" → VacationHomeFormDestination
   - "SE_HOUSE" → HouseFormDestination
6. HouseFormDestination loads:
   a. CreateHouseSessionAndPriceIntentUseCase("SE_HOUSE") — reuses existing use case
   b. User fills 8 fields
   c. SubmitHouseFormAndGetOffersUseCase — submits {ssn, email, street, zipCode,
      livingSpace, ancillaryArea, numberCoInsured, yearOfConstruction,
      numberOfBathrooms, isSubleted, extraBuildings: []}
7. SelectTier (purchase-common) — though SE_HOUSE has only one tier (per storefront),
   the SelectTier screen still works correctly for single-tier offers (no-op pick)
8. Summary → Signing → Success (purchase-common, existing)
```

## SubmitHouseFormAndGetOffersUseCase

New use case in `data/`. Mirrors `SubmitVacationHomeFormAndGetOffersUseCase` but:
- Different form-data keys per the V1 table above
- Returns `Either<ErrorMessage, HouseOffers>` using the existing `HouseOffers` / `HouseTierOffer` models

## HousePurchaseNavGraph branch

The `Form` destination handler conditionally renders based on `graphRoute.productName`:

```kotlin
navdestination<Form> { backStackEntry ->
  val graphRoute = navController.getRouteFromBackStack<HousePurchaseGraphDestination>(backStackEntry)
  when (graphRoute.productName) {
    "SE_VACATION_HOME" -> {
      val viewModel: VacationHomeFormViewModel = koinViewModel { parametersOf(graphRoute.productName) }
      VacationHomeFormDestination(...)
    }
    "SE_HOUSE" -> {
      val viewModel: HouseFormViewModel = koinViewModel { parametersOf(graphRoute.productName) }
      HouseFormDestination(...)
    }
    else -> error("Unknown productName for HousePurchaseGraph: ${graphRoute.productName}")
  }
}
```

Post-form destinations (`SelectTier`, `Summary`, `Signing`, `Success`) are unchanged — both products share them via `purchase-common`.

## DI changes

`HousePurchaseModule.kt` adds:

```kotlin
single<SubmitHouseFormAndGetOffersUseCase> { SubmitHouseFormAndGetOffersUseCaseImpl(apolloClient = get()) }

viewModel<HouseFormViewModel> { params ->
  HouseFormViewModel(
    productName = params.get(),
    createHouseSessionAndPriceIntentUseCase = get(),
    submitHouseFormAndGetOffersUseCase = get(),
  )
}
```

## App-level wiring

No new callbacks needed. `onNavigateToHousePurchase: (productName) -> Unit` already exists (from the vacation home PR) and accepts arbitrary product names.

`onNavigateToApartmentPurchase: (productName) -> Unit` already accepts both `SE_APARTMENT_BRF` and `SE_APARTMENT_RENT`.

So `app` module needs **no changes** other than the `feature-insurances` dependency picking up the picker dialog UI.

## Testing & verification

Same precedent as vacation home (no JVM unit tests for form layer):

- `./gradlew :feature-purchase-house:assemble`
- `./gradlew :app:assemble`
- `./gradlew ktlintCheck`
- Manual emulator verification per the `verifying-android-changes-in-emulator` skill:
  - Generic `hemforsakring` URL → picker opens
  - Pick Villa → house form opens
  - Pick Hyresrätt → apartment form opens (with RENT productName)
  - Pick Bostadsrätt → apartment form opens (with BRF productName)
  - Submit valid house form → tier select → summary → BankID → success
  - Specific `hemforsakring/villaforsakring` URL → house form opens directly (no picker)
  - Specific `hemforsakring/bostadsratt` URL → apartment-BRF directly (no picker)

Compose previews: `HouseFormDestination` previews for empty / filled / errors states, matching the vacation home pattern.

## Out of scope (separate follow-up PRs)

- `useRegistrationAddress` toggle + member-address prefill query
- `currentInsurance` dropdown + insurer query + cancellation wiring
- `extraBuildings` add/remove dialog UI (shared with vacation home)
- SE_APARTMENT_STUDENT support (5th hem variant; not in cross-sell paths today)
- Removing/cleaning up specific URL shortcuts (`bostadsratt`, `hyresratt`) — kept for deep-link compatibility
- House cross-sell card icon/imagery if separate from current "Home" card (out of mobile scope)

## Key design decisions

1. **Sibling form composable in the same module.** `feature-purchase-house` was named forward for this — vacation home and house are different products that share the same backend mutations and post-form flow.

2. **Picker dialog over a full picker screen.** User explicitly preferred a modal Dialog over a HedvigBottomSheet or a routed picker screen. Dialog UI lives in `feature-insurances` (close to where the cross-sell click originates), state is local `rememberSaveable`.

3. **Keep specific URL shortcuts.** Cross-sell URLs that already disambiguate the sub-type (`villaforsakring`, `bostadsratt`, `hyresratt`) bypass the picker — they're treated as direct deep-links. This is a low-risk addition since the routing change is order-sensitive but the new branches don't overlap with anything existing.

4. **`SubmitHouseFormAndGetOffersUseCase` is its own type, not a parameterization of the vacation-home use case.** Field sets differ enough that a single use case with optional parameters would have more conditionals than two parallel use cases.

5. **Reuse `CreateHouseSessionAndPriceIntentUseCase` as-is.** It takes `productName: String` and works for both `SE_VACATION_HOME` and `SE_HOUSE`. No new GraphQL ops.

6. **Single-tier behavior in SelectTier.** SE_HOUSE returns only the `SE_HOUSE` tier from storefront. The shared `SelectTierDestination` handles a one-item offer list naturally (one selectable card). No special-casing needed.

7. **All-English copy with Lokalise TODOs.** Same convention as the pet form and the post-refactor vacation home form.
