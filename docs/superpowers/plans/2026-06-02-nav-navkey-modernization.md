# Navigation NavKey Modernization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Nav2-era `Destination` marker and `Navigator` interface with Nav3-native `HedvigNavKey` and direct back-stack manipulation, and flatten each feature's key file to top-level keys with a single public entry point.

**Architecture:** Three phases. (A) Atomic global rename + additive back-stack extensions, both keeping the build green. (B) One task per feature graph: flatten that feature's keys, convert its graph builder to take the raw back-stack list, update its deep-link provider / serializer / external references, and update its single app-shell call site. `Navigator` stays alive through Phase B so each task compiles. (C) Delete `Navigator` and its shell construction, then run the full gates.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, Navigation3 (`androidx.navigation3` 1.2.0-alpha03), Metro DI, kotlinx.serialization. Build: Gradle. Tests: JUnit + the existing `:navigation-compose` test source.

**Reference spec:** `docs/superpowers/specs/2026-06-02-nav-navkey-modernization-design.md`

---

## Conventions used throughout (read before any task)

- **Marker:** `HedvigNavKey` (was `Destination`), in `com.hedvig.android.navigation.common`. `NavKeyTypeAware` (was `DestinationNavTypeAware`).
- **Concrete key naming:** `...Key` suffix. Every screen is a top-level key (`internal` unless it's a public entry point).
- **Public entry key naming:** `<FeatureName>Key` (e.g. `TerminateInsuranceKey`). No `Graph` suffix — `public` visibility is the entry-point signal. A feature with several deep-link targets keeps several public keys, each named for its screen (e.g. `ProfileKey`, `ContactInfoKey`, `EurobonusKey`).
- **Screen composables are NOT renamed** — they keep their existing `XDestination()` names. Only key *types* change.
- **Build gate per task:** the module(s) you touched must compile. Use the narrowest gate that proves it, e.g. `./gradlew :feature-<name>:compileDebugKotlin`. Phase A and C also build `:app:assembleDebug`.
- **Formatting:** run `./gradlew ktlintFormat` before each commit.
- **zsh note:** this repo's shell is zsh — quote glob args (`--include="*.kt"`) and use arrays for loops.

---

## Phase A — Core types and extensions

### Task 1: Rename `Destination` → `HedvigNavKey` and `DestinationNavTypeAware` → `NavKeyTypeAware`

This is a single atomic mechanical rename across the whole codebase. The build is green only when every reference is updated, so do it in one task.

**Files:**
- Modify: `app/navigation/navigation-common/src/commonMain/kotlin/com/hedvig/android/navigation/common/Destination.kt` → rename file to `HedvigNavKey.kt`
- Modify: every `.kt` file that references the type `com.hedvig.android.navigation.common.Destination` or `DestinationNavTypeAware` (≈ all key files + graph builders + `navigation-compose` core). The authoritative list is "every file importing `com.hedvig.android.navigation.common.Destination`".

- [ ] **Step 1: Rename the marker file and types**

`HedvigNavKey.kt`:
```kotlin
package com.hedvig.android.navigation.common

import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KType

interface HedvigNavKey : NavKey

interface NavKeyTypeAware {
  val typeList: List<KType>
}
```

- [ ] **Step 2: Update every reference**

Replace, repo-wide, in `.kt` files only:
- `import com.hedvig.android.navigation.common.Destination` → `import com.hedvig.android.navigation.common.HedvigNavKey`
- `import com.hedvig.android.navigation.common.DestinationNavTypeAware` → `import com.hedvig.android.navigation.common.NavKeyTypeAware`
- the type `Destination` used as a supertype / generic arg / bound — i.e. `: Destination`, `<Destination>`, `out Destination`, `KClass<out Destination>`, `: DestinationNavTypeAware` — to the `HedvigNavKey` / `NavKeyTypeAware` equivalents.

**Do not** touch identifiers that merely end in `Destination` (the screen composables like `TerminationDateDestination`, and the per-feature key types like `TerminateInsuranceDestination` — those are handled in Phase B). Target only the bare `Destination` / `DestinationNavTypeAware` type tokens from the `navigation.common` package. Prefer an IDE "Rename" refactor on the two symbols; if using text tools, review every hunk.

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:assembleDebug :navigation-compose:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL. (Pure rename; no behavior change.)

- [ ] **Step 4: Format and commit**

```bash
./gradlew ktlintFormat
git add -A app/
git commit -m "refactor(nav): rename Destination marker to HedvigNavKey"
```

---

### Task 2: Add back-stack extensions with unit tests

Add the central extensions that replace `Navigator`'s non-trivial methods. `Navigator` still exists; these are added alongside and tested now.

**Files:**
- Create: `app/navigation/navigation-compose/src/commonMain/kotlin/com/hedvig/android/navigation/compose/HedvigNavBackStack.kt`
- Test: `app/navigation/navigation-compose/src/commonTest/kotlin/com/hedvig/android/navigation/compose/HedvigNavBackStackTest.kt` (if `commonTest` does not exist in this module, create it; otherwise place under the existing test source set — verify with `ls app/navigation/navigation-compose/src`)

- [ ] **Step 1: Write the failing tests**

```kotlin
package com.hedvig.android.navigation.compose

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable

@Serializable private data object A : HedvigNavKey
@Serializable private data class B(val id: String) : HedvigNavKey
@Serializable private data object C : HedvigNavKey

class HedvigNavBackStackTest {
  @Test fun popBackStack_atRoot_returnsFalseAndKeepsRoot() {
    val stack = mutableListOf<HedvigNavKey>(A)
    assertFalse(stack.popBackStack())
    assertEquals(listOf(A), stack)
  }

  @Test fun popBackStack_popsTop() {
    val stack = mutableListOf<HedvigNavKey>(A, B("x"))
    assertTrue(stack.popBackStack())
    assertEquals(listOf(A), stack)
  }

  @Test fun popUpTo_exclusive_keepsTarget() {
    val stack = mutableListOf<HedvigNavKey>(A, B("x"), C)
    stack.popUpTo<B>(inclusive = false)
    assertEquals(listOf<HedvigNavKey>(A, B("x")), stack)
  }

  @Test fun popUpTo_inclusive_removesTarget() {
    val stack = mutableListOf<HedvigNavKey>(A, B("x"), C)
    stack.popUpTo<B>(inclusive = true)
    assertEquals(listOf<HedvigNavKey>(A), stack)
  }

  @Test fun popUpTo_absentTarget_isNoOp() {
    val stack = mutableListOf<HedvigNavKey>(A, C)
    stack.popUpTo<B>(inclusive = true)
    assertEquals(listOf<HedvigNavKey>(A, C), stack)
  }

  @Test fun navigateAndPopUpTo_popsThenPushes() {
    val stack = mutableListOf<HedvigNavKey>(A, B("x"), C)
    stack.navigateAndPopUpTo<A>(B("y"), inclusive = false)
    assertEquals(listOf<HedvigNavKey>(A, B("y")), stack)
  }

  @Test fun findLastOrNull_returnsMostRecentOfType() {
    val stack = mutableListOf<HedvigNavKey>(B("first"), A, B("second"))
    assertEquals(B("second"), stack.findLastOrNull<B>())
    assertNull(mutableListOf<HedvigNavKey>(A).findLastOrNull<B>())
  }

  @Test fun removeAllOf_removesEveryEntryOfType() {
    val stack = mutableListOf<HedvigNavKey>(B("1"), A, B("2"), C)
    stack.removeAllOf<B>()
    assertEquals(listOf<HedvigNavKey>(A, C), stack)
  }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :navigation-compose:allTests` (or `:navigation-compose:testDebugUnitTest` if that is the module's test task — check `./gradlew :navigation-compose:tasks --group=verification`)
Expected: FAIL — unresolved references `popBackStack`, `popUpTo`, etc.

- [ ] **Step 3: Implement the extensions**

`HedvigNavBackStack.kt`:
```kotlin
package com.hedvig.android.navigation.compose

import com.hedvig.android.navigation.common.HedvigNavKey

/** Pops the top entry. Returns false if the back stack is at its root (nothing popped). */
fun MutableList<HedvigNavKey>.popBackStack(): Boolean {
  if (size <= 1) return false
  removeAt(lastIndex)
  return true
}

/** Equivalent to Nav2 navigateUp(). */
fun MutableList<HedvigNavKey>.navigateUp(): Boolean = popBackStack()

/** Pops up to (and optionally including) the most recent entry of [T]. No-op if absent. */
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.popUpTo(inclusive: Boolean) {
  val index = indexOfLast { it is T }
  if (index == -1) return
  val removeFrom = if (inclusive) index else index + 1
  while (size > removeFrom) {
    removeAt(lastIndex)
  }
}

/** Pops up to (and optionally including) the most recent [T], then pushes [key]. */
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.navigateAndPopUpTo(
  key: HedvigNavKey,
  inclusive: Boolean,
) {
  popUpTo<T>(inclusive)
  add(key)
}

/** Most recent back-stack entry of [T], or null. */
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.findLastOrNull(): T? =
  lastOrNull { it is T } as T?

/** Removes every entry of [T] from the back stack. */
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.removeAllOf() {
  removeAll { it is T }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :navigation-compose:allTests`
Expected: PASS (8 tests).

- [ ] **Step 5: Verify iOS native still compiles**

Run: `./gradlew :navigation-compose:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL (`is T` checks are KMP-clean).

- [ ] **Step 6: Format and commit**

```bash
./gradlew ktlintFormat
git add app/navigation/navigation-compose/
git commit -m "feat(nav): add back-stack extensions to replace Navigator methods"
```

---

## Phase B — Per-feature conversion

### Conversion Recipe (apply in every Phase B task)

For the feature's key file, graph builder, deep-link provider, serializer registration, and app-shell call site:

1. **Flatten the key file.** Remove the `internal sealed interface <Feature>Destination` wrapper. Promote each member to a top-level `@Serializable` key with a `...Key` suffix and `: HedvigNavKey`. Mark `internal` except the public entry point(s). Rename the old public graph-root key `<Feature>GraphDestination` → public `<Feature>Key`. Keep any `companion object : NavKeyTypeAware { override val typeList = ... }` exactly as-is (just on the renamed type). Leave non-key helper data classes (parameter holders) unchanged.

2. **Convert the graph builder.** Change the parameter `navigator: Navigator` → `backStack: MutableList<HedvigNavKey>` (import `com.hedvig.android.navigation.common.HedvigNavKey`; drop `import com.hedvig.android.navigation.compose.Navigator`). Update each `navdestination<OldKey>` to `navdestination<NewKey>`. Translate call sites using this table:

   | Before | After |
   |---|---|
   | `navigator.navigate(key)` | `backStack.add(key)` |
   | `navigator::navigateUp` | `backStack::navigateUp` |
   | `navigator::popBackStack` | `backStack::popBackStack` |
   | `navigator.popBackStack()` | `backStack.popBackStack()` |
   | `navigator.navigate(key, T::class, inclusive)` / `navigate<T>(key, inclusive)` | `backStack.navigateAndPopUpTo<T>(key, inclusive)` |
   | `navigator.popUpTo<T>(inclusive)` | `backStack.popUpTo<T>(inclusive)` |
   | `navigator.findLastOrNull<T>()` | `backStack.findLastOrNull<T>()` |
   | `navigator.clearBackStackOf<T>()` | `backStack.removeAllOf<T>()` |

   Private helper functions in the file that take `navigator: Navigator` (e.g. terminate-insurance's `navigateFromSurvey`, `navigateToTerminateFlowDestination`) get the same parameter swap.

3. **Update the deep-link provider** (`<Feature>DeepLinks.kt`): change `OldKey.serializer()` references to the new key type names.

4. **Update external references.** Other modules referencing the renamed public key (deep-link container patterns, home tiles, cross-feature navigation, the serializer registration in `DestinationSerializersModule`) must use the new name. Find them: `/usr/bin/grep -rn "<OldPublicKeyName>" app --include="*.kt"`.

5. **Update the app-shell call site.** In `HedvigNavHost.kt` (and `HedvigTopLevelBackStacks.kt` / `DeepLinkFirstUriHandler.kt` if they call this graph), change `xGraph(navigator = navigator, ...)` to pass the back-stack list: `xGraph(backStack = <backStackList>, ...)`. The shell still constructs `Navigator(backStackList)` for not-yet-converted features, so the list variable is in scope; pass it directly. (Verify the list variable name by reading `HedvigNavHost.kt` before the first feature task.)

6. **Gate:** `./gradlew :feature-<name>:compileDebugKotlin :app:compileDebugKotlin`, then `./gradlew ktlintFormat`, then commit `refactor(nav): flatten <feature> keys and drop Navigator`.

---

### Task 3: feature-terminate-insurance (canonical worked example)

**Files:**
- Modify: `app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/navigation/TerminateInsuranceDestination.kt`
- Modify: `app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/navigation/TerminateInsuranceGraph.kt`
- Modify: `app/feature/feature-terminate-insurance/src/main/kotlin/com/hedvig/android/feature/terminateinsurance/navigation/TerminateInsuranceDeepLinks.kt`
- Modify: app-shell call site + any external references to `TerminateInsuranceGraphDestination`

- [ ] **Step 1: Flatten the key file**

Rewrite `TerminateInsuranceDestination.kt` so the sealed wrapper is gone and every screen is a top-level key. Public entry renamed to `TerminateInsuranceKey`:
```kotlin
@Serializable
data class TerminateInsuranceKey(
  @SerialName("contractId") val insuranceId: String? = null,
) : HedvigNavKey

@Serializable
internal data class TerminationSurveyFirstStepKey(
  val options: List<TerminationSurveyOption>,
  val action: TerminationAction,
  val commonParams: TerminationGraphParameters,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<List<TerminationSurveyOption>>(),
      typeOf<TerminationAction>(),
      typeOf<TerminationGraphParameters>(),
    )
  }
}

