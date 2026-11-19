# Native Cross-Sell Purchase: Backend (storefront) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Every Swedish cross-sell product can be quoted and signed through storefront's in-app purchase API, gated per product by config.

**Architecture:** Builds on `InAppProductHandler` from `origin/feat/in-app-purchase`: one Spring `@Component` per product returns typed form fields and maps validated values to `PricingFormData`; `InAppPurchaseService` runs them through the existing shop-session/price-intent pipeline. This plan adds per-product gating, three field types, multi-value fields, eight handlers and product-page data.

**Tech Stack:** Kotlin, Spring Boot, graphql-java (schema-first `.graphqls`), Maven (`./mvnw`), JUnit 5, MockK, AssertJ, TestContainers (Docker must be running).

**Spec:** `docs/superpowers/specs/2026-11-18-native-cross-sell-purchase-design.md` (in the android repo)

**Repo:** `~/repos/storefront`. All paths below are relative to it. `P` = `src/main/kotlin/com/hedvig/storefront/inapppurchase`, `T` = `src/test/kotlin/com/hedvig/storefront/inapppurchase`.

## Global Constraints

- Sweden only. `InAppProduct` values map 1:1 to `ProductName`: `ACCIDENT→SE_ACCIDENT`, `PAYMENT_PROTECTION→SE_PAYMENT_PROTECTION`, `DOG→SE_PET_DOG`, `CAT→SE_PET_CAT`, `CAR→SE_CAR`, `APARTMENT_RENT→SE_APARTMENT_RENT`, `APARTMENT_BRF→SE_APARTMENT_BRF`, `APARTMENT_STUDENT→SE_APARTMENT_STUDENT`, `HOUSE→SE_HOUSE`, `VACATION_HOME→SE_VACATION_HOME`.
- Field ids are the `PricingFormData` property names (`street`, `zipCode`, `livingSpace`, …) plus `startDate`.
- Every handler's fields end with `InAppPurchaseStartDate.field(today)`.
- Yes/no questions are `SINGLE_SELECT` with options `"true"`/`"false"`, titles `I18n.get("IN_APP_PURCHASE_OPTION_YES")` / `..._NO`.
- All user-facing strings via `I18n.get("IN_APP_PURCHASE_…")` with new keys; field titles match the web's labels.
- Web shop behaviour must not change: never touch `ProductName.available()` or web price-intent resolvers.
- Run `./mvnw test -Dtest=<Class>` per task, `./mvnw test` before each commit that touches shared code.
- Commit messages end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.

## Review Focus

1. A member who already has a dog contract opens the dog form → eligible, form shown (not `ALREADY_INSURED`). Test in Task 5.
2. Breeds submitted with more values than `maxSelections`, or a breed not in the options → field error, not a 500. Test in Task 3.
3. An extra building with a non-numeric or zero area, or an unknown type → field error on `extraBuildings`. Test in Task 3.
4. A product not in `inAppPurchaseProducts` → `NOT_AVAILABLE` from the form *and* `inAppProduct = null` on its cross-sell *and* quote rejected. Test in Task 2.
5. Car quote where the registration number is not found by underwriting → `fieldErrors` on `registrationNumber`, not a `userError` the app can't place. Test in Task 6.

---

### Task 1: Combined branch on master

**Files:** none new; resolves conflicts in files touched by both branches (`CrossSellService.kt`, `gqltypes/CrossSell.kt`, `CrossSell.graphqls`, `StorefrontProperties.kt`).

- [ ] **Step 1:** `git fetch` then `git switch -c feat/in-app-purchase-all-products origin/master`.
- [ ] **Step 2:** `git merge origin/feat/cross-sell-options` then `git merge origin/feat/in-app-purchase`. Resolve conflicts keeping both features (cross-sell `options` and `inAppProduct` both on `CrossSell`).
- [ ] **Step 3:** Run `./mvnw test -Dtest='InAppPurchase*,CrossSell*,ProductFaq*'`. Expected: all PASS.
- [ ] **Step 4:** Run `./mvnw test`. Expected: BUILD SUCCESS.
- [ ] **Step 5:** Commit the merge.

### Task 2: Per-product gating

