# Bottom Sheets & Dialogs as Navigation-3 Scenes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add idiomatic Navigation-3 overlay scene support (bottom sheets + dialogs as real back-stack entries) and migrate the sheets that are genuinely "destination-like", while deliberately leaving inline the sheets that should stay inline.

**Architecture:** Today every `HedvigBottomSheet` is rendered *inline* inside a destination composable and driven by a `HedvigBottomSheetState` (`show`/`dismiss`) toggle — invisible to the back stack, not process-death safe, with its own ad-hoc back handling. Nav3 1.2.0-alpha03 ships an `OverlayScene`/`SceneStrategy` mechanism (and a built-in `DialogSceneStrategy`) that renders an entry *as an overlay on top of the entry beneath it*. We add a generic `BottomSheetSceneStrategy` (modeled on the framework's `AnimatedBottomSheetSample` + our existing `NavSuiteSceneDecoratorStrategy` metadata-opt-in pattern), wire it plus the built-in `DialogSceneStrategy` into the single `NavDisplay` in `HedvigApp`, then migrate sheets bucket-by-bucket. The cross-feature lambda-threading model is **kept as-is** (see the companion discussion / `HedvigEntryProvider`): a sheet that becomes a key is pushed via `backstack.add(...)` from the entry, and any "what to do on selection" stays a lambda threaded down from the entry.

**Tech Stack:** Kotlin, Jetpack Compose, `androidx.navigation3:navigation3-ui:1.2.0-alpha03`, Material 3 `ModalBottomSheet`, Metro DI, kotlinx.serialization (polymorphic back-stack persistence).

## Global Constraints

- New `HedvigNavKey`s are `@Serializable`; any module declaring a key **must** have `navKeys()` in its `hedvig { }` block or the app crashes on process-death restore. (Verbatim project rule.)
- Keys internal to a feature stay `internal`; only cross-feature-reachable keys move to a `feature-{name}-navigation` module.
- A key carries only `@Serializable` args — **no callbacks**. Anything the sheet must "do" on the opener's behalf is threaded as a lambda from the entry, never stored on the key.
- Never edit `strings.xml`; hardcode English + `// TODO: Add "<en>" / "<sv>" to Lokalise` if new copy is needed (none is needed in this plan).
- Run `./gradlew ktlintFormat` before every commit. Max line length 120, 2-space indent, trailing commas on.
- ViewModels (if any) resolve via `metroViewModel()` / `assistedMetroViewModel(...)`, never `viewModel()`.
- Overlay scene strategies must be ordered **before** non-overlay strategies, and `SinglePaneSceneStrategy()` must be the last fallback in the `sceneStrategies` list.

---

## Background: the full sheet classification (32 sites)

Drives which sites migrate and which stay inline. Buckets:
- **B1 read-only / informational** — only displays content + a close button. No value back, no navigation.
- **B2 navigation-triggering** — primary action dismisses then navigates (in-app key, OS picker, or flow-advance). The "result" is an action, not a value handed back.
- **B3 value-returning** — returns a chosen value that mutates the opener's own state (emits an event to the opener's VM, sets a form field).

