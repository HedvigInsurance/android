# Single Flat Home-Pinned Nav3 Back Stack + Process-Death Persistence — Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development for the parallel per-feature
> persistence tasks (Task 5). Tasks 1–4 are tightly coupled and implemented inline. Steps use
> checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the per-tab back-stack design with one flat Home-pinned `SnapshotStateList`, give it
Google's "return to Home, then exit" back behavior, and make it survive process death.

**Architecture:** One real `SnapshotStateList<HedvigNavKey>` rendered by `NavDisplay` and mutated by
all graphs. Home pinned at index 0 when logged in; contiguous per-tab runs above it; `currentTopLevel`
and `isLoggedIn` derived from the list. Persistence via `rememberSerializable` + a merged
`SerializersModule` contributed per feature module through Metro multibinding.

**Tech Stack:** Kotlin, Jetpack Compose, androidx.navigation3 1.2.0-alpha03,
androidx.compose.runtime.saveable.rememberSerializable, androidx.savedstate serialization,
kotlinx.serialization polymorphic, Metro DI.

See spec: `docs/superpowers/specs/2026-06-02-nav3-single-flat-backstack-design.md`.

---

### Task 1: Pure run-logic + `topLevelGraphOrNull` with unit tests

**Files:**
- Create: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogic.kt`
- Test: `app/app/src/test/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogicTest.kt`

- [ ] **Step 1: Write `TopLevelRunLogic.kt`**

```kotlin
package com.hedvig.android.app.navigation

import com.hedvig.android.app.ui.startDestination
import com.hedvig.android.feature.forever.navigation.ForeverKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.core.TopLevelGraph

/** Reverse of [startDestination]: the tab this key is the root of, or null if it is not a tab root. */
internal fun HedvigNavKey.topLevelGraphOrNull(): TopLevelGraph? = when (this) {
  is HomeKey -> TopLevelGraph.Home
  is InsurancesKey -> TopLevelGraph.Insurances
  is ForeverKey -> TopLevelGraph.Forever
  is PaymentsKey -> TopLevelGraph.Payments
  is ProfileKey -> TopLevelGraph.Profile
  else -> null
}

/** The tab owning the top entry: nearest tab key at or below the top. Null if the stack has no tab key. */
internal fun nearestTopLevelGraph(stack: List<HedvigNavKey>): TopLevelGraph? {
  for (index in stack.indices.reversed()) {
    val tab = stack[index].topLevelGraphOrNull()
    if (tab != null) return tab
  }
  return null
}

/**
 * Moves [tab]'s contiguous run to the end, preserving the relative order of the remaining runs.
 * If [tab] has no run yet, appends a fresh `[tab.startDestination]` run.
 */
internal fun moveRunToTop(stack: List<HedvigNavKey>, tab: TopLevelGraph): List<HedvigNavKey> {
  val start = stack.indexOfFirst { it.topLevelGraphOrNull() == tab }
  if (start == -1) return stack + tab.startDestination
  val end = ((start + 1)..stack.lastIndex).firstOrNull { stack[it].topLevelGraphOrNull() != null } ?: stack.size
  val run = stack.subList(start, end).toList()
  return stack.subList(0, start) + stack.subList(end, stack.size) + run
}

/** Truncates to Home's run (Home + its drill-downs), discarding every parked side-tab run. */
internal fun collapseToHome(stack: List<HedvigNavKey>): List<HedvigNavKey> {
  val firstSideRunStart = (1..stack.lastIndex).firstOrNull { stack[it].topLevelGraphOrNull() != null }
    ?: return stack.toList()
  return stack.subList(0, firstSideRunStart).toList()
}

/** Drops the top run's drill-downs, keeping its root key. */
internal fun popTopRunToStart(stack: List<HedvigNavKey>): List<HedvigNavKey> {
  val topRunStart = stack.indexOfLast { it.topLevelGraphOrNull() != null }
  if (topRunStart == -1) return stack.toList()
  return stack.subList(0, topRunStart + 1).toList()
}
```

- [ ] **Step 2: Write `TopLevelRunLogicTest.kt`**

```kotlin
package com.hedvig.android.app.navigation