**Files:**
- Modify: `src/main/kotlin/com/hedvig/storefront/config/StorefrontProperties.kt` (replace `inAppPurchaseEnabled: Boolean` with `inAppPurchaseProducts: Set<InAppProduct> = emptySet()`)
- Modify: `src/main/resources/application*.yml` (staging/dev profiles: all ten products; production: empty)
- Modify: `$P/InAppPurchaseService.kt` (`isEnabled()` → `isEnabled(product: InAppProduct)`), `src/main/kotlin/com/hedvig/storefront/crosssell/CrossSellService.kt` (`inAppProduct()`), `$P/InAppPurchaseResolvers.kt` if it reads the flag
- Test: `$T/InAppPurchaseTest.kt`

**Interfaces:**
- Produces: `StorefrontProperties.featureFlags.inAppPurchaseProducts: Set<InAppProduct>`; `InAppPurchaseService.isEnabled(product: InAppProduct): Boolean`.

- [ ] **Step 1: Write failing tests** in `InAppPurchaseTest`:
  - `form is not available when the product is not enabled` — set `inAppPurchaseProducts = setOf(ACCIDENT)`, `queryForm("PAYMENT_PROTECTION").eligibility.reason == "NOT_AVAILABLE"`, fields empty.
  - `cross-sell has no inAppProduct when its product is not enabled` — same config, payment-protection cross-sell `inAppProduct == null`, accident cross-sell `== "ACCIDENT"`.
  - `quote is rejected when the product is not enabled` — quote PAYMENT_PROTECTION returns `userError` (or throws bad input, matching how the existing not-eligible quote path behaves).
- [ ] **Step 2:** `./mvnw test -Dtest=InAppPurchaseTest` → the three FAIL.
- [ ] **Step 3:** Implement. Existing tests that set `inAppPurchaseEnabled = true` set `inAppPurchaseProducts = InAppProduct.entries.toSet()` instead.
- [ ] **Step 4:** `./mvnw test -Dtest='InAppPurchaseTest,CrossSellTest'` → PASS.
- [ ] **Step 5:** Commit `feat: gate in-app purchase per product`.

### Task 3: Multi-value fields and TEXT, SEARCHABLE_SELECT, EXTRA_BUILDINGS

**Files:**
- Modify: `$P/InAppPurchaseTypes.kt`, `$P/InAppPurchaseFormValidator.kt`, `src/main/resources/graphql/InAppPurchase.graphqls`
- Test: `$T/InAppPurchaseFormValidatorTest.kt`

**Interfaces:**
- Produces (Kotlin and GraphQL):
  - `InAppPurchaseFormFieldType` gains `TEXT`, `SEARCHABLE_SELECT`, `EXTRA_BUILDINGS`.
  - `InAppPurchaseFormField` gains `maxSelections: Int? = null` (SEARCHABLE_SELECT only; null = 1). GraphQL: `maxSelections: Int`.
  - EXTRA_BUILDINGS: `options` = building types; each submitted value is JSON `{"type":"GARAGE","area":25,"hasWaterConnected":false}`.
  - `InAppPurchaseFieldValues(values: Map<String, List<String>>)` with `string(id): String`, `int(id): Int`, `date(id): LocalDate`, `boolean(id): Boolean` (`"true"`/`"false"`), `strings(id): List<String>`, `extraBuildings(id): List<InAppExtraBuilding>`.
  - `data class InAppExtraBuilding(val type: String, val area: Int, val hasWaterConnected: Boolean)`.
- Validation rules: every field required except EXTRA_BUILDINGS (empty list valid). Single value for NUMBER, DATE, SINGLE_SELECT, TEXT (more values = bad input, as today). SEARCHABLE_SELECT: 1..(maxSelections ?: 1) values, each an option value. EXTRA_BUILDINGS: each value parses, `type` in options, `area` > 0.