| # | Site (file:line — name) | Data | Bucket | Action | Disposition |
|---|---|---|---|---|---|
| 1 | CustomizeAddonDestination.kt:572 (alreadyActiveAddon) | Unit | B1 | info + close | **Migrate** |
| 2 | ChatLoadedScreen.kt:781 (banner) | DisplayInfo | B1 | info + close | **Migrate** |
| 3 | InboxDestination.kt:133 (newChatSelect) | Unit | B2 | dismiss → new conversation (one branch opens another sheet) | **Migrate** |
| 4 | InboxDestination.kt:134 (startClaim) | Unit | B2 | dismiss → claim chat | **Migrate** |
| 5 | ClaimOutcomeNewClaimDestination.android.kt:44 | Unit | B2 | OS notification-permission / settings | **Stay inline** (OS/permission launcher) |
| 6 | FormStep.kt:440 (search) | String? | B3 | `onOptionSelected` mutates form | **Stay inline** |
| 7 | UploadFilesStep.kt:180 (fileTypeSelect) | Unit | B2 | OS photo/file/camera pickers | **Stay inline** (OS launcher) |
| 8 | AudioRecordingStepSections.kt:252 | Unit | B3 | record/submit/redo drive opener state | **Stay inline** |
| 9 | AddFilesDestination.kt:120 (fileTypeSelect) | Unit | B2 | OS pickers / camera | **Stay inline** (OS launcher) |
| 10 | ClaimDetailsDestination.kt:275 (fileTypeSelect) | Unit | B2 | OS pickers / camera | **Stay inline** (OS launcher) |
| 11 | ClaimDetailsDestination.kt:335 (explanation) | Unit | B1 | info + close | **Migrate** |
| 12 | CrossSellSheet.kt:31 | CrossSellSheetData | B2 | `onCrossSellClick(url)` navigates | **Migrate (special, see note)** |
| 13 | EditCoInsuredAddMissingInfoDestination.kt:139 | AddBottomSheetContentState | B3 | SSN/name form → opener VM | **Stay inline** |
| 14 | EditCoInsuredAddOrRemoveDestination.kt:181 (add) | AddBottomSheetContentState | B3 | add-coinsured form → opener VM | **Stay inline** |
| 15 | EditCoInsuredAddOrRemoveDestination.kt:210 (remove) | RemoveBottomSheetContentState | B3 | `onRemoveCoInsured` → opener VM | **Stay inline** |
| 16 | EditCoInsuredAddOrRemoveDestination.kt:233 (costBreakdown) | PriceInfoForBottomSheet | B1 | price display only | **Migrate (shared price key)** |
| 17 | HomeDestination.kt:236 (crossSell) | CrossSellSheetData | B2 | `onCrossSellClick` navigates | **Migrate (special, see note)** |
| 18 | HomeDestination.kt:243 (startClaim) | Unit | B2 | dismiss → claim chat | **Migrate** |
| 19 | InsuranceEvidenceEmailInputDestination.kt:134 (explanation) | Unit | B1 | info + close | **Migrate** |
| 20 | ContractDetailDestination.kt:163 (costBreakdown) | PriceInfoForBottomSheet | B1 | price display only | **Migrate (shared price key)** |
| 21 | CoverageTab.kt:63 | InsurableLimit | B1 | limit info + close | **Migrate** |
| 22 | UpcomingChangesBottomSheetContent.kt:56 | PriceInfoForBottomSheet | B1 | nested price display | **Migrate (shared price key)** |
| 23 | YourInfoTab.kt:167 (editYourInfo) | Unit | B2 | each action dismiss → navigates | **Migrate** |
| 24 | YourInfoTab.kt:201 (upcomingChanges) | Unit | B1 | info (nested price) | **Migrate** |
| 25 | YourInfoTab.kt:428 (removeAddon) | ContractAddon | B2 | dismiss → removeAddon/upgradeAddon | **Migrate** |
| 26 | MarketingDestination.kt:91 (preferences) | Unit | B3 | `selectLanguage` → opener VM (+ dev settings) | **Stay inline** |
| 27 | PaymentDetailsDestination.kt:129 (foreverInfo) | Unit | B1 | info + close | **Migrate** |
| 28 | PaymentDetailsDestination.kt:131 (explanation) | String | B1 | info + close | **Migrate (Phase-1 exemplar)** |
| 29 | DiscountsDestination.kt:143 (foreverInfo) | Unit | B1 | info + close | **Migrate** |
| 30 | MemberPaymentDetailsDestination.kt:126 (explanation) | PaymentExplanationData | B1 | info + close | **Migrate** |
| 31 | TerminationConfirmationDestination.kt:128 (areYouSure) | Unit | B2 | confirm → submit/advance flow | **Stay inline** (trivial confirm) |
| 32 | TerminationScaffold.kt:60 (explanation) | String | B1 | info + close | **Migrate** |

**Counts:** B1 = 14, B2 = 12, B3 = 6. **Migrate = 18, Stay inline = 14.**

**Disposition rationale:**
- **Stay inline — B3 (6):** value-returning to the immediate parent; an inline closure is strictly cleaner than a serializable key + a result bus. Migrating these would *force* building result-passing infra for negative benefit.
- **Stay inline — OS launchers (5, 7, 9, 10):** these drive `rememberLauncherForActivityResult` / permission requests bound to the composition; they are not in-app destinations.
- **Stay inline — trivial confirm (31):** a one-shot "are you sure → submit" with no content worth a key.
- **Migrate special (12, 17 — CrossSell):** there is already a `feature-cross-sell-sheet` module and a `CrossSellEligibleDestination` marker; the cross-sell sheet is a cross-cutting concern with its own appearance rules. **Do not fold it into the generic bottom-sheet scene in this plan** — leave it as the last item and decide separately. Listed as Migrate only to flag it; it is **out of scope for Phases 0–1**.