@Serializable
internal data class TerminationSurveySecondStepKey(
  val subOptions: List<TerminationSurveyOption>,
  val action: TerminationAction,
  val commonParams: TerminationGraphParameters,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<List<TerminationSurveyOption>>(),
      typeOf<TerminationAction>(),
      typeOf<TerminationGraphParameters>(),
    )
  }
}

@Serializable
internal data class TerminationDateKey(
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val commonParams: TerminationGraphParameters,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<LocalDate>(),
      typeOf<List<ExtraCoverageItem>>(),
      typeOf<TerminationGraphParameters>(),
    )
  }
}

@Serializable
internal data class TerminationConfirmationKey(
  val terminationType: TerminationType,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val commonParams: TerminationGraphParameters,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey {
  @Serializable
  sealed interface TerminationType {
    @Serializable data object Deletion : TerminationType
    @Serializable data class Termination(val terminationDate: LocalDate) : TerminationType
  }

  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<TerminationType>(),
      typeOf<List<ExtraCoverageItem>>(),
      typeOf<TerminationGraphParameters>(),
    )
  }
}

@Serializable
internal data class InsuranceDeletionKey(
  val commonParams: TerminationGraphParameters,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<TerminationGraphParameters>(),
      typeOf<List<ExtraCoverageItem>>(),
    )
  }
}