import com.hedvig.android.feature.forever.navigation.ForeverKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.core.TopLevelGraph
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.Serializable

@Serializable private data class Drill(val id: String) : HedvigNavKey

class TopLevelRunLogicTest {
  @Test fun topLevelGraphOrNull_mapsTabRoots() {
    assertEquals(TopLevelGraph.Home, HomeKey.topLevelGraphOrNull())
    assertEquals(TopLevelGraph.Insurances, InsurancesKey.topLevelGraphOrNull())
    assertEquals(TopLevelGraph.Forever, ForeverKey.topLevelGraphOrNull())
    assertEquals(TopLevelGraph.Payments, PaymentsKey.topLevelGraphOrNull())
    assertEquals(TopLevelGraph.Profile, ProfileKey.topLevelGraphOrNull())
    assertNull(Drill("x").topLevelGraphOrNull())
  }

  @Test fun nearestTopLevelGraph_isOwnerOfTop() {
    assertEquals(
      TopLevelGraph.Insurances,
      nearestTopLevelGraph(listOf(HomeKey, Drill("h"), InsurancesKey, Drill("i"))),
    )
    assertEquals(TopLevelGraph.Home, nearestTopLevelGraph(listOf(HomeKey, Drill("h"))))
    assertNull(nearestTopLevelGraph(listOf(Drill("login"))))
  }

  @Test fun moveRunToTop_appendsWhenAbsent() {
    assertEquals(
      listOf(HomeKey, InsurancesKey),
      moveRunToTop(listOf(HomeKey), TopLevelGraph.Insurances),
    )
  }

  @Test fun moveRunToTop_movesExistingRunPreservingHomeBaseAndOtherOrder() {
    val stack = listOf(HomeKey, Drill("h"), InsurancesKey, Drill("i"), PaymentsKey, Drill("p"))
    assertEquals(
      listOf(HomeKey, Drill("h"), PaymentsKey, Drill("p"), InsurancesKey, Drill("i")),
      moveRunToTop(stack, TopLevelGraph.Insurances),
    )
  }

  @Test fun collapseToHome_keepsHomeRunDiscardsSideRuns() {
    val stack = listOf(HomeKey, Drill("h"), InsurancesKey, Drill("i"), PaymentsKey)
    assertEquals(listOf(HomeKey, Drill("h")), collapseToHome(stack))
    assertEquals(listOf(HomeKey), collapseToHome(listOf(HomeKey, InsurancesKey)))
    assertEquals(listOf(HomeKey), collapseToHome(listOf(HomeKey)))
  }

  @Test fun popTopRunToStart_dropsTopRunDrilldowns() {
    assertEquals(
      listOf(HomeKey, InsurancesKey),
      popTopRunToStart(listOf(HomeKey, InsurancesKey, Drill("i1"), Drill("i2"))),
    )
    assertEquals(listOf(HomeKey), popTopRunToStart(listOf(HomeKey, Drill("h"))))
  }
}
```

- [ ] **Step 3: Run tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.TopLevelRunLogicTest"`
Expected: BUILD SUCCESSFUL, all tests pass. (Grep output for literal `BUILD SUCCESSFUL`.)

- [ ] **Step 4: ktlintFormat + commit**

```bash
./gradlew ktlintFormat
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogic.kt \
        app/app/src/test/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogicTest.kt
git commit -m "feat(nav): pure run logic for single flat top-level back stack"
```

---

### Task 2: Rewrite `HedvigTopLevelBackStacks` to a single flat list

**Files:**
- Modify (full rewrite): `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigTopLevelBackStacks.kt`

This task introduces `rememberSerializable`; the `SavedStateConfiguration` parameter is wired in Task 4.
To keep Task 2 self-contained and compiling, `rememberHedvigTopLevelBackStacks` already takes the
`SavedStateConfiguration` parameter here (its single caller, `MainActivity`, is updated in Task 4).

