# Nav3 Back-Stack Process-Death Persistence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist the app's per-tab and login Navigation 3 back stacks across process death (and the rare Activity recreation) by serializing their `HedvigNavKey` keys with kotlinx.serialization, wired through the existing `HedvigBaseSerializersModule` + `merge()` scaffolding.

**Architecture:** Each feature module contributes a `SerializersModule` (registering its own `HedvigNavKey` subtypes polymorphically) into an `AppScope` `Set<SerializersModule>` via Metro. `:app` folds that set with `merge()` into one `SavedStateConfiguration`, which `rememberHedvigTopLevelBackStacks` feeds into a `rememberSerializable(...)` call per tab/login list. This keeps the project's `HedvigNavKey` typing intact (we deliberately do **not** use Nav3's `rememberNavBackStack`, which is hard-typed to `NavBackStack<NavKey>` and would force the whole `EntryProviderScope<HedvigNavKey>` DSL over to `NavKey`).

**Tech Stack:** Kotlin, Jetpack Compose, Navigation 3 (`androidx.navigation3` 1.2.0-alpha03), `androidx.savedstate` serialization (`rememberSerializable`, `SnapshotStateListSerializer`, `SavedStateConfiguration`), kotlinx.serialization, Metro DI (`dev.zacsweers.metro`).

---

## Background — current state (read before starting)

- `HedvigNavKey : NavKey` (`app/navigation/navigation-common/src/commonMain/kotlin/com/hedvig/android/navigation/common/HedvigNavKey.kt:6`). Every concrete key is already `@Serializable`.
- The persistence scaffolding already exists but is **inert**: `HedvigBaseSerializersModule = SerializersModule {}` (empty) and `fun Iterable<SerializersModule>.merge()` in `app/navigation/navigation-compose/src/commonMain/kotlin/com/hedvig/android/navigation/compose/DestinationSerializersModule.kt:24-28`. Nothing contributes to it, `merge()` has zero callers, and `rememberNavBackStack` is never actually called.
- Today the stacks are created with plain `remember { mutableStateListOf(...) }` (no saver) in `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigTopLevelBackStacks.kt:104-112`. They survive config changes only because `MainActivity` declares `android:configChanges=...` (`app/app/src/main/AndroidManifest.xml:37`), so the Activity is not recreated. They do **not** survive process death.
- There are **23 feature modules** that declare `HedvigNavKey` subtypes (full list in Task 3). No non-feature module declares keys.
- `NavKeyTypeAware` / `typeList` (`HedvigNavKey.kt:8`) is dead code — nothing reads `.typeList`. It is a Nav2 `typeMap` leftover and is **not** needed for this work. Do not rely on it. (Optional removal is Task 8.)

### Why `rememberSerializable` and not `rememberNavBackStack`

From the Nav3 source (`androidx.navigation3.runtime.RememberNavBackStack`): `rememberNavBackStack(configuration, vararg elements): NavBackStack<NavKey>` is literally `rememberSerializable(configuration, NavBackStackSerializer(PolymorphicSerializer(NavKey::class))) { NavBackStack(*elements) }`. `NavBackStack<T>` is `MutableList<T> by base` where `base: SnapshotStateList<T>`. So it yields a `MutableList<NavKey>`, which is incompatible with our `MutableList<HedvigNavKey>` DSL surface. We call the same underlying `rememberSerializable` primitive ourselves with `HedvigNavKey` typing.

## File Structure

**New files (one pair per feature module — 23 modules):**
- `<feature>/.../navigation/<Feature>SerializersModule.kt` — a top-level `internal val <feature>SerializersModule: SerializersModule` plus a `@ContributesTo(AppScope::class)` interface with a `@Provides @IntoSet` function returning it.
- `<feature>/.../navigation/<Feature>SerializersModuleTest.kt` — a kotlinx.serialization round-trip test over that module's keys.

**Modified files:**
- `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigTopLevelBackStacks.kt` — `rememberHedvigTopLevelBackStacks` takes a `SavedStateConfiguration` and creates each list via `rememberSerializable`.
- `app/app/src/main/kotlin/com/hedvig/android/app/MainActivity.kt` — inject `Set<SerializersModule>`, build the `SavedStateConfiguration`, pass it down.
- `app/navigation/navigation-compose/src/commonMain/kotlin/com/hedvig/android/navigation/compose/DestinationSerializersModule.kt` — fix the now-inaccurate doc comment.