@Serializable
internal data class TerminationSuccessKey(
  val terminationDate: LocalDate?,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(typeOf<LocalDate?>())
  }
}

@Serializable
internal data class TerminationFailureKey(
  val message: String?,
) : HedvigNavKey

@Serializable
internal data object UnknownScreenKey : HedvigNavKey

@Serializable
internal data class DeflectSuggestionKey(
  val description: String,
  val url: String?,
  val suggestionType: SuggestionType,
  val commonParams: TerminationGraphParameters,
  val action: TerminationAction,
  val selectedReasonId: String,
  val feedbackComment: String?,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<SuggestionType>(),
      typeOf<TerminationGraphParameters>(),
      typeOf<TerminationAction>(),
    )
  }
}
```
Leave `TerminationDateParameters` and `TerminationGraphParameters` unchanged (with `TerminationDateParameters`'s companion switched to `NavKeyTypeAware` — already done in Task 1).

- [ ] **Step 2: Convert the graph builder**

In `TerminateInsuranceGraph.kt`: change the param to `backStack: MutableList<HedvigNavKey>`, drop the `Navigator` import, add the `HedvigNavKey` import, and replace every `TerminateInsuranceDestination.X` with the new top-level `XKey`, `TerminateInsuranceGraphDestination` with `TerminateInsuranceKey`, and translate `navigator.*` per the Conversion Recipe table. The private helpers `navigateFromSurvey` and `navigateToTerminateFlowDestination` take `backStack: MutableList<HedvigNavKey>` instead of `navigator: Navigator`; the latter's `navigate<TerminateInsuranceGraphDestination>(destination, inclusive = true)` becomes `backStack.navigateAndPopUpTo<TerminateInsuranceKey>(destination, inclusive = true)`.

- [ ] **Step 3: Update deep-link provider + external refs**

In `TerminateInsuranceDeepLinks.kt`, `TerminateInsuranceGraphDestination.serializer()` → `TerminateInsuranceKey.serializer()`.
Run `/usr/bin/grep -rn "TerminateInsuranceGraphDestination" app --include="*.kt"` and update each hit (serializer registration, home/insurance navigation that opens the termination flow). Update the app-shell call in `HedvigNavHost.kt` to pass the back-stack list.

- [ ] **Step 4: Gate**

Run: `./gradlew :feature-terminate-insurance:compileDebugKotlin :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Format and commit**