- [ ] **Step 1: Replace the whole file with:**

```kotlin
package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.core.TopLevelGraph
import kotlinx.serialization.PolymorphicSerializer

/**
 * Owns the app's single Nav3 back stack for [androidx.navigation3.ui.NavDisplay].
 *
 * One flat [SnapshotStateList] is the sole source of truth. Logged out it is `[LoginKey, …]`. Logged
 * in, [HomeKey] is pinned at index 0 and contiguous per-tab "runs" sit above it. Feature graphs
 * mutate [backStack] directly; [NavDisplay] renders the same instance. [isLoggedIn] and
 * [currentTopLevel] are derived, so process-death restore reconstructs them for free.
 */
@Stable
internal class HedvigTopLevelBackStacks(
  val backStack: SnapshotStateList<HedvigNavKey>,
) {
  val isLoggedIn: Boolean
    get() = backStack.firstOrNull()?.topLevelGraphOrNull() != null

  val currentTopLevel: TopLevelGraph
    get() = nearestTopLevelGraph(backStack) ?: TopLevelGraph.Home

  /** The destination on top of the rendered stack — replaces Nav2's `navController.currentDestination`. */
  val currentDestination: HedvigNavKey?
    get() = backStack.lastOrNull()

  /**
   * Rail/bar tap. Re-tapping the current tab pops its run to the root; selecting Home from a side tab
   * returns to Home (discarding parked runs); selecting a different side tab moves its run to the top
   * (preserving Home at the base and the other runs).
   */
  fun selectTopLevel(topLevelGraph: TopLevelGraph) {
    Snapshot.withMutableSnapshot {
      val target = when {
        topLevelGraph == currentTopLevel -> popTopRunToStart(backStack)
        topLevelGraph == TopLevelGraph.Home -> collapseToHome(backStack)
        else -> moveRunToTop(backStack, topLevelGraph)
      }
      backStack.replaceWith(target)
    }
  }

  /**
   * System-back handler. Returns false when the app should finish. Drains the active run, returns a
   * non-Home tab root straight to Home (no wandering into parked tabs), and exits from the root.
   */
  fun handleBack(): Boolean {
    if (backStack.size <= 1) return false
    val topTab = backStack.last().topLevelGraphOrNull()
    Snapshot.withMutableSnapshot {
      if (topTab != null && topTab != TopLevelGraph.Home) {
        backStack.replaceWith(collapseToHome(backStack))
      } else {
        backStack.removeAt(backStack.lastIndex)
      }
    }
    return true
  }

  /** Move into the tabbed shell, Home pinned at the base. */
  fun setLoggedIn() {
    Snapshot.withMutableSnapshot {
      backStack.clear()
      backStack.add(HomeKey)
    }
  }

  /** Drop back to the login root. */
  fun setLoggedOut() {
    Snapshot.withMutableSnapshot {
      backStack.clear()
      backStack.add(LoginKey)
    }
  }
}

/** In-place replace; skips mutation when the content is already equal to avoid recomposition churn. */
private fun SnapshotStateList<HedvigNavKey>.replaceWith(target: List<HedvigNavKey>) {
  if (this == target) return
  clear()
  addAll(target)
}

@Composable
internal fun rememberHedvigTopLevelBackStacks(
  savedStateConfiguration: SavedStateConfiguration,
): HedvigTopLevelBackStacks {
  val backStack = rememberSerializable(
    configuration = savedStateConfiguration,
    serializer = SnapshotStateListSerializer(PolymorphicSerializer(HedvigNavKey::class)),
  ) {
    mutableStateListOf<HedvigNavKey>(LoginKey)
  }
  return remember(backStack) { HedvigTopLevelBackStacks(backStack) }
}
```

- [ ] **Step 2: Add deps to `:app` if `rememberSerializable` / `SnapshotStateListSerializer` /
`SavedStateConfiguration` don't resolve**

