# Native Cross-Sell Purchase: Android App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A member taps a cross-sell, sees its native product page, and buys any in-scope product in the app through one server-driven form, offers, summary and BankID flow.

**Architecture:** Combines `feature-product-in-app` (PR #3362, the product page) with `feature-in-app-purchase` (PR #3305, the purchase flow). The product page's button opens `InAppPurchaseKey(inAppProduct)` when the backend sets `inAppProduct`, else the web store URL. The form screen renders backend fields by type; this plan adds multi-value state and three renderers (TEXT, SEARCHABLE_SELECT, EXTRA_BUILDINGS).

**Tech Stack:** Kotlin, Jetpack Compose, Molecule presenters, Metro DI, Navigation 3, Apollo 4, assertk + Turbine + `molecule-test`.

**Spec:** `docs/superpowers/specs/2026-11-18-native-cross-sell-purchase-design.md`

**Depends on:** the backend plan `docs/superpowers/plans/2026-11-18-native-cross-sell-purchase-backend.md`. Tasks 1, 3–6 can run before the backend is on staging; Task 2 needs the schema from backend Tasks 3–8 (download it once deployed, or copy it by hand from the storefront branch); Task 8 needs all of it.

Paths: `IAP` = `app/feature/feature-in-app-purchase/src/main/kotlin/com/hedvig/android/feature/inapppurchase`, `IAPT` = `app/feature/feature-in-app-purchase/src/test/kotlin/com/hedvig/android/feature/inapppurchase`, `PP` = `app/feature/feature-product-in-app/src/commonMain/kotlin/com/hedvig/android/feature/product/inapp`, `PPT` = `app/feature/feature-product-in-app/src/jvmTest/kotlin/com/hedvig/android/feature/product/inapp`.

## Global Constraints

- Follow the repo `CLAUDE.md`: Metro (no Koin), Navigation 3 keys, `logcat` only, no Apollo types in public signatures, `popBackstack()` for in-content buttons (never `navigateUp`), no history/rejected-alternative comments, `Res.string.X` not imported `string`.
- No product-specific branches in the app: renderers switch on field type only. The only per-product code is the `InAppProduct` enum ↔ octopus mapping.
- New UI copy: hardcoded English + `// TODO: Add "<English>" / "<Swedish>" to Lokalise`.
- Field titles, options, defaults and error messages are rendered as the backend sends them.
- A field of an unknown type is dropped and logged at ERROR (existing behaviour in `GetInAppPurchaseFormUseCase`).
- Run `./gradlew :feature-in-app-purchase:test :feature-product-in-app:jvmTest` per task; `./gradlew ktlintFormat` before every commit.
- Check UI with the `render-previews` skill on each new renderer's `@Preview`.
- Commit messages end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.

## Review Focus

1. Member picks two breeds, removes one, submits → exactly one breed sent. Test in Task 4.
2. Backend returns a field error for a field id the form shows *and* one for an id it doesn't → first shown under its field, second in the error dialog (existing logic must survive the multi-value change). Test in Task 3.
3. Member adds an extra building, then deletes it, then submits → `extraBuildings` sent with zero values, not omitted. Test in Task 5.
4. Process death on the form with a multi-select chosen → value restored from `lastState`. Test in Task 3.
5. Cross-sell with `inAppProduct = null` (product not enabled) → product page button opens the web store URL, never the native flow. Test in Task 6.

---

### Task 1: One branch with both features

**Files:** merges `origin/feat/product-pages` and `origin/feat/native-purchase-flow` onto `origin/develop`; deletes `IAP/ui/productinfo/` and `InAppPurchaseProductInfoKey`; reverts #3305's edits to `.github/workflows/staging.yml` and `app/app/build.gradle.kts` staging bits; deletes `IAP/data/InAppPurchaseMocks.kt`.

- [ ] **Step 1:** `git fetch`, then `git switch -c feat/native-cross-sell-purchase origin/develop`.
- [ ] **Step 2:** `git merge origin/feat/product-pages`, resolve, `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL.
- [ ] **Step 3:** `git merge origin/feat/native-purchase-flow`, resolve. Where both branches changed the same cross-sell entry points (Home, Insurances, cross-sell sheet, `CrossSells.kt`), keep #3362's routing (`navigateToCrossSellProduct`) and drop #3305's direct `InAppPurchaseKey` routing — the product page is the only way into the flow (spec: "Gating and rollout").
- [ ] **Step 4:** Remove the product-info screen, its key and the mocks; drop the staging workflow change. Keep `InAppPurchaseKey(product: InAppProduct)` in `feature-in-app-purchase-navigation` as the flow's entry.
- [ ] **Step 5:** Verify: `./gradlew :app:assembleDebug :feature-in-app-purchase:test :feature-product-in-app:jvmTest :app:testDebugUnitTest --tests '*ExhaustiveBackStackSerializationTest*'` → BUILD SUCCESSFUL.
- [ ] **Step 6:** `./gradlew ktlintFormat`, commit `Combine product pages with the in-app purchase flow`.

### Task 2: Schema and product mapping

**Files:**
- Modify: `app/apollo/apollo-octopus-public/src/commonMain/graphql/com/hedvig/android/apollo/octopus/schema.graphqls` (run `./gradlew downloadOctopusApolloSchemaFromIntrospection` against dev once the backend branch is deployed; else hand-copy the storefront branch's `InAppPurchase.graphqls` and `CrossSellProductInfo` changes)
- Modify: `app/data/data-contract/src/commonMain/kotlin/com/hedvig/android/data/contract/InAppProduct.kt` (add `DOG, CAT, CAR, APARTMENT_RENT, APARTMENT_BRF, APARTMENT_STUDENT, HOUSE, VACATION_HOME`)
- Modify: `IAP/data/GetInAppPurchaseFormUseCase.kt` (`toOctopus()`), and the reverse mapping wherever `CrossSell.inAppProduct` is mapped
- Modify: `app/feature/feature-product-in-app/src/commonMain/graphql/CrossSellProductInfoQuery.graphql` (+ `inAppProduct`, `faq { question answer }`), `PP/data/CrossSellProductInfo.kt`, `PP/data/GetCrossSellProductInfoUseCase.kt`
- Test: `PPT/data/GetCrossSellProductInfoUseCaseTest.kt` (create if missing, Apollo test-network style used elsewhere in the repo)

**Interfaces:**
- Produces: `CrossSellProductInfo.inAppProduct: InAppProduct?`, `CrossSellProductInfo.faq: List<CrossSellProductInfo.FaqItem>` with `data class FaqItem(val question: String, val answerMarkdown: String)`.
- An octopus `InAppProduct.UNKNOWN__` maps to `null` (app older than the backend → web).

- [ ] **Step 1: Failing tests:** `product info maps the in-app product`, `product info maps an unknown in-app product to null`, `product info maps the faq`.
- [ ] **Step 2–4:** Run → FAIL, implement, → PASS.
- [ ] **Step 5:** ktlintFormat, commit `Map every in-app product and the product page's faq`.

### Task 3: Multi-value form state

**Files:**
- Modify: `IAP/data/InAppPurchase.kt` (field model), `IAP/data/GetInAppPurchaseFormUseCase.kt` (mapping), `IAP/ui/form/InAppPurchaseFormViewModel.kt` (presenter state + events)
- Test: create `IAPT/ui/form/InAppPurchaseFormPresenterTest.kt` (style: `CrossSellProductPresenterTest`, `presenter.test(initialState) { … }` with fake use cases)

**Interfaces:**
- Produces:
  - `InAppPurchaseFormField` gains `Text(id, title, defaultValue: String?)`, `SearchableSelect(id, title, options: List<SingleSelect.Option>, defaultValues: List<String>, maxSelections: Int)`, `ExtraBuildings(id, title, buildingTypes: List<SingleSelect.Option>)`.
  - `InAppPurchaseFormField.defaultValues(): List<String>` replaces `defaultValueOrNull()`.
  - Presenter `values: Map<String, List<String>>`; `FormFieldUiState(field, values: List<String>, errorMessage)` with `val value: String? = values.singleOrNull()` kept for single-value renderers.
  - Event `UpdateField(fieldId: String, values: List<String>)` replaces the single-value one; single-value renderers send `listOfNotNull(value)`.
  - Every form field is sent on submit, including empty lists.

- [ ] **Step 1: Failing tests:**
  - `loaded form starts with each field's default values`
  - `updating a field replaces its values and clears its error`
  - `submit sends every field with its values, including empty ones` (fake quote use case records `List<InAppPurchaseFieldInput>`)
  - `field errors for shown fields go under the field and others to the error dialog` (Review Focus 2)
  - `a restarted presenter keeps the member's multi-value input` (start from a `Content` lastState with a two-value field; Review Focus 4)
- [ ] **Step 2–4:** `./gradlew :feature-in-app-purchase:test --tests '*InAppPurchaseFormPresenterTest*'` → FAIL, implement, → PASS. Existing renderers compile against the new event.
- [ ] **Step 5:** ktlintFormat, commit `Let in-app purchase fields hold several values`.

### Task 4: TEXT and SEARCHABLE_SELECT renderers

**Files:**
- Modify: `IAP/ui/form/FormFieldInputs.kt` (dispatch), create `IAP/ui/form/TextFieldInput.kt`, `IAP/ui/form/SearchableSelectFieldInput.kt`
- Port from: `origin/feat/in-app-pet-purchase` → `app/feature/feature-purchase-pet/.../ui/form/PetFormDestination.kt` `BreedDropdown` (UI only; no pet names, no breed use case — options come from the field)
- Test: `IAPT/ui/form/SearchableSelectStateTest.kt`

**Interfaces:**
- Consumes: Task 3's field types and `UpdateField(fieldId, values)`.
- Produces: `@Composable internal fun TextFieldInput(state: FormFieldUiState, onValueChange: (String) -> Unit)`; `@Composable internal fun SearchableSelectFieldInput(state: FormFieldUiState, onValuesChange: (List<String>) -> Unit)`; pure helpers in `SearchableSelectFieldInput.kt`: `internal fun filterOptions(options: List<Option>, query: String): List<Option>` (case-insensitive substring on title), `internal fun toggle(selected: List<String>, value: String, maxSelections: Int): List<String>` (removes if present; adds if under max; when `maxSelections == 1` replaces).

- [ ] **Step 1: Failing tests:** `filter matches titles case-insensitively`, `toggle adds a value under the limit`, `toggle ignores a new value at the limit`, `toggle removes a selected value` (Review Focus 1), `toggle replaces the value when one selection is allowed`.
- [ ] **Step 2–4:** Run → FAIL, implement helpers and composables, → PASS.
- [ ] **Step 5:** Add `@Preview`s (text empty/filled/error; searchable select closed, open with query, two selected), render with `render-previews`, read the PNGs.
- [ ] **Step 6:** ktlintFormat, commit `Render text and searchable select purchase fields`.

### Task 5: EXTRA_BUILDINGS renderer

**Files:**
- Create: `IAP/ui/form/ExtraBuildingsFieldInput.kt`
- Port from: `origin/feat/extra-buildings-in-house-purchase` → `app/feature/feature-purchase-house/.../ui/extrabuildings/ExtraBuildingsSection.kt` (UI only)
- Test: `IAPT/ui/form/ExtraBuildingValueTest.kt`

**Interfaces:**
- Produces: `internal data class ExtraBuildingValue(val type: String, val area: Int, val hasWaterConnected: Boolean)` with `fun encode(): String` → `{"type":"GARAGE","area":25,"hasWaterConnected":false}` (kotlinx.serialization) and `companion fun decode(value: String): ExtraBuildingValue?`; `@Composable internal fun ExtraBuildingsFieldInput(state: FormFieldUiState, onValuesChange: (List<String>) -> Unit)` — list of added buildings with delete, "Add building" opening a dialog (type from `buildingTypes`, area number, water yes/no).

- [ ] **Step 1: Failing tests:** `encode writes the backend's json shape`, `decode reads what encode wrote`, `decode returns null for malformed json`; plus in `InAppPurchaseFormPresenterTest`: `removing the last extra building submits an empty list` (Review Focus 3).
- [ ] **Step 2–4:** Run → FAIL, implement, → PASS.
- [ ] **Step 5:** `@Preview`s (none added, two added, add dialog), render with `render-previews`, read the PNGs.
- [ ] **Step 6:** ktlintFormat, commit `Render the extra buildings purchase field`.

### Task 6: Product page leads into the flow

**Files:**
- Modify: `PP/ui/CrossSellProductDestination.kt` (FAQ section below the carousel; button), `PP/ui/CrossSellProductViewModel.kt` (button event), `app/feature/feature-product-in-app/src/androidMain/kotlin/com/hedvig/android/feature/product/inapp/navigation/ProductInAppEntries.kt`, `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigEntryProvider.kt`
- Modify: `IAP/ui/success/InAppPurchaseSuccessScreen.kt` / its entry (close)
- Test: `PPT/ui/CrossSellProductPresenterTest.kt`, `PPT/navigation/CrossSellProductNavigationTest.kt`

**Interfaces:**
- Consumes: `CrossSellProductInfo.inAppProduct`, `.faq` (Task 2); `InAppPurchaseKey(product)` (Task 1).
- Produces: `CrossSellProductEvent.Buy`; presenter emits a one-shot navigation via an injected `Backstack` (as `InAppPurchaseFormPresenter` does) or the destination calls `startPurchase: (InAppProduct) -> Unit` / `openStoreUrl` — pick the presenter route so it is testable. Rule: `inAppProduct != null` → `backstack.add(InAppPurchaseKey(inAppProduct))`, else `openCrossSellUrl(storeUrl)`.
- `feature-product-in-app` may depend on `feature-in-app-purchase-navigation` (cross-feature navigation modules are allowed).
- Success "Done" pops everything from `InAppPurchaseKey` up to and including the product page (`popUpTo<CrossSellProductKey>(inclusive = true)`, arranged so system Back from success behaves the same), landing where the cross-sell was tapped.

- [ ] **Step 1: Failing tests:** `buy opens the purchase flow when the product can be bought in the app`, `buy opens the web store when the product cannot be bought in the app` (Review Focus 5), `content shows the faq`; navigation test: `finishing a purchase returns to where the cross-sell was tapped` (back stack `[Home, CrossSellProductKey, InAppPurchaseKey, …, SuccessKey]` → `[Home]`).
- [ ] **Step 2–4:** Run → FAIL, implement, → PASS.
- [ ] **Step 5:** Preview product page with FAQ, render, read.
- [ ] **Step 6:** ktlintFormat, commit `Buy natively from the product page`.

### Task 7: Analytics on purchase

**Files:** wherever `cross_sell_clicked` is tracked on the product page button (find with `git grep -n cross_sell_clicked`), `IAP` summary/sign presenter.

- [ ] **Step 1:** Confirm the product page button still fires `cross_sell_clicked` for both native and web paths; add a failing test if the existing tracking class has tests.
- [ ] **Step 2:** If the tracking layer has an event helper that takes a name and params, add `cross_sell_purchased` (params: `product`) on `SIGNED`; test that it fires once. If it needs new plumbing, skip this step and note it in the PR (spec marks it not demo-critical).
- [ ] **Step 3:** ktlintFormat, commit.

### Task 8: Demo pass on staging

- [ ] **Step 1:** Backend plan Task 9 done; `./gradlew downloadOctopusApolloSchemaFromIntrospection` shows no diff from Task 2's schema (or update and rebuild).
- [ ] **Step 2:** `./gradlew :app:installDevelop` (the `develop` build type, `com.hedvig.dev.app`) on an emulator, log in as the pet-only staging demo member; turn on `SHOW_PRODUCT_PAGES` for them in Unleash (**ask Hugo** if you lack access).
- [ ] **Step 3:** For each product in build order (accident, trygghetsförsäkring, dog, cat, car, apartment ×3, villa, fritidshus): open its cross-sell → product page → form → offers → summary → BankID test sign → success → back where it started. Use the `inspect-network-traffic` skill when a step fails.
- [ ] **Step 4:** As the apartment demo member: accident form shows the prefilled household; no home cross-sells offered.
- [ ] **Step 5:** Report per product: works / fails (with the failing request).