```bash
./gradlew ktlintFormat
git add -A app/
git commit -m "refactor(nav): flatten terminate-insurance keys and drop Navigator"
```

---

### Tasks 4–26: remaining feature graphs

Apply the **Conversion Recipe** to each feature below, one task per feature, committing after each. For each, the implementer first reads the feature's key file + graph file to enumerate that feature's keys, then applies steps 1–6 of the recipe. Public entry key(s) follow the `<FeatureName>Key` rule; features with multiple deep-link targets keep multiple public keys (noted below). (terminate-insurance is Task 3 and is not repeated here.)

- [ ] **Task 4: feature-home** — `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/navigation/{HomeGraph,HomeDeepLinks}.kt` + home key file. Public: `HomeKey`. (High external-reference count — home is a top-level tab; grep the old public key carefully.)
- [ ] **Task 5: feature-insurances** — `.../feature/insurances/navigation/{InsuranceGraph,InsurancesDeepLinks}.kt`. Public: `InsurancesKey` (top-level tab).
- [ ] **Task 6: feature-profile** — `.../feature/profile/tab/ProfileGraph.kt`, `.../feature/profile/navigation/ProfileDeepLinks.kt`. Multiple public keys: `ProfileKey`, `ContactInfoKey`, `EurobonusKey` (top-level tab + deep-link targets).
- [ ] **Task 7: feature-payments** — `.../feature/payments/navigation/{PaymentsGraph,PaymentsDeepLinks}.kt`. Public: `PaymentsKey`.
- [ ] **Task 8: feature-chat** — `.../feature/chat/navigation/{CbmChatGraph,ChatDeepLinks}.kt`. Public keys for the chat + inbox/conversation deep-link targets (read `ChatDeepLinks.kt`: `ChatDestination`, `ChatDestinations.Chat`).
- [ ] **Task 9: feature-help-center** — `app/feature/feature-help-center/src/commonMain/.../HelpCenterGraph.kt` + `.../navigation/HelpCenterDeepLinks.kt`. **commonMain** — verify iOS native compile too (`:feature-help-center:compileKotlinIosSimulatorArm64` if that target exists).
- [ ] **Task 10: feature-travel-certificate** — `.../feature/travelcertificate/navigation/{TravelCertificateGraph,TravelCertificateDeepLinks}.kt`. Public: `TravelCertificateKey`.
- [ ] **Task 11: feature-addon-purchase** — `.../feature/addon/purchase/navigation/{AddonPurchaseNavGraph,AddonPurchaseDeepLinks}.kt`. The public key is the `TravelAddonTriage` key; `AddonPurchaseDeepLinks.kt` has the custom `AddonDeepLinkMatcher` subclassing `UriDeepLinkMatcher<TravelAddonTriage>` — keep that subclass, just track the renamed key type.
- [ ] **Task 12: feature-choose-tier** — `.../feature/change/tier/navigation/{ChooseTierGraph,ChooseTierNavDestination,ChooseTierDeepLinks}.kt`.
- [ ] **Task 13: feature-claim-details** — `.../feature/claim/details/navigation/{ClaimDetailDestinationGraph,ClaimDetailsDeepLinks}.kt`.
- [ ] **Task 14: feature-connect-payment-trustly** — `.../feature/connect/payment/trustly/navigation/{ConnectTrustlyPaymentGraph,TrustlyDeepLinks}.kt`.
- [ ] **Task 15: feature-delete-account** — `.../feature/deleteaccount/navigation/{DeleteAccountGraph,DeleteAccountDeepLinks}.kt`.
- [ ] **Task 16: feature-edit-coinsured** — `.../feature/editcoinsured/navigation/{EditCoInsuredGraph,EditCoInsuredDeepLinks}.kt`.
- [ ] **Task 17: feature-forever** — `.../feature/forever/navigation/{ForeverGraph,ForeverDeepLinks}.kt`. Public: `ForeverKey`.
- [ ] **Task 18: feature-insurance-certificate** — `.../feature/insurance/certificate/navigation/{InsuranceEvidenceGraph,InsuranceEvidenceDestination,InsuranceEvidenceDeepLinks}.kt`.
- [ ] **Task 19: feature-movingflow** — `.../feature/movingflow/{MovingFlowGraph,MovingFlowDeepLinks}.kt` + the movingflow key file.
- [ ] **Task 20: feature-payout-account** — `.../feature/payoutaccount/navigation/{PayoutAccountGraph,PayoutAccountDestination,PayoutAccountDeepLinks}.kt`.
- [ ] **Task 21: feature-chip-id** — `.../feature/chip/id/navigation/{ChipIdGraph,ChipIdNavDestination,ChipIdDeepLinks}.kt`.
- [ ] **Task 22: feature-image-viewer** — `.../feature/imageviewer/navigation/ImageViewerGraph.kt` (+ its key file; no deep-link provider).
- [ ] **Task 23: feature-login** — `.../feature/login/navigation/LoginGraph.kt` (+ its key file; no deep-link provider).
- [ ] **Task 24: feature-claim-chat** — `app/feature/feature-claim-chat/src/androidMain/.../ClaimChatNavGraph.kt` (androidMain).
- [ ] **Task 25: feature-claim-history** — key file `.../feature/claimhistory/nav/ClaimHistoryDestination.kt` (androidMain; flatten keys even though it may not take Navigator directly).
- [ ] **Task 26: feature-remove-addons** — `app/feature/feature-remove-addons/src/androidMain/.../RemoveAddonsNavGraph.kt` (androidMain).