If compilation fails on these imports, add to `app/app/build.gradle.kts` dependencies (versions via
catalog if present, else the same versions already on the classpath):
- `androidx.savedstate:savedstate-compose`
- `androidx.compose.runtime:runtime-saveable` (usually already present transitively via Compose BOM)

Prefer to confirm they resolve first (they are transitive via `navigation3-runtime`/`navigation3-ui`)
before adding anything.

- [ ] **Step 3: Compile gate** — deferred to Task 3 (HedvigNavHost/HedvigAppState still reference the
old API until then). Do NOT compile `:app` in isolation here; proceed to Task 3, then compile.

- [ ] **Step 4: Commit** (after Task 3 compiles green — Tasks 2+3 commit together).

---

### Task 3: Update `HedvigNavHost` and verify `HedvigAppState`

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`
- Verify (likely no change): `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppState.kt`

`HedvigAppState` reads only `backStacks.{currentTopLevel, isLoggedIn, currentDestination,
selectTopLevel, setLoggedIn, setLoggedOut, backStack}` — all still present (now derived), so it needs
no change. Confirm by reading it; only touch it if the build complains.

- [ ] **Step 1: Point `NavDisplay` at the single list**

In `HedvigNavHost.kt`, change the `HedvigNavDisplay` argument from `currentBackStack` to `backStack`:

```kotlin
  HedvigNavDisplay(
    backStack = hedvigAppState.backStacks.backStack,
    onBack = {
      if (!hedvigAppState.backStacks.handleBack()) {
        finishApp()
      }
    },
```

(Replace the existing `backStack = hedvigAppState.backStacks.currentBackStack,` and the
`onBack = { popBackStackOrFinish() },` lines.)

- [ ] **Step 2: Keep `popBackStackOrFinish` as-is**

The local `popBackStackOrFinish` (plain pop-or-finish) stays — it is passed to `profileGraph`,
`addonPurchaseNavGraph`, and `chipIdGraph` for drill-down up-navigation, which must NOT collapse to
Home. Only the system back (`onBack`) gets the collapse-to-Home rule.

- [ ] **Step 3: Compile gate**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. (Grep for literal `BUILD SUCCESSFUL`. KMP-with-android feature modules
have no `:feature-X:compileDebugKotlin`; compile through `:app`.)

If `rememberSerializable` needs an opt-in annotation, add `@OptIn(...)` as the compiler directs
(likely none; Nav3 uses it without opt-in).

- [ ] **Step 4: ktlintFormat + commit Tasks 2+3**

```bash
./gradlew ktlintFormat
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigTopLevelBackStacks.kt \
        app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt
# add HedvigAppState.kt only if it was modified
git commit -m "feat(nav): single flat Home-pinned back stack with return-to-Home back behavior"
```

---

### Task 4: Wire `SavedStateConfiguration` in `MainActivity`

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/MainActivity.kt`
- Modify (doc only): `app/navigation/navigation-compose/src/commonMain/kotlin/com/hedvig/android/navigation/compose/DestinationSerializersModule.kt`

- [ ] **Step 1: Inject `Set<SerializersModule>` and build the configuration**

Add the field alongside the other `@Inject` fields:

```kotlin
  @Inject private lateinit var serializersModules: Set<SerializersModule>
```

Imports to add:
```kotlin
import androidx.savedstate.serialization.SavedStateConfiguration
import com.hedvig.android.navigation.compose.merge
import kotlinx.serialization.modules.SerializersModule
```

In `setContent`, replace `val backStacks = rememberHedvigTopLevelBackStacks()` with:

```kotlin
        val savedStateConfiguration = remember(serializersModules) {
          SavedStateConfiguration {
            serializersModule = serializersModules.merge()
          }
        }
        val backStacks = rememberHedvigTopLevelBackStacks(savedStateConfiguration)
```

Add `import androidx.compose.runtime.remember` if not present.

- [ ] **Step 2: Fix the doc comment in `DestinationSerializersModule.kt`**

Replace the final sentence of the KDoc (the part that says the result is fed into the
`SavedStateConfiguration` passed to `rememberNavBackStack`) with: "…folds it with [merge], and feeds
the result into the `SavedStateConfiguration` used by `rememberHedvigTopLevelBackStacks` /
`rememberSerializable`." Keep `HedvigBaseSerializersModule` and `merge()` unchanged.

- [ ] **Step 3: Compile gate**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. The Metro multibinding for `Set<SerializersModule>` may be empty at this
point (no feature contributes yet) — that is fine; an empty set merges to `HedvigBaseSerializersModule`
and the app still builds. (Runtime persistence only works once Task 5 registers subtypes, but a
logged-out cold start seeds `[LoginKey]` which `feature-login` registers in Task 5.)

- [ ] **Step 4: ktlintFormat + commit**

```bash
./gradlew ktlintFormat
git add app/app/src/main/kotlin/com/hedvig/android/app/MainActivity.kt \
        app/navigation/navigation-compose/src/commonMain/kotlin/com/hedvig/android/navigation/compose/DestinationSerializersModule.kt
git commit -m "feat(nav): wire SavedStateConfiguration for back-stack process-death persistence"
```

---

### Task 5: Per-feature `SerializersModule` contributions (parallelizable via subagents)

Each feature module that declares `HedvigNavKey` subtypes contributes exactly one `SerializersModule`
registering all of them. **One subagent per module.** Each subagent:

1. Greps its module for every `HedvigNavKey` subtype:
   `grep -rn ": HedvigNavKey" <module>/src` (include nested/sealed keys; resolve multi-line decls by
   reading the file).
2. Creates a contribution file next to the module's existing navigation/DI code:

```kotlin
package <module navigation package>

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ContributesTo(AppScope::class)
interface <Module>SerializersModuleProvider {
  @Provides
  @IntoSet
  fun provide<Module>SerializersModule(): SerializersModule = SerializersModule {
    polymorphic(HedvigNavKey::class) {
      subclass(KeyA::class)
      subclass(KeyB::class)
      // … every HedvigNavKey subtype in this module
    }
  }
}
```

   For KMP modules (`feature-help-center` commonMain, `feature-remove-addons` androidMain) place the
   file in the matching source set; all imports above are multiplatform-safe.

3. Writes a round-trip unit test asserting each key survives encode→decode with the module installed:

```kotlin
package <package>

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json

class <Module>SerializersModuleTest {
  private val json = Json {
    serializersModule = <Module>SerializersModuleProvider::class.java // see note
      // Instead: instantiate the module directly, see below.
  }

  @Test fun roundTrips() {
    val module = object : <Module>SerializersModuleProvider {}.provide<Module>SerializersModule()
    val json = Json { serializersModule = module }
    val keys: List<HedvigNavKey> = listOf(/* one instance of each key */)
    for (key in keys) {
      val encoded = json.encodeToString(PolymorphicSerializer(HedvigNavKey::class), key)
      val decoded = json.decodeFromString(PolymorphicSerializer(HedvigNavKey::class), encoded)
      assertEquals(key, decoded)
    }
  }
}
```

   Note: instantiate the provider interface anonymously (`object : ...Provider {}`) to call its
   `@Provides` function directly in the test — no DI graph needed. Use the module's existing test
   source set and test deps (`kotlin.test`, kotlinx-serialization-json). If a module lacks a test
   setup, add the minimal `commonTest`/`test` deps mirroring a sibling feature module.

4. Compiles and runs its module's tests:
   `./gradlew :feature-<name>:testDebugUnitTest` (or `:feature-<name>:jvmTest` for KMP commonTest).

**Module checklist (verify each against a fresh grep — list captured 2026-06-02, confirm before use):**

- [ ] `feature-login` — LoginKey, SwedishLoginKey, GenericAuthCredentialsInputKey (+ any others)
- [ ] `feature-home` — HomeKey (+ any home-internal keys)
- [ ] `feature-insurances` — InsurancesKey, TerminatedInsurancesKey
- [ ] `feature-forever` — ForeverKey
- [ ] `feature-payments` — PaymentsKey, PaymentHistoryKey, DiscountsKey, ForeverKey (payments-internal),
      ManualChargeKey, ManualChargeSuccessKey, MemberPaymentDetailsKey, PaymentDetailsKey
- [ ] `feature-profile` — ProfileKey, ContactInfoKey, EurobonusKey, InformationKey, LicensesKey,
      CertificatesKey, SettingsGraphKey, SettingsKey
- [ ] `feature-travel-certificate` — TravelCertificateKey, TravelCertificateChooseContractKey,
      TravelCertificateDateInputKey, TravelCertificateTravellersInputKey, ShowCertificateKey
- [ ] `feature-payout-account` — PayoutAccountKey, SelectPayoutMethodKey, EditBankAccountKey,
      SetupSwishPayoutKey, SetupInvoicePayoutKey
- [ ] `feature-chat` — ChatKey, InboxKey
- [ ] `feature-chip-id` — ChipIdKey (+ nested)
- [ ] `feature-claim-chat` — ClaimChatKey, UpdateAppKey (+ nested)
- [ ] `feature-claim-details` — ClaimDetailsKey (+ nested)
- [ ] `feature-claim-history` — ClaimHistoryKey
- [ ] `feature-connect-payment-trustly` — TrustlyKey
- [ ] `feature-delete-account` — DeleteAccountKey
- [ ] `feature-edit-coinsured` — CoInsuredAddInfoKey, CoInsuredAddOrRemoveKey, EditCoInsuredTriageKey,
      EditCoInsuredSuccessKey (+ any others)
- [ ] `feature-help-center` (KMP commonMain) — HelpCenterKey, HelpCenterHomeKey, PuppyGuideKey,
      PuppyGuideArticleKey (+ any others)
- [ ] `feature-image-viewer` — ImageViewerKey
- [ ] `feature-insurance-certificate` — InsuranceEvidenceKey (+ nested)
- [ ] `feature-movingflow` — SelectContractForMovingKey, HousingTypeKey, EnterNewAddressKey,
      CompareCoverageKey (+ any others; watch nested sealed keys)
- [ ] `feature-addon-purchase` — AddonPurchaseKey, SubmitSuccessKey, SubmitFailureKey (+ any others)
- [ ] `feature-remove-addons` (KMP androidMain) — RemoveAddonsKey, RemoveAddonSubmitSuccessKey,
      RemoveAddonSubmitFailureKey (+ any others)
- [ ] `feature-change-tier` (feature-choose-tier) — ChooseTierKey, StartTierFlowKey,
      StartTierFlowChooseInsuranceKey, ComparisonKey, SubmitSuccessKey, SubmitFailureKey (+ others)
- [ ] `feature-terminate-insurance` — TerminateInsuranceKey, UnknownScreenKey (+ all flow keys)

> The trailing "+ any others" entries MUST be resolved by the per-module grep — do not ship a partial
> registration. A forgotten subtype throws at deserialization (caught by the round-trip test only if
> that key is in the test's key list, so the test must include EVERY key found).

- [ ] **Final step: full app compile + targeted tests**

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
```
Expected: BUILD SUCCESSFUL.

Then commit per-module (or in grouped commits) with explicit paths:
`git commit -m "feat(nav): register <module> HedvigNavKey subtypes for back-stack persistence"`.

---

### Task 6: Manual on-device verification (human)

- [ ] Drill into a side tab; switch tabs via the rail — side-tab drill-down state is preserved.
- [ ] System-back from a side-tab root returns to Home (parked side runs discarded), not the previous tab.
- [ ] Re-tap the current tab pops it to its root.
- [ ] Home-root back exits the app.
- [ ] Enable "Don't keep activities"; background/foreground deep in a side tab — back stack and active
      tab restore correctly (process-death round-trip).