**Reference files (read, do not necessarily modify):**
- `app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationMetroProviders.kt` — canonical `@ContributesTo(AppScope::class)` + `@Provides @IntoSet` pattern.
- `app/feature/feature-choose-tier/src/main/kotlin/com/hedvig/android/feature/change/tier/navigation/ChooseTierDeepLinks.kt` — canonical per-feature Metro contribution pattern.
- `app/app/src/main/kotlin/com/hedvig/android/app/ui/TopLevelGraphStartDestination.kt` — the per-tab start keys.

---

### Task 1: Verify the saved-state serialization API on this Nav3 version (spike — blocking)

This task confirms the exact API before any code depends on it. The signatures below were read from `navigation3-runtime` 1.1.1 sources; the project uses `1.2.0-alpha03`, so verify they hold.

**Files:**
- Inspect only (no production changes). You may create a throwaway scratch to compile-check.

- [ ] **Step 1: Confirm `rememberSerializable`, `SnapshotStateListSerializer`, `SavedStateConfiguration` are resolvable and find which artifact provides them**

Run:
```bash
# Find the savedstate-compose and nav3 artifacts actually on the classpath
./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep -iE "savedstate|navigation3" | sort -u
```
Expected: an `androidx.savedstate:savedstate-compose` (or `org.jetbrains.androidx.savedstate:savedstate-compose`) entry and `androidx.navigation3:navigation3-runtime:1.2.0-alpha03`.

- [ ] **Step 2: Confirm the exact `rememberSerializable` signature, especially the per-call `key` parameter**

The per-tab lists are created in a loop, so each `rememberSerializable` call site is positionally identical — they MUST be disambiguated by an explicit `key`. Verify the signature shape:

```kotlin
// Expected (androidx.compose.runtime.saveable.rememberSerializable):
@Composable
fun <T : Any> rememberSerializable(
  vararg inputs: Any?,
  serializer: KSerializer<T>,
  configuration: SavedStateConfiguration = SavedStateConfiguration.DEFAULT,
  key: String? = null,
  init: () -> T,
): T
```

Run (inspect the actual API jar):
```bash
CLS=$(find ~/.gradle/caches -path "*savedstate-compose*" -name "*.jar" | grep -viE "sources|metadata" | head -1)
unzip -o "$CLS" -d /tmp/ssc_api >/dev/null 2>&1
find /tmp/ssc_api -name "*.class" | grep -iE "RememberSerializable|SnapshotStateListSerializer"
# javap the RememberSerializable class to read the signature + presence of a String key param
```
Expected: a `rememberSerializable` overload with a `String`/`key` parameter, and a `SnapshotStateListSerializer` class.

- [ ] **Step 3: Decide fallback if `key` is absent**

If there is NO per-call `key` parameter, the plan's loop bodies must instead wrap each call in `androidx.compose.runtime.key(graph) { rememberSerializable(...) }` to give each tab a distinct positional identity. Record which approach applies; later tasks reference "the verified `rememberSerializable` call form".

- [ ] **Step 4: Confirm `SavedStateConfiguration` builder + `serializersModule` property**