After every feature is converted, no file should import `com.hedvig.android.navigation.compose.Navigator` except `Navigator.kt` itself and the app-shell construction. Verify:
```bash
/usr/bin/grep -rln "navigator: Navigator" app --include="*.kt"
```
Expected: only app-shell files remain (handled in Phase C).

---

## Phase C — Remove Navigator and finalize

### Task 27: Delete `Navigator` and its shell construction

**Files:**
- Delete: `app/navigation/navigation-compose/src/commonMain/kotlin/com/hedvig/android/navigation/compose/Navigator.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`, `HedvigTopLevelBackStacks.kt`, `app/app/src/main/kotlin/com/hedvig/android/app/urihandler/DeepLinkFirstUriHandler.kt`

- [ ] **Step 1: Remove Navigator construction in the shell**

Delete every `Navigator(backStack)` construction and `Navigator` import in the three shell files. The graph-builder calls already pass the back-stack list (done in Phase B). Where the shell or `DeepLinkFirstUriHandler` itself called `navigator.navigate(...)` for deep links, replace with `backStack.add(...)` / the appropriate extension.

- [ ] **Step 2: Delete Navigator.kt**

```bash
git rm app/navigation/navigation-compose/src/commonMain/kotlin/com/hedvig/android/navigation/compose/Navigator.kt
```