- [ ] **Step 1: Write failing tests** in `InAppPurchaseFormValidatorTest`:
  - `text field accepts any non-blank value` / `text field reports a missing value`
  - `searchable select accepts up to maxSelections options` (maxSelections 2, values `["LABRADOR","POODLE"]` → Valid, `strings()` returns both)
  - `searchable select reports more values than maxSelections` (3 values → Invalid with that field's id)
  - `searchable select reports a value that is not an option`
  - `extra buildings accepts no buildings`
  - `extra buildings parses each building` (`extraBuildings()` == `[InAppExtraBuilding("GARAGE", 25, false)]`)
  - `extra buildings reports an unknown type, a non-positive area and malformed json` (three cases, each Invalid on `extraBuildings`)
  - `boolean reads true and false`
- [ ] **Step 2:** `./mvnw test -Dtest=InAppPurchaseFormValidatorTest` → new tests FAIL.
- [ ] **Step 3:** Implement. Existing callers of `InAppPurchaseFieldValues` keep working (single-value getters read `values[id]?.single()`).
- [ ] **Step 4:** `./mvnw test -Dtest='InAppPurchaseFormValidatorTest,InAppPurchaseTest'` → PASS.
- [ ] **Step 5:** Commit `feat: text, searchable select and extra buildings form fields`.

### Task 4: Product-page data carries inAppProduct and FAQ

**Files:**
- Modify: the `crossSellProductInfo` resolver and type added by `feat/cross-sell-options` (find with `git grep -n crossSellProductInfo`), its `.graphqls`
- Test: the existing `crossSellProductInfo` test class from that branch

**Interfaces:**
- Produces GraphQL on `CrossSellProductInfo`: `inAppProduct: InAppProduct` (same rule as `CrossSell.inAppProduct`, gated by Task 2) and `faq: [ProductFaqItem!]!` (reuse the FAQ type and extractor `ProductFaq.kt` from `feat/in-app-purchase`; same Storyblok source as `inAppPurchaseForm.product.faq`).

- [ ] **Step 1: Failing tests:** `product info has the in-app product when it is enabled`, `product info has no in-app product when it is not enabled`, `product info has the product's faq`.
- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement. **Step 4:** Run → PASS.
- [ ] **Step 5:** Commit `feat: in-app product and faq on cross-sell product info`.

### Task 5: DOG and CAT handlers, and buying more than one

**Files:** Create `$P/PetInAppHandlers.kt` (`DogInAppHandler`, `CatInAppHandler`); modify `$P/InAppProductHandler.kt`, `$P/InAppPurchaseService.kt`, `InAppProduct` enum + `InAppPurchase.graphqls`. Test: create `$T/PetInAppPurchaseTest.kt`.

**Interfaces:**
- Produces: `InAppProductHandler.allowsMultiple: Boolean` (default `false`). When `true`, `eligibility()` skips the `existingContractFor` check. `true` for DOG, CAT, CAR, VACATION_HOME.

**Fields (in order):** `name` TEXT; `birthDate` DATE (max today, min today − 30 years, no default); `breeds` SEARCHABLE_SELECT, options from `ShopService.getSwedishDogBreeds()` / `getSwedishCatBreeds()` (value and title = `breed`, sorted by `visibilityWeight` desc), `maxSelections` copied from the web's breed field (racoon `apps/store/src/features/priceCalculator` breed field; mixed breeds); `gender` SINGLE_SELECT `MALE`/`FEMALE`; `isNeutered` yes/no; DOG: `isPreviousDogOwner` yes/no; CAT: `hasOutsideAccess` yes/no; start date. `allowsMultiple = true`.

`toPricingFormData`: `PricingFormData(name, birthDate, breeds, gender, isNeutered, isPreviousDogOwner | hasOutsideAccess)`.

- [ ] **Step 1: Failing tests:** `dog form lists the fields in order` (ids exactly as above), `dog breeds come from underwriter` (mock `underwriter.getSwedishDogBreeds()`), `dog quote sends the pet data to the price intent` (quote with valid values → offers; verify the price intent data), same three for cat; `member with a dog contract can buy another dog` (`memberHasContracts(TestData.contract(insuranceType = <dog insurance type>))` → `queryForm("DOG").eligibility.isEligible`). Existing `ALREADY_INSURED` accident test keeps passing.
- [ ] **Step 2–4:** Run `./mvnw test -Dtest=PetInAppPurchaseTest` → FAIL, implement, → PASS.
- [ ] **Step 5:** Commit `feat: in-app dog and cat purchase`.

### Task 6: CAR handler

**Files:** Create `$P/CarInAppHandler.kt`; enum + schema. Test: create `$T/CarInAppPurchaseTest.kt`.

**Fields:** `registrationNumber` TEXT; `mileage` SINGLE_SELECT with the web's car-mileage options (values and labels copied from racoon's `car-mileage` field component); start date. `allowsMultiple = true`. Normalise the registration number (uppercase, strip spaces) in `toPricingFormData`.