Expected usage: `SavedStateConfiguration { serializersModule = someModule }`. Confirm the builder DSL and property exist (same package as `rememberSerializable`'s `configuration` param type, `androidx.savedstate.serialization.SavedStateConfiguration`).

- [ ] **Step 5: Record findings**

Write the confirmed import paths and the exact `rememberSerializable` call form into the top of this task as a comment for the rest of the plan. No commit (no production changes). If a needed artifact is missing from `:app`/`navigation-compose`, note that Task 5/Task 2 must add it.

---

### Task 2: Worked example — `feature-choose-tier` SerializersModule + round-trip test (TDD)

This is the template every other feature follows in Task 3. Implement it fully here first.

**Files:**
- Create: `app/feature/feature-choose-tier/src/main/kotlin/com/hedvig/android/feature/change/tier/navigation/ChooseTierSerializersModule.kt`
- Test: `app/feature/feature-choose-tier/src/test/kotlin/com/hedvig/android/feature/change/tier/navigation/ChooseTierSerializersModuleTest.kt`
- Possibly modify: `app/feature/feature-choose-tier/build.gradle.kts` (test dependency on kotlinx-serialization-json, only if not already present)

The choose-tier keys (from `ChooseTierNavDestination.kt`) are: `StartTierFlowKey`, `StartTierFlowChooseInsuranceKey`, `ChooseTierKey`, `ComparisonKey`, `SummaryKey`, `SubmitSuccessKey`, `SubmitFailureKey`. Several are `internal`, which is exactly why the registration must live inside this module (the app module cannot see them).

- [ ] **Step 1: Write the failing test**

Create `ChooseTierSerializersModuleTest.kt`:
```kotlin
package com.hedvig.android.feature.change.tier.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class ChooseTierSerializersModuleTest {
  @Test
  fun roundTripsEveryChooseTierKey() {
    val params = InsuranceCustomizationParameters(
      insuranceId = "ins-1",
      activationDate = LocalDate(2026, 1, 1),
      quoteIds = listOf("q1", "q2"),
    )
    val keys: List<HedvigNavKey> = listOf(
      StartTierFlowKey(insuranceId = "ins-1"),
      StartTierFlowChooseInsuranceKey,
      ChooseTierKey(parameters = params),
      ComparisonKey(
        comparisonParameters = com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters(
          termsIds = listOf("t1"),
          selectedTermsVersion = "t1",
        ),
      ),
      SummaryKey(
        params = SummaryParameters(
          quoteIdToSubmit = "q1",
          insuranceId = "ins-1",
          activationDate = LocalDate(2026, 1, 1),
        ),
      ),
      SubmitSuccessKey(activationDate = LocalDate(2026, 1, 1)),
      SubmitFailureKey,
    )

    val json = Json { serializersModule = chooseTierSerializersModule }
    val serializer = ListSerializer(PolymorphicSerializer(HedvigNavKey::class))
    val encoded = json.encodeToString(serializer, keys)
    val decoded = json.decodeFromString(serializer, encoded)

    assertEquals(keys, decoded)
  }
}
```

Note: this uses `Json` purely as a polymorphism check. `SavedStateConfiguration` and `Json` both resolve polymorphism through the same `SerializersModule` mechanism, so a missing `subclass(...)` registration fails identically in both. Json keeps the test a fast pure-JVM unit test.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :feature-choose-tier:testDebugUnitTest --tests "*ChooseTierSerializersModuleTest*"`
Expected: FAIL — unresolved reference `chooseTierSerializersModule` (it does not exist yet). If instead the test module lacks `kotlinx-serialization-json`, add `testImplementation(libs.kotlinx.serialization.json)` to `app/feature/feature-choose-tier/build.gradle.kts` and re-run; then expect the unresolved-reference failure.

- [ ] **Step 3: Write the implementation**

Create `ChooseTierSerializersModule.kt`:
```kotlin
package com.hedvig.android.feature.change.tier.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal val chooseTierSerializersModule: SerializersModule = SerializersModule {
  polymorphic(HedvigNavKey::class) {
    subclass(StartTierFlowKey::class)
    subclass(StartTierFlowChooseInsuranceKey::class)
    subclass(ChooseTierKey::class)
    subclass(ComparisonKey::class)
    subclass(SummaryKey::class)
    subclass(SubmitSuccessKey::class)
    subclass(SubmitFailureKey::class)
  }
}

@ContributesTo(AppScope::class)
interface ChooseTierSerializersModuleProvider {
  @Provides
  @IntoSet
  fun provideChooseTierSerializersModule(): SerializersModule = chooseTierSerializersModule
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :feature-choose-tier:testDebugUnitTest --tests "*ChooseTierSerializersModuleTest*"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/feature/feature-choose-tier/src/main/kotlin/com/hedvig/android/feature/change/tier/navigation/ChooseTierSerializersModule.kt \
        app/feature/feature-choose-tier/src/test/kotlin/com/hedvig/android/feature/change/tier/navigation/ChooseTierSerializersModuleTest.kt \
        app/feature/feature-choose-tier/build.gradle.kts
git commit -m "feat(nav): register choose-tier nav keys for back-stack serialization"
```

---

### Task 3: Repeat the Task 2 pattern for every remaining feature module

Apply the exact Task 2 pattern to each of the remaining 22 feature modules. Each module gets its own `<Feature>SerializersModule.kt` + `<Feature>SerializersModuleTest.kt` and its own commit. The only thing that differs per module is the package, the `val`/interface names, and the `subclass(...)` list.

**The authoritative per-module key list comes from this command (run it; do not guess):**
```bash
grep -rn ": HedvigNavKey" app 2>/dev/null | grep -v /build/ | grep -E "object |class " | sed -E 's#^app/##'
```

**Modules to cover (each a separate sub-task + commit):**
- [ ] `feature-addon-purchase`
- [ ] `feature-chat`
- [ ] `feature-chip-id`
- [ ] `feature-claim-chat`
- [ ] `feature-claim-history`
- [ ] `feature-connect-payment-trustly`
- [ ] `feature-delete-account`
- [ ] `feature-edit-coinsured`
- [ ] `feature-forever`
- [ ] `feature-help-center` (KMP — put files under `src/commonMain` + `src/commonTest`, matching its existing `HelpCenterDeepLinks.kt` location)
- [ ] `feature-home`
- [ ] `feature-image-viewer`
- [ ] `feature-insurance-certificate`
- [ ] `feature-insurances`
- [ ] `feature-login` (must register `LoginDestination` — it seeds the logged-out stack)
- [ ] `feature-movingflow` (inspect its keys at `MovingFlowGraph.kt:55`; if any key nests a sealed `@Serializable` type, that nested hierarchy needs its own `polymorphic(...) { subclass(...) }` block in the same module)
- [ ] `feature-payments`
- [ ] `feature-payout-account`
- [ ] `feature-profile`
- [ ] `feature-remove-addons` (KMP — `src/androidMain`, matching `RemoveAddonsNavGraph.kt`)
- [ ] `feature-terminate-insurance`
- [ ] `feature-travel-certificate`

For each module, per sub-task:

- [ ] **Step 1: List the module's keys**

Run (example for `feature-home`):
```bash
grep -rn ": HedvigNavKey" app/feature/feature-home 2>/dev/null | grep -v /build/ | grep -E "object |class "
```
Record the exact key type names and their source directory (`src/main`, `src/commonMain`, or `src/androidMain`).

- [ ] **Step 2: Write the failing round-trip test**

Create `<Feature>SerializersModuleTest.kt` next to the module's other navigation tests, mirroring Task 2 Step 1. Construct one instance of every key from Step 1 (provide realistic constructor arguments for data-class keys; for nested `@Serializable` param types, build them too). Assert the `Json { serializersModule = <feature>SerializersModule }` list round-trip equals the input.

- [ ] **Step 3: Run the test, confirm it fails**

Run: `./gradlew :<feature-gradle-name>:testDebugUnitTest --tests "*SerializersModuleTest*"`
Expected: FAIL (unresolved `<feature>SerializersModule`). Add `testImplementation(libs.kotlinx.serialization.json)` to that module's `build.gradle.kts` if the test can't resolve `Json`.

- [ ] **Step 4: Write the implementation**

Create `<Feature>SerializersModule.kt` mirroring Task 2 Step 3: an `internal val <feature>SerializersModule` registering every key from Step 1 via `subclass(...)`, plus a `@ContributesTo(AppScope::class)` interface with a `@Provides @IntoSet fun(): SerializersModule` returning it. Keep names unique per module (e.g. `homeSerializersModule` / `HomeSerializersModuleProvider`).

- [ ] **Step 5: Run the test, confirm it passes**

Run: `./gradlew :<feature-gradle-name>:testDebugUnitTest --tests "*SerializersModuleTest*"`
Expected: PASS.

- [ ] **Step 6: Commit (one per module)**

```bash
git add app/feature/<feature>/...
git commit -m "feat(nav): register <feature> nav keys for back-stack serialization"
```

---

### Task 4: Wire `:app` to consume the merged module in `rememberHedvigTopLevelBackStacks`

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigTopLevelBackStacks.kt:104-112`
- Possibly modify: `app/app/build.gradle.kts` (only if Task 1 found the savedstate API is not already on `:app`'s classpath)

- [ ] **Step 1: Add the savedstate dependency if Task 1 flagged it missing**

If Task 1 Step 1 showed `savedstate-compose` is not on `:app`'s `debugRuntimeClasspath`, add its catalog entry to `gradle/libs.versions.toml` and `implementation(libs.androidx.savedstate.compose)` to `app/app/build.gradle.kts`. Otherwise skip.

- [ ] **Step 2: Replace `rememberHedvigTopLevelBackStacks` body to use the verified `rememberSerializable` call form**

Replace the existing function (`HedvigTopLevelBackStacks.kt:104-112`) with (adjust the `rememberSerializable` call to exactly the form confirmed in Task 1):
```kotlin
@Composable
internal fun rememberHedvigTopLevelBackStacks(
  savedStateConfiguration: SavedStateConfiguration,
): HedvigTopLevelBackStacks {
  val tabBackStacks = TopLevelGraph.entries.associateWith { graph ->
    rememberSerializable(
      serializer = SnapshotStateListSerializer(PolymorphicSerializer(HedvigNavKey::class)),
      configuration = savedStateConfiguration,
      key = "hedvig-backstack-tab-${graph.name}",
    ) { mutableStateListOf<HedvigNavKey>(graph.startDestination) }
  }
  val loginBackStack = rememberSerializable(
    serializer = SnapshotStateListSerializer(PolymorphicSerializer(HedvigNavKey::class)),
    configuration = savedStateConfiguration,
    key = "hedvig-backstack-login",
  ) { mutableStateListOf<HedvigNavKey>(LoginDestination) }
  return remember(tabBackStacks, loginBackStack) {
    HedvigTopLevelBackStacks(tabBackStacks, loginBackStack)
  }
}
```
Add imports: `androidx.compose.runtime.saveable.rememberSerializable`, `androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer`, `androidx.savedstate.serialization.SavedStateConfiguration`, `kotlinx.serialization.PolymorphicSerializer`. Keep the existing `mutableStateListOf`, `remember`, `TopLevelGraph`, `HedvigNavKey`, `LoginDestination`, `startDestination` imports.

If Task 1 found no `key` parameter, instead wrap each call: `key("hedvig-backstack-tab-${graph.name}") { rememberSerializable(serializer = ..., configuration = ...) { ... } }` using `androidx.compose.runtime.key`.

The `HedvigTopLevelBackStacks` class, `ForwardingBackStack`, and `MutableListDelegate` are unchanged — `rememberSerializable` returns a `SnapshotStateList<HedvigNavKey>`, exactly the type the constructor expects.

- [ ] **Step 3: Compile to verify types line up**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. (Note: a piped/tailed gradle invocation can mask failures — grep for the literal `BUILD SUCCESSFUL`.) If it fails on `rememberHedvigTopLevelBackStacks` call sites, that is expected — Task 5 fixes the caller.

- [ ] **Step 4: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigTopLevelBackStacks.kt app/app/build.gradle.kts gradle/libs.versions.toml
git commit -m "feat(nav): create top-level back stacks via rememberSerializable"
```

---

### Task 5: Inject `Set<SerializersModule>` in `MainActivity` and build the `SavedStateConfiguration`

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/MainActivity.kt` (field injection near line 71; call site at line 160)

- [ ] **Step 1: Add the injected set field**

In the field-injection block (alongside `deepLinkMatcherProviders` at `MainActivity.kt:71`), add:
```kotlin
  @Inject private lateinit var serializersModules: Set<SerializersModule>
```
Add imports: `kotlinx.serialization.modules.SerializersModule`, `com.hedvig.android.navigation.compose.merge`, `androidx.savedstate.serialization.SavedStateConfiguration`, `androidx.compose.runtime.remember`.

- [ ] **Step 2: Build the configuration and pass it to `rememberHedvigTopLevelBackStacks`**

Replace `MainActivity.kt:160`:
```kotlin
        val backStacks = rememberHedvigTopLevelBackStacks()
```
with:
```kotlin
        val savedStateConfiguration = remember(serializersModules) {
          SavedStateConfiguration { serializersModule = serializersModules.merge() }
        }
        val backStacks = rememberHedvigTopLevelBackStacks(savedStateConfiguration)
```

- [ ] **Step 3: Compile and run the full `:app` unit tests**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL. The Metro graph must resolve `Set<SerializersModule>` from the 23 feature contributions; a missing `@ContributesTo`/`@Provides @IntoSet` in any feature surfaces here as a graph/compile error.

- [ ] **Step 4: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/MainActivity.kt
git commit -m "feat(nav): feed merged SerializersModule into back-stack persistence"
```

---

### Task 6: Fix the scaffolding doc comment

**Files:**
- Modify: `app/navigation/navigation-compose/src/commonMain/kotlin/com/hedvig/android/navigation/compose/DestinationSerializersModule.kt:6-22`

- [ ] **Step 1: Correct the inaccurate reference**

In the KDoc, change the sentence that says the merged module is fed into "the `SavedStateConfiguration` passed to `rememberNavBackStack`" to reference `rememberSerializable`:
```
 * Features contribute their module via Metro `@Provides @IntoSet`; `:app` injects the resulting
 * `Set<SerializersModule>`, folds it with [merge], and feeds the result through a
 * `SavedStateConfiguration` into the `rememberSerializable` calls in
 * `rememberHedvigTopLevelBackStacks` (we do not use `rememberNavBackStack` — it is hard-typed to
 * `NavBackStack<NavKey>`, incompatible with our `HedvigNavKey` DSL).
```

- [ ] **Step 2: Commit**

```bash
git add app/navigation/navigation-compose/src/commonMain/kotlin/com/hedvig/android/navigation/compose/DestinationSerializersModule.kt
git commit -m "docs(nav): correct back-stack persistence wiring comment"
```

---

### Task 7: Manual on-device process-death verification

Automated tests cover per-feature polymorphic registration (Task 2/3) and DI wiring (Task 5). The only true end-to-end check is a real process-death restore, because `configChanges` already masks rotation.

**Files:** none.

- [ ] **Step 1: Build and install the debug app**

Run: `./gradlew :app:installDebug`
Expected: installs `com.hedvig.dev.app`.

- [ ] **Step 2: Drill into a non-Home tab and force process death**

In the running app: log in, switch to (e.g.) the Insurances tab, navigate 2-3 screens deep, then switch to the Payments tab and go one screen deep. Background the app (Home button), then kill the process:
```bash
adb shell am kill com.hedvig.dev.app
```
Relaunch from the launcher.

- [ ] **Step 3: Verify each tab restored its own depth**

Expected: the app reopens on the Insurances tab at the same depth (2-3 screens in), and switching to Payments shows its 1-screen depth preserved. No crash. If a `SerializationException` for an unregistered subtype appears in logcat (`adb logcat | grep -i serializ`), the named key's feature module is missing its `subclass(...)` registration — return to Task 3 for that module.

- [ ] **Step 4: Record the result**

No commit. Note pass/fail and any missing-registration follow-ups.

---

### Task 8 (optional, separate concern): Remove dead `NavKeyTypeAware`

`NavKeyTypeAware`/`typeList` has no readers and is not used by this serialization approach. Removing it is safe cleanup but unrelated to persistence — do it only if desired, as its own commit. It touches `HedvigNavKey.kt:8` and the ~12 feature companion objects that implement it.

**Files:**
- Modify: `app/navigation/navigation-common/src/commonMain/kotlin/com/hedvig/android/navigation/common/HedvigNavKey.kt` and every `companion object : NavKeyTypeAware` declaration.

- [ ] **Step 1: Confirm zero readers**

Run: `grep -rn "\.typeList" app | grep -v /build/ | grep -v "override val typeList"`
Expected: no output (no consumers).

- [ ] **Step 2: Delete the interface and all implementations**

Remove `interface NavKeyTypeAware { ... }` from `HedvigNavKey.kt`, and remove every `companion object : NavKeyTypeAware { override val typeList = ... }` (and the now-unused `NavKeyTypeAware`/`KType`/`typeOf` imports) across the feature modules found by:
```bash
grep -rln "NavKeyTypeAware" app | grep -v /build/
```

- [ ] **Step 3: Compile everything**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor(nav): remove unused NavKeyTypeAware"
```

---

## Risks & notes for the executor

- **All-or-nothing coverage:** a single feature missing its `subclass(...)` registration crashes only at save/restore time. Task 3's per-feature round-trip tests are the guardrail; Task 7 is the backstop. Do not skip a module because it "only has cross-feature keys" — the feature that *owns* a key (the one that defines it) must register it, regardless of who pushes it onto the stack.
- **Nested types:** `@Serializable` data-class params (e.g. `InsuranceCustomizationParameters`) serialize via their generated serializers and need no polymorphic registration. Only `HedvigNavKey` subtypes need `subclass(...)`. Watch `feature-movingflow` for nested *sealed* hierarchies that need their own polymorphic block.
- **Metro `interface` providers:** `@Provides @IntoSet` on a non-abstract function inside a `@ContributesTo(AppScope::class)` interface is the established pattern (`ApplicationMetroProviders.kt`). Keep provider interface names unique per module.
- **Gradle exit codes:** when checking build success, grep for the literal `BUILD SUCCESSFUL` — a piped/tailed gradle command can return exit 0 even on `BUILD FAILED`.
- **Scope:** this spans 23 feature modules; it is intentionally a standalone PR, separate from the in-flight nav2→nav3 migration branch.
```