- [ ] **Step 3: Verify no references remain**

Run: `/usr/bin/grep -rn "com.hedvig.android.navigation.compose.Navigator\|: Navigator\b\|Navigator(" app --include="*.kt"`
Expected: no output.

- [ ] **Step 4: Full build gates**

Run: `./gradlew :app:assembleDebug :navigation-compose:compileKotlinIosSimulatorArm64 :navigation-compose:allTests`
Expected: BUILD SUCCESSFUL; tests PASS.

- [ ] **Step 5: Format and commit**

```bash
./gradlew ktlintFormat
git add -A app/
git commit -m "refactor(nav): remove Navigator interface"
```

---

### Task 28: Final verification

- [ ] **Step 1: Confirm clean vocabulary**

Run: `/usr/bin/grep -rn "sealed interface .*Destination\b" app --include="*.kt"`
Expected: no per-feature sealed `…Destination` key wrappers remain (screen composables ending in `Destination` are fine and expected).

- [ ] **Step 2: Full gates one more time**

Run: `./gradlew :app:assembleDebug :navigation-compose:compileKotlinIosSimulatorArm64 ktlintCheck`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Hand back for on-device testing**

Report completion. Note that the same on-device deep-link / back-stack pass pending for PR2 covers this change's behavioral surface.

---

## Notes for the executor

- **Do not commit** `.claude/scheduled_tasks.lock` or the unrelated `.gitignore` `/micro-apps/RaidHelper` change. Stage explicit paths under `app/` and `docs/`.
- **Behavior preservation is the contract.** The extensions in Task 2 are line-for-line ports of `NavigatorImpl`; if any feature relied on subtler Navigator behavior, the per-feature gate plus the extension unit tests should surface it.
- Features that have **no** deep-link provider (image-viewer, login, claim-chat, claim-history, remove-addons) skip recipe step 3.
- `commonMain` features (help-center) and `androidMain` graphs (claim-chat, remove-addons, claim-history) — match the existing source set; verify the matching compile target in the gate.