- [ ] **Step 1: Failing tests:** `car form lists registration number, mileage and start date`, `car quote sends the normalised registration number` (`"abc 123"` → `"ABC123"`), `car quote reports an unknown registration number on its field` (make the confirm fail the way underwriting does for an unknown car; assert `fieldErrors[0].fieldId == "registrationNumber"`). The last test pins Review Focus 5: map that failure in `InAppPurchaseService.quote` to a field error when the handler declares the field (`InAppProductHandler.fieldForQuoteFailure(message): String?`, default null; CAR returns `registrationNumber`).
- [ ] **Step 2–4:** Run → FAIL, implement, → PASS.
- [ ] **Step 5:** Commit `feat: in-app car purchase`.

### Task 7: Apartment handlers

**Files:** Create `$P/ApartmentInAppHandlers.kt` (one class per subtype, sharing a private field-list function); enum + schema. Test: create `$T/ApartmentInAppPurchaseTest.kt`.

**Fields:** `street` TEXT, `zipCode` TEXT, `livingSpace` NUMBER (min 1), `numberCoInsured` NUMBER (min 0, default from the member's home contract if any, else 0), start date. `toPricingFormData` sends exactly the keys racoon's `SE_APARTMENT_RENT` / `SE_APARTMENT_BRF` / `SE_APARTMENT_STUDENT` templates send (check them for `subType`/`isStudent`).

- [ ] **Step 1: Failing tests:** `apartment form lists address, size, household and start date` (one per subtype, parameterised), `rental apartment quote sends the apartment data`, `student apartment quote sends the student flag the web sends`.
- [ ] **Step 2–4:** Run → FAIL, implement, → PASS.
- [ ] **Step 5:** Commit `feat: in-app apartment purchase`.

### Task 8: HOUSE and VACATION_HOME handlers

**Files:** Create `$P/HouseInAppHandlers.kt`; enum + schema. Test: create `$T/HouseInAppPurchaseTest.kt`.

**Fields:**
- HOUSE: `street`, `zipCode` TEXT; `livingSpace` NUMBER (min 1); `ancillaryArea` NUMBER (min 0, default 0); `yearOfConstruction` NUMBER (min 1700, max current year); `numberOfBathrooms` NUMBER (min 0, max 10, default 1); `numberCoInsured` NUMBER (default from home contract, else 0); `isSubleted` yes/no; `extraBuildings` EXTRA_BUILDINGS; start date.
- VACATION_HOME: `street`, `zipCode`; `livingSpace`; `yearOfConstruction`; `numberOfBathrooms`; `hasWaterConnected` yes/no; `multipleOwners` yes/no; `isSubleted` yes/no; `extraBuildings`; start date. `allowsMultiple = true`.
- Building type options: the same list the web's extra-buildings field offers (find where storefront serves it to the web price intent, else copy racoon's options); titles via I18n.

- [ ] **Step 1: Failing tests:** `house form lists the fields in order`, `house quote sends extra buildings to the price intent` (one garage 25 m² → `PricingFormData.extraBuildings` holds it in the web's JSON shape), `house quote with no extra buildings sends an empty list`, same order/quote tests for vacation home.
- [ ] **Step 2–4:** Run → FAIL, implement, → PASS.
- [ ] **Step 5:** Commit `feat: in-app house and vacation home purchase`.

### Task 9: Staging

- [ ] **Step 1:** `./mvnw test` → BUILD SUCCESS.
- [ ] **Step 2:** `git push -u origin feat/in-app-purchase-all-products`.
- [ ] **Step 3:** **Ask Hugo** how this branch is deployed to staging (storefront's deploy path is not documented in the repo) and do it once he confirms.
- [ ] **Step 4:** Against staging GraphQL, run `inAppPurchaseForm` for each of the ten products as the demo member and confirm each returns fields. Expected: ten non-empty field lists.