This plan implements **Phase 0** (infra) + **Phase 1** (one fully-worked exemplar, site #28) and leaves the remaining 16 migrations as a mechanical rollout (Task 5 table) that repeats the exemplar recipe.

## File Structure

- **Create** `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/BottomSheetSceneStrategy.kt` — generic `BottomSheetSceneStrategy<T>` + `BottomSheetScene<T>` + `bottomSheet()` metadata helper. Lives next to `NavSuiteSceneDecorator.kt`. Responsibility: turn a metadata-marked top entry into a `ModalBottomSheet`-hosted overlay. Styling (colors/shape/drag handle) is passed in as plain values so this module need not depend on `design-system`.
- **Create** `app/navigation/navigation-compose/src/androidUnitTest/kotlin/com/hedvig/android/navigation/compose/BottomSheetSceneStrategyTest.kt` — unit test for the metadata helper + `calculateScene` decision.
- **Create** `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigBottomSheetSceneStrategy.kt` — `rememberHedvigBottomSheetSceneStrategy()` that reads `HedvigTheme` tokens and returns a configured `BottomSheetSceneStrategy<HedvigNavKey>` (mirrors the existing `rememberHedvigChromeStrategy` seam).
- **Modify** `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigApp.kt:180-205` — add `sceneStrategies = listOf(bottomSheetSceneStrategy, DialogSceneStrategy(), SinglePaneSceneStrategy())` to the `NavDisplay` call.
- **Modify** `app/feature/feature-payments/src/main/kotlin/com/hedvig/android/feature/payments/ui/details/PaymentDetailsDestination.kt` — exemplar migration of site #28: replace the inline `<String>` explanation sheet with a `PaymentDetailExplanationKey` entry + threaded lambda.
- **Modify** `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigEntryProvider.kt` (or the payments sub-builder it delegates to) — register the new entry and thread the `onShowExplanation` lambda into `PaymentDetailsDestination`.

---

## Task 1: Generic `BottomSheetSceneStrategy` in navigation-compose

**Files:**
- Create: `app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/BottomSheetSceneStrategy.kt`
- Create: `app/navigation/navigation-compose/src/androidUnitTest/kotlin/com/hedvig/android/navigation/compose/BottomSheetSceneStrategyTest.kt`

**Interfaces:**
- Produces:
  - `class BottomSheetSceneStrategy<T : Any>(containerColor: Color, contentColor: Color, scrimColor: Color, shape: Shape, dragHandle: @Composable () -> Unit) : SceneStrategy<T>`
  - `BottomSheetSceneStrategy.Companion.bottomSheet(): Map<String, Any>` — metadata marker to attach to an `entry<Key>(metadata = ...)`.
- Consumes: `androidx.navigation3.scene.{Scene, SceneStrategy, SceneStrategyScope, OverlayScene}`, `androidx.navigation3.runtime.{NavEntry, NavMetadataKey, contains, metadata}`, Material3 `ModalBottomSheet`/`rememberModalBottomSheetState`/`SheetState`.

- [ ] **Step 1: Write the failing test**

Create `BottomSheetSceneStrategyTest.kt`:

```kotlin
package com.hedvig.android.navigation.compose

import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.contains
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.SceneStrategyScope
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BottomSheetSceneStrategyTest {
  private fun strategy() = BottomSheetSceneStrategy<String>(
    containerColor = Color.White,
    contentColor = Color.Black,
    scrimColor = Color.Black,
    shape = RectangleShape,
    dragHandle = {},
  )

  private fun entry(key: String, metadata: Map<String, Any> = emptyMap()) =
    NavEntry(key = key, metadata = metadata) { }

  @Test
  fun `bottomSheet metadata is recognized as a bottom-sheet marker`() {
    assertTrue(BottomSheetSceneStrategy.bottomSheet().keys.isNotEmpty())
  }

  @Test
  fun `calculateScene returns null when the top entry has no bottomSheet metadata`() {
    val entries = listOf(entry("a"), entry("b"))
    val scene = with(strategy()) { with(SceneStrategyScope<String>()) { calculateScene(entries) } }
    assertNull(scene)
  }

  @Test
  fun `calculateScene returns an overlay scene that overlays the entries below the sheet`() {
    val entries = listOf(entry("a"), entry("b", BottomSheetSceneStrategy.bottomSheet()))
    val scene = with(strategy()) { with(SceneStrategyScope<String>()) { calculateScene(entries) } }
    val overlay = assertIs<OverlayScene<String>>(scene)
    assertTrue(overlay.overlaidEntries.map { it.contentKey } == listOf<Any>("a"))
    assertTrue(overlay.entries.map { it.contentKey } == listOf<Any>("b"))
  }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :navigation-compose:testDebugUnitTest --tests "com.hedvig.android.navigation.compose.BottomSheetSceneStrategyTest"`
Expected: FAIL — `Unresolved reference: BottomSheetSceneStrategy`.

- [ ] **Step 3: Write the implementation**

Create `BottomSheetSceneStrategy.kt`:

```kotlin
package com.hedvig.android.navigation.compose

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * A [SceneStrategy] that renders any entry whose metadata carries [bottomSheet] inside a
 * [ModalBottomSheet], on top of the entry beneath it. Modeled on the framework
 * `AnimatedBottomSheetSample` and the metadata-opt-in pattern of [NavSuiteSceneDecoratorStrategy].
 *
 * Styling is passed in as plain values so this KMP module needs no dependency on `design-system`;
 * `:app` supplies the Hedvig tokens via `rememberHedvigBottomSheetSceneStrategy`.
 *
 * Must be placed before non-overlay strategies in `NavDisplay(sceneStrategies = ...)`.
 */
@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetSceneStrategy<T : Any>(
  private val containerColor: Color,
  private val contentColor: Color,
  private val scrimColor: Color,
  private val shape: Shape,
  private val dragHandle: @Composable () -> Unit,
) : SceneStrategy<T> {
  override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
    val entry = entries.lastOrNull() ?: return null
    if (BottomSheetKey !in entry.metadata) return null
    return BottomSheetScene(
      key = entry.contentKey,
      entry = entry,
      previousEntries = entries.dropLast(1),
      onBack = onBack,
      containerColor = containerColor,
      contentColor = contentColor,
      scrimColor = scrimColor,
      shape = shape,
      dragHandle = dragHandle,
    )
  }

  companion object {
    /** Attach to a destination's entry metadata to render it as a Hedvig bottom sheet overlay. */
    fun bottomSheet(): Map<String, Any> = metadata { put(BottomSheetKey, Unit) }
  }
}

internal object BottomSheetKey : NavMetadataKey<Unit>

@OptIn(ExperimentalMaterial3Api::class)
private class BottomSheetScene<T : Any>(
  override val key: Any,
  private val entry: NavEntry<T>,
  override val previousEntries: List<NavEntry<T>>,
  private val onBack: () -> Unit,
  private val containerColor: Color,
  private val contentColor: Color,
  private val scrimColor: Color,
  private val shape: Shape,
  private val dragHandle: @Composable () -> Unit,
) : OverlayScene<T> {
  override val entries: List<NavEntry<T>> = listOf(entry)
  override val overlaidEntries: List<NavEntry<T>> = previousEntries

  private lateinit var sheetState: SheetState

  override val content: @Composable () -> Unit = {
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = onBack,
      sheetState = sheetState,
      containerColor = containerColor,
      contentColor = contentColor,
      scrimColor = scrimColor,
      shape = shape,
      dragHandle = dragHandle,
    ) {
      entry.Content()
    }
  }

  // Run the hide animation before the overlay leaves composition when popped.
  override suspend fun onRemove() {
    if (::sheetState.isInitialized) sheetState.hide()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BottomSheetScene<*>) return false
    return key == other.key && previousEntries == other.previousEntries && entry == other.entry
  }

  override fun hashCode(): Int = key.hashCode() * 31 + previousEntries.hashCode() * 31 + entry.hashCode()
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :navigation-compose:testDebugUnitTest --tests "com.hedvig.android.navigation.compose.BottomSheetSceneStrategyTest"`
Expected: PASS (3 tests).
If the module has no `androidUnitTest` source set yet, the first run creates it implicitly via the directory; confirm the test is discovered. If it is not, the module's KMP android target needs `androidUnitTest` — verify with `./gradlew :navigation-compose:tasks --all | grep -i unitTest`.

- [ ] **Step 5: Format and commit**

```bash
./gradlew :navigation-compose:ktlintFormat
git add app/navigation/navigation-compose/src/androidMain/kotlin/com/hedvig/android/navigation/compose/BottomSheetSceneStrategy.kt app/navigation/navigation-compose/src/androidUnitTest/kotlin/com/hedvig/android/navigation/compose/BottomSheetSceneStrategyTest.kt
git commit -m "feat(nav): add BottomSheetSceneStrategy for overlay sheet entries"
```

---

## Task 2: Hedvig-styled strategy factory in :app

**Files:**
- Create: `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigBottomSheetSceneStrategy.kt`

**Interfaces:**
- Consumes: `BottomSheetSceneStrategy` (Task 1), `HedvigTheme`/`BottomSheetDefaults` tokens.
- Produces: `@Composable fun rememberHedvigBottomSheetSceneStrategy(): BottomSheetSceneStrategy<HedvigNavKey>`.

- [ ] **Step 1: Write the implementation**

```kotlin
package com.hedvig.android.app.ui

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.BottomSheetSceneStrategy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberHedvigBottomSheetSceneStrategy(): BottomSheetSceneStrategy<HedvigNavKey> {
  val containerColor = HedvigTheme.colorScheme.backgroundPrimary
  val contentColor = HedvigTheme.colorScheme.textPrimary
  val scrimColor = HedvigTheme.colorScheme.scrim
  val shape = HedvigTheme.shapes.cornerXLarge
  return remember(containerColor, contentColor, scrimColor, shape) {
    BottomSheetSceneStrategy(
      containerColor = containerColor,
      contentColor = contentColor,
      scrimColor = scrimColor,
      shape = shape,
      dragHandle = { },
    )
  }
}
```

Note: confirm the exact token names against `HedvigTheme.colorScheme` (the `scrim` token may be named differently — grep `colorScheme` in `design-system-hedvig`); use `BottomSheetDefaults.DragHandle()` for `dragHandle` if a Hedvig drag handle is wanted. Match whatever `HedvigBottomSheet` uses today (`BottomSheetTokens`/`ScrimTokens` in `design-system-hedvig/.../HedvigBottomSheet.kt`) so migrated sheets look identical.

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL (capture full output to a file; do not pipe through `tail` — that masks Gradle's exit code).

- [ ] **Step 3: Format and commit**

```bash
./gradlew :app:ktlintFormat
git add app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigBottomSheetSceneStrategy.kt
git commit -m "feat(nav): add Hedvig-styled bottom-sheet scene strategy factory"
```

---

## Task 3: Wire scene strategies into the single NavDisplay

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigApp.kt:160-205`

**Interfaces:**
- Consumes: `rememberHedvigBottomSheetSceneStrategy()` (Task 2), `androidx.navigation3.scene.DialogSceneStrategy`, `androidx.navigation3.scene.SinglePaneSceneStrategy`.

- [ ] **Step 1: Add the strategy list next to the existing decorator strategies**

In `HedvigApp.kt`, just after the existing `val sceneDecoratorStrategies = rememberHedvigChromeStrategy(...)` block (line ~161-164), add:

```kotlin
          val bottomSheetSceneStrategy = rememberHedvigBottomSheetSceneStrategy()
          val sceneStrategies = remember(bottomSheetSceneStrategy) {
            listOf(
              bottomSheetSceneStrategy,
              DialogSceneStrategy<HedvigNavKey>(),
              SinglePaneSceneStrategy(),
            )
          }
```

- [ ] **Step 2: Pass `sceneStrategies` to NavDisplay**

In the `NavDisplay(...)` call (line ~180), add the parameter alongside `sceneDecoratorStrategies = sceneDecoratorStrategies,`:

```kotlin
                    sceneStrategies = sceneStrategies,
                    sceneDecoratorStrategies = sceneDecoratorStrategies,
```

Add imports at the top of the file:

```kotlin
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import com.hedvig.android.navigation.common.HedvigNavKey
```

- [ ] **Step 3: Verify the app still builds and renders unchanged**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. At this point no entry carries `bottomSheet()`/`dialog()` metadata, so every entry still resolves to `SinglePaneScene` — behavior is identical to before. This is a safe, separately-committable checkpoint.

- [ ] **Step 4: Manually verify no visual regression**

Build & launch the app; navigate between tabs and into a few detail screens. Expected: identical chrome and transitions to before (the new strategies are inert until an entry opts in). UI correctness cannot be asserted by the compiler — confirm by hand and note it.

- [ ] **Step 5: Format and commit**

```bash
./gradlew :app:ktlintFormat
git add app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigApp.kt
git commit -m "feat(nav): wire bottom-sheet + dialog scene strategies into NavDisplay"
```

---

## Task 4: Exemplar migration — payment details explanation sheet (site #28)

Migrates the `<String>` explanation sheet in `PaymentDetailsDestination.kt` from inline `HedvigBottomSheetState` to a real `PaymentDetailExplanationKey` overlay entry. Chosen as the exemplar because it is B1 (read-only, zero result-passing), its data is a plain `String` (trivially serializable), and the whole change is contained to one feature file + its entry registration.

**Files:**
- Modify: `app/feature/feature-payments/src/main/kotlin/com/hedvig/android/feature/payments/ui/details/PaymentDetailsDestination.kt` (sheet at :131, `show()` at :302, content `PaymentDetailsExplanationBottomSheet` at :437)
- Modify: the payments entry builder where `PaymentDetailsDestination` is registered (find via `grep -rn "PaymentDetailsDestination(" app/app app/feature/feature-payments/src` — it is invoked from the `paymentsEntries`/`addPaymentsEntries` wiring reachable from `HedvigEntryProvider.kt`)
- Verify: `app/feature/feature-payments/build.gradle.kts` has `navKeys()` (add if missing)

**Interfaces:**
- Produces: `@Serializable internal data class PaymentDetailExplanationKey(val title: String, val body: String) : HedvigNavKey`
- Consumes: `BottomSheetSceneStrategy.bottomSheet()` metadata (Task 1), `Backstack.add` / `popBackstack`.

- [ ] **Step 1: Verify `navKeys()` is enabled for feature-payments**

Run: `grep -n "navKeys" app/feature/feature-payments/build.gradle.kts`
Expected: a `navKeys()` line inside `hedvig { }`. If absent, add it:

```kotlin
hedvig {
  // ...existing...
  navKeys()
}
```
and run `./gradlew :feature-payments:compileDebugKotlin` to confirm it configures.

- [ ] **Step 2: Define the key**

In a navigation file of feature-payments (e.g. `.../payments/navigation/PaymentsDestination.kt` — wherever the feature's other `internal` keys live; grep `: HedvigNavKey` under `feature-payments/src`), add:

```kotlin
@Serializable
internal data class PaymentDetailExplanationKey(
  val title: String,
  val body: String,
) : HedvigNavKey
```

(Imports: `com.hedvig.android.navigation.common.HedvigNavKey`, `kotlinx.serialization.Serializable`.)

- [ ] **Step 3: Add the explanation entry next to the payment-details entry**

In the payments entry builder, register the sheet entry with the bottom-sheet metadata. The content is exactly the body that `PaymentDetailsExplanationBottomSheet` renders today (title + body text + close button), but driven by the back stack:

```kotlin
entry<PaymentDetailExplanationKey>(
  metadata = BottomSheetSceneStrategy.bottomSheet(),
) { key ->
  PaymentDetailExplanationContent(
    title = key.title,
    body = key.body,
    onClose = { backstack.popBackstack() },
  )
}
```

- [ ] **Step 4: Move the sheet content into a stateless composable**

In `PaymentDetailsDestination.kt`, replace the `PaymentDetailsExplanationBottomSheet(sheetState)` definition (around :437) with a stateless content composable (no `HedvigBottomSheetState`; the scene supplies the `ModalBottomSheet` chrome):

```kotlin
@Composable
internal fun PaymentDetailExplanationContent(
  title: String,
  body: String,
  onClose: () -> Unit,
) {
  // body lifted verbatim from the old HedvigBottomSheet { } lambda, minus the sheet wrapper:
  HedvigText(text = title, modifier = Modifier.fillMaxWidth())
  Spacer(Modifier.height(8.dp))
  HedvigText(text = body, color = HedvigTheme.colorScheme.textSecondary, modifier = Modifier.fillMaxWidth())
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    onClick = onClose,
    text = stringResource(Res.string.general_close_button),
    enabled = true,
    buttonStyle = Ghost,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(8.dp))
  Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
}
```

(Copy the actual title/body composition from the current `PaymentDetailsExplanationBottomSheet` at :437-455 — the snippet above mirrors the sibling `TerminationScaffold.ExplanationBottomSheet:126` shape; use the payments file's real content.)

- [ ] **Step 5: Replace the inline state + show() call with a navigation lambda**

- Delete `val paymentDetailsExplanationBottomSheetState = rememberHedvigBottomSheetState<String>()` (:131) and the `PaymentDetailsExplanationBottomSheet(...)` call (:132).
- Add an `onShowExplanation: (title: String, body: String) -> Unit` parameter to `PaymentDetailsDestination` (:88) and the inner private composable (:113) that currently holds the sheet, threading it down to the `.clickable { ... }` site (:302).
- Replace `paymentDetailsExplanationBottomSheetState.show(textToShow)` (:302) with `onShowExplanation(<title>, textToShow)` (use the same fixed title the old sheet rendered).
- In the entry that renders `PaymentDetailsDestination` (Step 3's builder), supply:

```kotlin
onShowExplanation = { title, body -> backstack.add(PaymentDetailExplanationKey(title, body)) },
```

- [ ] **Step 6: Confirm the exhaustive serialization test still passes (auto-covers the new key)**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.ExhaustiveBackStackSerializationTest"`
Expected: PASS — the new `PaymentDetailExplanationKey` round-trips through back-stack serialization. If it FAILS with a missing polymorphic serializer, `navKeys()` is missing on feature-payments (Step 1) or the key isn't `@Serializable`.

- [ ] **Step 7: Build and manually verify the sheet**

Run: `./gradlew :app:compileDebugKotlin` (expect SUCCESSFUL), then launch and open Payments → tap the info affordance.
Expected, verify by hand: sheet slides up with Hedvig styling identical to before; scrim tap and system Back both dismiss it (now via `popBackstack`); process death while the sheet is open restores *with the sheet still shown* (was lost before). Note this is a manual check — the compiler can't assert it.

- [ ] **Step 8: Format and commit**

```bash
./gradlew :feature-payments:ktlintFormat :app:ktlintFormat
git add app/feature/feature-payments app/app/src/main/kotlin/com/hedvig/android/app/navigation
git commit -m "feat(payments): migrate payment-detail explanation sheet to a nav3 overlay entry"
```

---

## Task 5: Roll out the remaining read-only & navigation-trigger migrations

Repeat the Task 4 recipe for each remaining **Migrate** site. Each is its own commit + manual verification. Recipe per site: (a) ensure the owning module has `navKeys()`; (b) define an `internal @Serializable …Key : HedvigNavKey` carrying only serializable args; (c) register `entry<Key>(metadata = BottomSheetSceneStrategy.bottomSheet()) { … }`; (d) lift the sheet body into a stateless composable; (e) replace `state.show(x)` with a threaded `onShowX`/`navigateToX` lambda calling `backstack.add(Key(x))`; (f) replace internal dismiss/close with `backstack.popBackstack()`; (g) run `ExhaustiveBackStackSerializationTest`; (h) build + manually verify; (i) ktlintFormat + commit.

**Group A — simple B1 info sheets (`Unit`/`String`/simple data), no result, lowest risk:**
- 1 CustomizeAddonDestination (alreadyActiveAddon, `Unit`) — needs `navKeys()` on feature-addon-purchase.
- 2 ChatLoadedScreen (banner, `DisplayInfo`) — confirm `CbmChatMessage.Banner.DisplayInfo` is `@Serializable`; if not, carry its primitive fields on the key instead.
- 11 ClaimDetailsDestination (explanation, `Unit`).
- 19 InsuranceEvidenceEmailInputDestination (explanation, `Unit`).
- 21 CoverageTab (`InsurableLimit`) — confirm `InsurableLimit` is `@Serializable`; else carry label+description strings.
- 24 YourInfoTab (upcomingChanges, `Unit`).
- 27 PaymentDetailsDestination (foreverInfo, `Unit`).
- 29 DiscountsDestination (foreverInfo, `Unit`).
- 30 MemberPaymentDetailsDestination (`PaymentExplanationData`) — confirm `@Serializable`.
- 32 TerminationScaffold (explanation, `String`) — higher ripple: `TerminationScaffold` has 7 call sites (`grep -rn "TerminationScaffold(" app/feature/feature-terminate-insurance/src`). Add an `onShowExplanation: (String) -> Unit` param to the scaffold and pass `{ backstack.add(TerminationExplanationKey(it)) }` from each of the 7 entries. Do this site last in Group A.

**Group B — shared price-breakdown key (sites 16, 20, 22):** all three render `PriceInfoForBottomSheet`. Highest payoff (one destination reused 3×) but requires `PriceInfoForBottomSheet` to be `@Serializable`. Steps: (1) make `PriceInfoForBottomSheet` `@Serializable` (it holds display items / `UiMoney` — verify each field is serializable, wrap if not); (2) define `PriceBreakdownKey(val info: PriceInfoForBottomSheet) : HedvigNavKey` in a small shared `-navigation` module reachable by feature-insurances **and** feature-edit-coinsured (e.g. extend an existing insurances-navigation module — confirm the dependency is allowed under the feature-isolation rule, which permits depending on `-navigation` modules); (3) register one shared entry in `:app`; (4) replace all three inline price sheets with `backstack.add(PriceBreakdownKey(info))`. If making `PriceInfoForBottomSheet` serializable proves invasive, fall back to keeping these three inline and revisit — record the decision.

**Group C — B2 navigation-trigger sheets (the "result" is a navigation):**
- 3 InboxDestination (newChatSelect, `Unit`) — note one branch opens another sheet; that nested sheet also becomes an entry or stays inline — decide during migration.
- 4 InboxDestination (startClaim, `Unit`).
- 18 HomeDestination (startClaim, `Unit`).
- 23 YourInfoTab (editYourInfo, `Unit`) — actions dismiss → navigate; the key holds the IDs needed; actions call existing `navigateToX` lambdas after `popBackstack()`.
- 25 YourInfoTab (removeAddon, `ContractAddon`) — the key carries `relatedContractId` + `addonVariant` (the args already passed to `navigateToRemoveAddon`/`navigateToUpgradeAddon`), **not** the whole `ContractAddon`. Confirm `addonVariant`'s type is serializable; if not, carry its id and re-resolve.

**Explicitly DO NOT migrate (leave inline, no task):** sites 5, 6, 7, 8, 9, 10, 13, 14, 15, 26, 31 (OS launchers, value-returning B3, trivial confirm). Sites 12 & 17 (cross-sell) are deferred to a separate decision — out of scope here.

- [ ] **Step 1:** Migrate Group A sites one commit each, in the order listed (simplest first, TerminationScaffold last).
- [ ] **Step 2:** Migrate Group B (shared price key) — single feature spike first to validate `PriceInfoForBottomSheet` serialization before touching all three call sites.
- [ ] **Step 3:** Migrate Group C sites one commit each.
- [ ] **Step 4:** After each commit run `./gradlew :app:testDebugUnitTest --tests "*ExhaustiveBackStackSerializationTest"` and `:app:compileDebugKotlin`; manually verify the sheet opens, scrim/back dismiss it, and the surviving inline sheets are untouched.

---

## Self-Review

**Spec coverage:** Phase 0 infra (Tasks 1–3) delivers the overlay scene mechanism + wiring; Task 4 proves it end-to-end on one site; Task 5 covers all remaining Migrate sites and explicitly enumerates the Stay-inline sites with reasons. The full 32-site classification is in the Background table. Dialogs are covered by wiring the built-in `DialogSceneStrategy` in Task 3 (no app dialog currently needs migrating; the strategy is available for future use). The cross-feature lambda question (#3) is intentionally a no-op here — the plan keeps lambdas and only uses `backstack.add` from entries.

**Placeholder scan:** Concrete code is given for Tasks 1–4. Task 5 is a deliberate repeat-the-recipe rollout (DRY: the recipe is spelled out once in Task 4); each site notes its specific serialization caveat rather than hand-waving. Two values must be confirmed against the codebase at execution time and are flagged inline: the exact `HedvigTheme.colorScheme.scrim`/shape token names (Task 2) and each non-primitive sheet payload's `@Serializable`-ness (Task 5) — these are verification steps, not unwritten logic.

**Type consistency:** `BottomSheetSceneStrategy.bottomSheet()` (Task 1) is the same symbol consumed in Tasks 3–5. `rememberHedvigBottomSheetSceneStrategy(): BottomSheetSceneStrategy<HedvigNavKey>` (Task 2) is consumed in Task 3. `PaymentDetailExplanationKey(title, body)` and `PaymentDetailExplanationContent(title, body, onClose)` match between Task 4 Steps 2–5. Scene-strategy ordering (overlays → SinglePane fallback) matches the framework requirement verified in `NavDisplay.kt`.

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-06-18-bottom-sheet-nav3-scenes.md`. Two execution options:

1. **Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration.
2. **Inline Execution** — execute tasks in this session using executing-plans, batch execution with checkpoints.

Which approach?
