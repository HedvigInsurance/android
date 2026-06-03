# Nav2 → Nav3 Migration Verification Plan

> **For agentic workers:** This is a *verification* plan, not a feature build. Execute it top-to-bottom, task-by-task, over a long single run. Steps use checkbox (`- [ ]`) syntax for tracking. Do **not** stop between tasks to check in — only stop on a genuine regression you cannot explain, a missing input, or when the whole plan is complete. Record every finding in the running ledger (Task 0.3). Do **not** commit, push, or modify `ugglan/` Swift. When you must change code to *add a test*, that is allowed; changing production nav behavior is **not** — flag it instead.

**Goal:** Prove the stacked Nav2→Nav3 migration (NavKey modernization + single flat home-pinned back stack + process-death persistence + nav-suite chrome decorator) preserves the original app's navigation behavior exactly, with no crash surfaces, no unreachable destinations, and no lost deep links.

**Architecture under test:** One `SnapshotStateList<HedvigNavKey>` rendered by `NavDisplay`; Home pinned at base; contiguous per-tab "runs"; system-back collapses side tabs to Home; the list is `rememberSerializable` so it survives process death via a Metro-multibound `Set<SerializersModule>`. Global chrome (bar/rail) is injected as a `SceneDecoratorStrategy` (`NavSuiteSceneDecoratorStrategy`) living in `navigation-compose`.

**Tech stack:** Kotlin 2.3.21, Navigation3 1.2.0-alpha03, Metro DI, Molecule, KMP (Android + iOS native), Gradle + Develocity, JUnit/assertK/Turbine.

**Source-of-truth specs:**
- `docs/superpowers/specs/2026-06-02-nav-navkey-modernization-design.md`
- `docs/superpowers/specs/2026-06-02-nav3-single-flat-backstack-design.md`
- Plans: `2026-06-02-nav-navkey-modernization.md`, `2026-06-02-nav3-backstack-process-death-persistence.md`, `2026-06-02-nav3-single-flat-backstack.md`

**Standing operational constraints (apply to every task):**
- A piped/tailed Gradle invocation returns exit `0` even on `BUILD FAILED`. **Always grep for the literal string `BUILD SUCCESSFUL`** to confirm success; never trust the shell exit code of a piped gradle command.
- The umbrella native CI is path-filtered and can let a transitively-exported KMP module break the Kotlin/Native build silently. This plan therefore runs the **full** native compile locally rather than trusting CI.
- Known *intentional* gap, not a regression: recents/URL sharing (`onProvideAssistContent`) was dropped in the Nav3 migration and is deferred. Do not chase it as a defect.

---

## How to use the ledger

Create `docs/superpowers/plans/verification-findings.md` at the start (Task 0.3) and append to it as you go. Every task ends with a ledger line: `TASK <id> — PASS | FAIL | GAP | N/A — <one-line note + evidence path>`. `GAP` means "no defect in production behavior, but the verification asset (e.g. a test) is missing." At the end (Task 10.x) you summarize it.

---

## Phase 0 — Baseline & instrumentation

### Task 0.1: Confirm clean, known starting state

**Files:** none (read-only)

- [ ] **Step 1: Record branch + HEAD + working-tree state**

Run:
```bash
cd /Users/stylianosgakis/hedvig/apps/android
git status --short && echo "---" && git rev-parse --abbrev-ref HEAD && git rev-parse HEAD
```
Expected: branch `eng/agentic-refactor` (or the current PR2 branch). Note any modified files so you can distinguish pre-existing edits from anything you touch. Do not revert the user's in-progress edits (e.g. the commented-out `isChromeCaller` guard in `NavSuiteSceneDecorator.kt`).

- [ ] **Step 2: Confirm the schema + strings prerequisites are present** (a missing schema masquerades as a nav failure)

Run:
```bash
ls app/apollo/apollo-octopus-public/src/main/graphql/ >/dev/null 2>&1 && echo SCHEMA_OK || echo SCHEMA_MISSING
```
Expected: `SCHEMA_OK`. If `SCHEMA_MISSING`, run `./gradlew downloadOctopusApolloSchemaFromIntrospection` before proceeding.

- [ ] **Step 3: Ledger** — `TASK 0.1 — PASS — branch/HEAD recorded, schema present`.

### Task 0.2: Establish the baseline (Nav2) behavior reference

**Files:** none (read-only)

The original Nav2 behavior is the oracle. You cannot run the old app, so reconstruct the intended behavior from `develop` and the specs.

- [ ] **Step 1: Diff the nav surface against `develop`** to see exactly what the migration changed:
```bash
git diff --stat develop...HEAD -- 'app/navigation/**' 'app/app/src/main/kotlin/com/hedvig/android/app/navigation/**' 'app/app/src/main/kotlin/com/hedvig/android/app/ui/**' | tail -40
```
- [ ] **Step 2:** Read the "Data flow (unchanged)" and behavioral-rule sections of both specs. Write a short "expected behavior" checklist into the ledger covering: forward push, up, system-back at drill-down, system-back at side-tab root (→ Home), system-back at Home root (→ exit), tab re-tap (→ pop tab to its root), tab switch (→ move run to top, preserve state), select Home from side tab (→ collapse to Home). These become the Phase 8 oracle.
- [ ] **Step 3: Ledger** — `TASK 0.2 — PASS — behavior oracle written to ledger`.

### Task 0.3: Create the findings ledger

**Files:** Create `docs/superpowers/plans/verification-findings.md`

- [ ] **Step 1:** Write the file with a header and an empty `## Findings log` section, plus the Phase 0.2 behavior oracle checklist.
- [ ] **Step 2: Ledger** — `TASK 0.3 — PASS — ledger created`.

---

## Phase 1 — Build gates (does it even assemble, everywhere)

### Task 1.1: Compile the app, all variants

**Files:** none

- [ ] **Step 1: Debug + staging + release compile**
```bash
./gradlew :app:compileDebugKotlin :app:compileStagingKotlin :app:compileReleaseKotlin 2>&1 | tee /tmp/nav-build-app.log | tail -5
grep -c "BUILD SUCCESSFUL" /tmp/nav-build-app.log
```
Expected: the `grep -c` prints `1`. If `0`, open `/tmp/nav-build-app.log`, find the first `e:` error line, record it in the ledger as `FAIL`, and stop — a non-compiling app invalidates everything downstream.

- [ ] **Step 2: Assemble debug APK** (catches resource/manifest/merge issues the Kotlin compile misses)
```bash
./gradlew :app:assembleDebug 2>&1 | tee /tmp/nav-assemble.log | tail -5
grep -c "BUILD SUCCESSFUL" /tmp/nav-assemble.log
```
Expected: `1`.

- [ ] **Step 3: Ledger** — `TASK 1.1 — PASS|FAIL — <log path>`.

### Task 1.2: KMP / iOS native compile gate

**Files:** none

The nav modules (`navigation-common`, `navigation-compose`) and several features are commonMain shared with iOS. A reified/`KClass`/Android-only leak compiles on JVM but breaks native.

- [ ] **Step 1: Compile the iOS simulator target for the nav modules and the umbrella**
```bash
./gradlew :navigation-common:compileKotlinIosSimulatorArm64 :navigation-compose:compileKotlinIosSimulatorArm64 2>&1 | tee /tmp/nav-native.log | tail -8
grep -c "BUILD SUCCESSFUL" /tmp/nav-native.log
```
Expected: `1`.

- [ ] **Step 2: Full umbrella native link** (defeats the path-filtered-CI blind spot — a transitively-exported module can break native without touching `umbrella.yml`'s filter)
```bash
./gradlew :umbrella:compileKotlinIosSimulatorArm64 2>&1 | tee /tmp/nav-umbrella-native.log | tail -8
grep -c "BUILD SUCCESSFUL" /tmp/nav-umbrella-native.log
```
Expected: `1`. Do **not** run the embed/sign Apple framework task or touch `ugglan/`.

- [ ] **Step 3: Ledger** — `TASK 1.2 — PASS|FAIL — native gate`.

### Task 1.3: Lint + ktlint

**Files:** none

- [ ] **Step 1:**
```bash
./gradlew ktlintCheck 2>&1 | tee /tmp/nav-ktlint.log | tail -5
grep -c "BUILD SUCCESSFUL" /tmp/nav-ktlint.log
./gradlew :app:lint 2>&1 | tee /tmp/nav-lint.log | tail -5
grep -c "BUILD SUCCESSFUL" /tmp/nav-lint.log
```
Expected: both `1`. For lint, also scan the report for any new nav-related warnings (unused nav args, missing deep-link intent filters):
```bash
grep -iE "deeplink|navigation|intent-filter" app/app/build/reports/lint-results-debug.txt 2>/dev/null | head
```
- [ ] **Step 2: Ledger** — `TASK 1.3 — PASS|FAIL — lint/ktlint`.

---

## Phase 2 — Static residue scan (no Nav2 idiom survived)

### Task 2.1: Assert zero Nav2 navigation API references

**Files:** none (these greps must each return nothing; any hit is a finding)

- [ ] **Step 1: Run the residue scan**
```bash
cd /Users/stylianosgakis/hedvig/apps/android
echo "== Navigator interface (excl ExternalNavigator) =="; grep -rn "\bNavigator\b" --include="*.kt" app/ | grep -v "ExternalNavigator" | grep -v "navigation.activity"
echo "== Nav2 controller types =="; grep -rn "NavHostController\|rememberNavController\|NavBackstackEntry\b\|NavGraphBuilder\b" --include="*.kt" app/
echo "== Nav2 compose DSL imports =="; grep -rn "androidx.navigation.compose\|androidx.navigation.NavController\|androidx.navigation.navArgument" --include="*.kt" app/
echo "== old Destination marker =="; grep -rn "navigation.common.Destination\b\|: Destination\b\|DestinationNavTypeAware" --include="*.kt" app/
echo "== Nav2 navigate(...) call shape =="; grep -rn "\.navigate(" --include="*.kt" app/ | grep -v "navigateUp\|navigateAndPopUpTo\|navigateToTopLevelGraph\|ExternalNavigator\|externalNavigator\|navigateTo"
```
Expected: every section empty. Any line is a `FAIL` — record the file:line. (Note: `navigateTo*` helper names on screen composables are fine; the bare Nav2 `navigate(dest, ...)` shape is not.)

- [ ] **Step 2: Assert the back-stack extension vocabulary is the only mutation API** — spot-check that graphs mutate `backstack` directly:
```bash
grep -rn "backstack.add(\|backstack::navigateUp\|popUpTo<\|navigateAndPopUpTo<\|removeAllOf<\|findLastOrNull<" --include="*.kt" app/feature/ | wc -l
```
Expected: a non-trivial count (the migration moved every feature to this API). Record the number.

- [ ] **Step 3: Ledger** — `TASK 2.1 — PASS|FAIL — residue scan (N extension call-sites)`.

### Task 2.2: Assert every feature graph builder takes `backstack`, not `navigator`

**Files:** none

- [ ] **Step 1:**
```bash
grep -rln "navigator: Navigator" --include="*.kt" app/
grep -rn "EntryProviderScope<HedvigNavKey>\." --include="*.kt" app/feature/ | grep -i "graph" | head -40
```
Expected: first grep empty. Second lists the per-feature graph builders — sanity-check each has a `backstack: MutableList<HedvigNavKey>` parameter by opening 3–4 at random.
- [ ] **Step 2: Ledger** — `TASK 2.2 — PASS|FAIL`.

---

## Phase 3 — Serialization completeness (the crash surface — highest priority)

> **Why this phase is the centerpiece:** the back stack is `rememberSerializable` over a *non-sealed* `HedvigNavKey` interface. Polymorphic serialization needs **every** concrete subtype registered via `subclass(...)`. There are **124** concrete subtypes across **27** modules. A single missing registration throws at process-death restore — a crash that compiles fine, passes the current test suite (which only covers 6 tab keys), and only fires on a real device under memory pressure. This phase makes that failure mode impossible to ship.

### Task 3.1: Build the authoritative subtype inventory

**Files:** Create `docs/superpowers/plans/navkey-inventory.txt` (scratch artifact)

- [ ] **Step 1: Enumerate every concrete subtype**
```bash
cd /Users/stylianosgakis/hedvig/apps/android
grep -rn ": HedvigNavKey" --include="*.kt" app/ \
  | grep -v "interface HedvigNavKey" \
  | grep -vE "//|/\*" \
  | sed -E 's/.*(class|object|data class|data object) +([A-Za-z0-9_]+).*/\2/' \
  | sort -u > docs/superpowers/plans/navkey-inventory.txt
wc -l docs/superpowers/plans/navkey-inventory.txt
```
Expected: ~124 unique names. Eyeball the file for false positives (e.g. a generic `T : HedvigNavKey` bound, or a sealed parent that is itself abstract). Note any abstract/sealed parents — those are *not* registered directly; their concrete children are.

- [ ] **Step 2: Enumerate every registered subtype**
```bash
grep -rhn "subclass(" --include="*.kt" app/ \
  | sed -E 's/.*subclass\(([A-Za-z0-9_]+)::class\).*/\1/' \
  | sort -u > /tmp/navkey-registered.txt
wc -l /tmp/navkey-registered.txt
```

- [ ] **Step 3: Diff — declared-but-not-registered is the bug**
```bash
echo "=== DECLARED but NOT registered (must be empty) ==="
comm -23 docs/superpowers/plans/navkey-inventory.txt /tmp/navkey-registered.txt
echo "=== REGISTERED but not declared (stale registration / typo) ==="
comm -13 docs/superpowers/plans/navkey-inventory.txt /tmp/navkey-registered.txt
```
Expected: the first set empty. Any entry in the first set is a process-death crash waiting to happen — record each as a `FAIL` with its module. Entries in the second set are stale/typo registrations — record as `GAP`.

- [ ] **Step 4: Ledger** — `TASK 3.1 — PASS|FAIL — <N declared, M registered, K missing>`. If K>0, list the missing types; they are fixed by adding `subclass(X::class)` to that module's contribution (test added in 3.3, do not silently patch without a ledger entry).

### Task 3.2: Verify no serial-name collisions across the merged module

**Files:** none

Polymorphic serialization keys on the serial name (default = FQN). Two keys with the same `@SerialName` silently overwrite and corrupt restore.

- [ ] **Step 1: Find explicit `@SerialName` on nav keys and check for dupes**
```bash
grep -rn "@SerialName(" --include="*.kt" app/feature app/app/src/main app/navigation \
  | sed -E 's/.*@SerialName\("([^"]+)".*/\1/' | sort | uniq -d
```
Expected: empty (no duplicates). Any duplicate is a collision risk — record it and check whether the two annotated declarations are nav keys.
- [ ] **Step 2: Ledger** — `TASK 3.2 — PASS|FAIL — serial-name collisions`.

### Task 3.3: Add an exhaustive round-trip test through the REAL merged module

**Files:**
- Modify: `app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackSerializationTest.kt`
- (Reference) the Metro graph that exposes `Set<SerializersModule>`

This is the one piece of *new code* this plan adds, because the current test (6 keys, local module) cannot catch a missing registration. The new test resolves the **production** merged `Set<SerializersModule>` (or, if that is impractical in a unit test, the union of every feature's `provide*SerializersModule()`), then asserts each of the 124 inventory types round-trips.

- [ ] **Step 1: Locate how `:app` builds the merged module.** Read the production wiring (search `serializersModules.merge()` / `Set<SerializersModule>` provider). Decide between two strategies and record which you chose:
  - (A) **Reflective inventory test** — instantiate the merged module and, for each subtype name in `navkey-inventory.txt`, assert it is registered (via attempting `module.getPolymorphic(HedvigNavKey::class, instanceOrName)`), no construction needed for `data object`s, and a JSON round-trip for the ones you can construct cheaply.
  - (B) **Explicit round-trip list** — import every key and round-trip a representative instance. More verbose but stronger (proves the wire format too).

- [ ] **Step 2: Write the failing test.** Example skeleton (strategy B for tab keys + a sampling harness for the rest):
```kotlin
@Test
fun `every registered HedvigNavKey subtype round-trips through the production module`() {
  val module = productionMergedSerializersModule()        // the same Set<SerializersModule>.merge() :app uses
  val json = Json { serializersModule = module }
  val serializer = PolymorphicSerializer(HedvigNavKey::class)
  // For each sample instance covering all 27 modules' keys:
  allSampleKeys().forEach { key ->
    val restored = json.decodeFromString(serializer, json.encodeToString(serializer, key))
    assertThat(restored).isEqualTo(key)
  }
}
```
Provide `allSampleKeys()` as an explicit list — one constructed instance per concrete subtype (use realistic args; `data object`s are singletons). The list length must equal the inventory count; assert that in the test so a newly-added key without a sample fails CI.

- [ ] **Step 3: Run it, expect failures for any unregistered/uninstantiable key**
```bash
./gradlew :app:testDebugUnitTest --tests "*BackstackSerializationTest*" 2>&1 | tee /tmp/nav-serial-test.log | tail -20
grep -c "BUILD SUCCESSFUL" /tmp/nav-serial-test.log
```
- [ ] **Step 4:** For each failure, the fix is a missing `subclass(...)` in the owning module (or a `@SerialName` collision). Add the registration, re-run. Record each fix in the ledger.
- [ ] **Step 5: Ledger** — `TASK 3.3 — PASS|GAP — exhaustive round-trip (N keys), fixed K registrations`.

### Task 3.4: Per-feature serializer test sweep (defense in depth)

**Files:** none (audit only; creating 27 tests is optional follow-up)

- [ ] **Step 1:** The specs called for one round-trip test per feature module. Audit which exist:
```bash
find app/feature -name "*Test*.kt" | xargs grep -l "SerializersModule\|polymorphic\|subclass" 2>/dev/null
```
Expected per spec: ~23. If far fewer exist, record a `GAP` (the centralized Task 3.3 test now covers correctness; per-feature tests are localized regression guards). Do **not** generate 23 test files in this run unless explicitly asked — log the gap and move on.
- [ ] **Step 2: Ledger** — `TASK 3.4 — PASS|GAP — <found X of ~23 per-feature serializer tests>`.

---

## Phase 4 — Back-stack semantics (the behavior math)

### Task 4.1: Run and audit the pure run-logic tests

**Files:** Read `app/app/src/test/kotlin/com/hedvig/android/app/navigation/TopLevelRunLogicTest.kt`

- [ ] **Step 1: Run them**
```bash
./gradlew :app:testDebugUnitTest --tests "*TopLevelRunLogicTest*" 2>&1 | tee /tmp/nav-runlogic.log | tail -20
grep -c "BUILD SUCCESSFUL" /tmp/nav-runlogic.log
```
Expected: `1`.
- [ ] **Step 2: Coverage audit against the spec's invariants.** Open the test and tick off, in the ledger, that each of these has a case (add the missing ones as new test cases if absent — this is allowed, it's test-only):
  - `topLevelGraphOrNull` maps all 5 tab keys + returns null for a drill-down/login key.
  - `nearestTopLevelGraph` scans from the end.
  - `moveRunToTop`: absent run appends `[tabStartKey]`; present run reorders; **Home stays at base**; relative order of other parked runs preserved.
  - `collapseToHome`: keeps Home + Home drill-downs, discards all side runs.
  - `popTopRunToStart`: keeps top run root, drops its drill-downs; no-op when already at root.
  - `handleBack`: `size<=1` → false (exit); non-Home tab root on top → collapseToHome + true; drill-down on top → pop + true; login-flow entry → pop + true.
  - `selectTopLevel`: re-tap current → popTopRunToStart; select Home from side → collapseToHome; switch side tab → moveRunToTop.
- [ ] **Step 3: Ledger** — `TASK 4.1 — PASS|GAP — run-logic coverage (added K cases)`.

### Task 4.2: Run and audit the back-stack extension tests

**Files:** Read `app/navigation/navigation-compose/src/commonTest/kotlin/com/hedvig/android/navigation/compose/HedvigNavBackstackTest.kt`

- [ ] **Step 1: Run (JVM + native to confirm KMP-clean)**
```bash
./gradlew :navigation-compose:testDebugUnitTest 2>&1 | tee /tmp/nav-ext-jvm.log | tail -10; grep -c "BUILD SUCCESSFUL" /tmp/nav-ext-jvm.log
./gradlew :navigation-compose:iosSimulatorArm64Test 2>&1 | tee /tmp/nav-ext-native.log | tail -10; grep -c "BUILD SUCCESSFUL" /tmp/nav-ext-native.log
```
Expected: both `1`.
- [ ] **Step 2: Coverage audit** — confirm cases for: `popBackstack` at root (returns false, list unchanged); `navigateUp`; `popUpTo<T>` inclusive vs exclusive; `popUpTo<T>` when T absent (define & assert the no-op/clear behavior matches `NavigatorImpl`); `navigateAndPopUpTo<T>` when target present and absent; `findLastOrNull<T>` hit/miss; `removeAllOf<T>` removes all instances. Add missing cases (test-only).
- [ ] **Step 3: Ledger** — `TASK 4.2 — PASS|GAP`.

### Task 4.3: Run the SavedState round-trip test for keys

**Files:** Read `app/navigation/navigation-common/src/commonTest/kotlin/com/hedvig/android/navigation/common/HedvigNavKeySavedStateTest.kt`

- [ ] **Step 1:**
```bash
./gradlew :navigation-common:testDebugUnitTest 2>&1 | tee /tmp/nav-common-test.log | tail -10
grep -c "BUILD SUCCESSFUL" /tmp/nav-common-test.log
```
- [ ] **Step 2: Ledger** — `TASK 4.3 — PASS|FAIL`.

### Task 4.4: Full unit-test sweep of every nav-touching module

**Files:** none

- [ ] **Step 1: Run the whole suite once** (catches anything the targeted runs missed). This is the long-running step.
```bash
./gradlew testDebugUnitTest 2>&1 | tee /tmp/nav-fulltest.log | tail -30
grep -c "BUILD SUCCESSFUL" /tmp/nav-fulltest.log
grep -E "FAILED|> Task .* FAILED" /tmp/nav-fulltest.log | head -40
```
Expected: `BUILD SUCCESSFUL` count `1`, no `FAILED` lines. Triage any failure: nav-related → finding; unrelated flaky → note and re-run that module once.
- [ ] **Step 2: Ledger** — `TASK 4.4 — PASS|FAIL — full unit suite`.

---

## Phase 5 — Deep-link parity (every external entry point still resolves)

> A deep link that no longer maps to a key is an invisible regression — the app just no-ops or 404s on a push notification / marketing link. There are 23 `*DeepLinks.kt` providers.

### Task 5.1: Inventory every deep-link pattern and its target key

**Files:** Create `docs/superpowers/plans/deeplink-inventory.md`

- [ ] **Step 1: Enumerate providers and the patterns they register**
```bash
cd /Users/stylianosgakis/hedvig/apps/android
grep -rln "DeepLink" --include="*.kt" app/feature | grep -i "DeepLinks.kt"
grep -rn "hedvig://\|https://\|addUriPattern\|DeepLink(\|pathPattern\|uriPattern" --include="*.kt" app/feature app/app/src/main | grep -iE "deeplink|uri|hedvig://|https://" | head -120
```
- [ ] **Step 2:** For each provider, record in the inventory file: the URI pattern(s) → the `HedvigNavKey` it produces. Cross-check against the manifest's `<intent-filter>` host/scheme entries:
```bash
grep -nE "scheme|host|pathP|data android" app/app/src/main/AndroidManifest.xml | head -60
```
Every manifest-advertised deep link host/path must have a matching matcher entry. Every matcher must produce a key that exists in the inventory (Task 3.1).
- [ ] **Step 3: Ledger** — `TASK 5.1 — PASS|FAIL — <N patterns inventoried, M unmatched>`.

### Task 5.2: Test the deep-link matcher resolves each pattern

**Files:** Read existing matcher tests (search `HedvigDeepLinkMatcher` tests); add cases if thin.

- [ ] **Step 1:** Run any existing matcher tests; audit coverage against the Task 5.1 inventory. The matcher is in `navigation-compose` commonMain, so a `match(uri)` → expected-key test is pure and cheap.
```bash
find app -name "*Test*.kt" | xargs grep -l "HedvigDeepLinkMatcher\|DeepLinkMatcher" 2>/dev/null
./gradlew :navigation-compose:testDebugUnitTest --tests "*DeepLink*" 2>&1 | tail -15
```
- [ ] **Step 2:** For each inventory pattern with no test, add a `match("hedvig://…") == ExpectedKey(...)` case (test-only). Prioritize the high-traffic ones: home, payments, chat, claim-details, help-center, forever/referrals, edit-coinsured, terminate-insurance.
- [ ] **Step 3: Ledger** — `TASK 5.2 — PASS|GAP — matcher coverage (added K cases)`.

### Task 5.3: Logged-out deep-link buffering

**Files:** Read `app/app/.../urihandler/DeepLinkFirstUriHandler.kt` and the `deepLinkChannel` wiring.

- [ ] **Step 1:** Confirm by reading the code (and any test) that a deep link arriving while logged out is buffered and replayed after login — the spec's data flow says "buffered until logged in." Verify the buffer survives the login transition under the new single-list model (`setLoggedIn()` clears the list to `[HomeKey]`; the buffered key must be pushed *after* that, not before).
- [ ] **Step 2: Ledger** — `TASK 5.3 — PASS|FAIL — logged-out buffering preserved`.

---

## Phase 6 — Nav-suite chrome (bar/rail) decorator correctness

> The chrome moved into a `SceneDecoratorStrategy` (`NavSuiteSceneDecoratorStrategy` in `navigation-compose`). Regressions here are visual: bar appears on the wrong screens, double-composes, jumps during transitions, or shows on full-screen flows.

### Task 6.1: Verify the opt-in marker is attached to exactly the right destinations

**Files:** none

- [ ] **Step 1: List every destination that opts into chrome**
```bash
grep -rn "NavSuiteSceneDecoratorStrategy.showNavBar()" --include="*.kt" app/
```
Expected: the 5 tab roots (Home, Insurances, Forever, Payments, Profile) **plus** the documented deeper destinations that keep the bar (per the migration: InsuranceContractDetail, TerminatedInsurances, Eurobonus, ClaimHistory). Cross-check this set against the original Nav2 app's "which screens show the bottom bar" list (reconstruct from `develop` if unsure). Any screen that showed the bar in Nav2 but is missing here, or vice-versa, is a finding.
- [ ] **Step 2: Confirm no full-screen flow accidentally opts in** — scan the claim flow, chat, login, movingflow, terminate-insurance inner steps for stray `showNavBar()`.
- [ ] **Step 3: Ledger** — `TASK 6.1 — PASS|FAIL — chrome opt-in set (N destinations) matches Nav2`.

### Task 6.2: Verify the rail/bar variant selection

**Files:** Read `app/app/.../ui/HedvigAppState.kt` (`navigationSuiteType` derivation) and `NavigationSuite.kt`.

- [ ] **Step 1:** Confirm the width/height→`NavigationSuiteType` mapping (Compact→Bar, Expanded-height→RailXLarge, else Rail) is unchanged from Nav2, and `NavigationSuiteChrome` renders the matching design-system component. This is a read-and-confirm.
- [ ] **Step 2: Note the user's in-progress experiment.** The `isChromeCaller` guard in `NavSuiteScene.content` is currently commented out (`chromeContent()` runs unconditionally). Per the file's own KDoc, that guard is what prevents the movable chrome from being requested by *both* scenes during a transition. Flag in the ledger that this must be restored (or its removal justified) before merge — with it disabled, transitions between two chrome-bearing scenes may double-compose the chrome. Do not silently re-enable it; surface it.
- [ ] **Step 3: Ledger** — `TASK 6.2 — PASS|FLAG — variant selection OK; isChromeCaller guard disabled (line ~120)`.

### Task 6.3: Compile-confirm the decorator module boundaries

**Files:** none

- [ ] **Step 1:** Confirm `navigation-compose` stays design-system-free (chrome injected via lambda) — the strategy must not import `design.system.hedvig.*`:
```bash
grep -rn "design.system.hedvig" app/navigation/navigation-compose/src/
```
Expected: empty. A hit means the module boundary the migration relied on has been violated.
- [ ] **Step 2: Ledger** — `TASK 6.3 — PASS|FAIL — module boundary intact`.

---

## Phase 7 — Reachability & entry-provider completeness

> Every key must have exactly one `entry<Key>` in some graph builder, and every graph builder must be wired into the app's `entryProvider`. A key with no entry → blank screen / crash when navigated to. An orphan entry → dead code masking a missing screen.

### Task 7.1: Every key has an entry; every entry has a key

**Files:** Create `docs/superpowers/plans/reachability.txt`

- [ ] **Step 1: Collect declared keys (reuse Task 3.1 inventory) and declared entries**
```bash
cd /Users/stylianosgakis/hedvig/apps/android
grep -rhn "entry<" --include="*.kt" app/ | sed -E 's/.*entry<([A-Za-z0-9_]+)>.*/\1/' | sort -u > /tmp/nav-entries.txt
echo "=== keys with NO entry<> (navigable target missing a screen — investigate) ==="
comm -23 docs/superpowers/plans/navkey-inventory.txt /tmp/nav-entries.txt
echo "=== entries with NO matching key (orphan/typo) ==="
comm -13 docs/superpowers/plans/navkey-inventory.txt /tmp/nav-entries.txt
```
Expected nuance: some keys are *parameters-only* or nested-sealed and legitimately lack a direct `entry<>` (they're handled inside a parent entry). Investigate each entry in the first set; classify as `intentional` or `FAIL`. The second set should be empty.
- [ ] **Step 2: Confirm every feature graph builder is invoked** from the app's central `entryProvider`/`HedvigNavHost`:
```bash
grep -rn "Graph(" app/app/src/main/kotlin/com/hedvig/android/app/navigation/ | grep -iE "graph\(|insuranceGraph|homeGraph|profileGraph|paymentsGraph|foreverGraph|chatGraph|claim" | head -60
```
Cross-check the list of `EntryProviderScope<HedvigNavKey>.xGraph(...)` builders (from Task 2.2) against the call sites here. A defined-but-never-invoked graph = a whole feature unreachable.
- [ ] **Step 3: Ledger** — `TASK 7.1 — PASS|FAIL — <K keys without entries (J intentional), L orphan entries, M uninvoked graphs>`.

### Task 7.2: Cross-module navigation call-site sanity

**Files:** none

- [ ] **Step 1:** The migration renamed public entry keys (`…Graph` → `…Key`). Confirm cross-feature navigation (home tiles, cross-sell, help-center deflections) push the correct current key names — there should be no commented-out or TODO'd navigation:
```bash
grep -rn "TODO\|FIXME" --include="*.kt" app/ | grep -iE "navig|backstack|deeplink|nav3|nav2" | head
```
Record any nav-related TODO (the recents/URL-sharing one is expected; anything else is a finding).
- [ ] **Step 2: Ledger** — `TASK 7.2 — PASS|FAIL`.

---

## Phase 8 — Behavioral parity (instrumented / on-device)

> Unit tests prove the math; this phase proves the *app*. Prefer an instrumented test where one exists; otherwise produce a precise manual script for the human. The oracle is the Task 0.2 checklist.

### Task 8.1: Attempt automated instrumented coverage

**Files:** none (discover), then possibly add an instrumented test

- [ ] **Step 1: Check for an emulator/device and existing instrumented nav tests**
```bash
adb devices
find app/app -path "*androidTest*" -name "*.kt" | xargs grep -l "backstack\|NavDisplay\|onBackPressed\|Espresso" 2>/dev/null | head
```
- [ ] **Step 2:** If a device is attached, run any existing instrumented nav tests:
```bash
./gradlew :app:connectedDebugAndroidTest 2>&1 | tee /tmp/nav-instr.log | tail -20; grep -c "BUILD SUCCESSFUL" /tmp/nav-instr.log
```
If none exist, do **not** build a full Espresso suite in this run (large, brittle) — instead write the manual script in 8.2 and mark this `N/A — no device` or `GAP — no instrumented nav coverage`.
- [ ] **Step 3: Ledger** — `TASK 8.1 — PASS|GAP|N/A`.

### Task 8.2: Produce the manual on-device test matrix for the human

**Files:** Append a "Manual test matrix" section to the ledger.

- [ ] **Step 1:** Write a numbered, tap-by-tap script the user can run on a debug build. Cover, with expected results, exactly the Task 0.2 oracle:
  1. Cold start logged in → lands on Home, bar visible, Home tab selected.
  2. Drill into Insurances → a contract detail → bar still visible (it's an opt-in deeper screen); system-back → returns up one level, not to Home.
  3. From Insurances drill-down, system-back repeatedly → drains to Insurances root → next back → **returns to Home** (not exit, not a previously-visited side tab).
  4. At Home root, system-back → **app exits**.
  5. Tab bar: Home → Insurances → Profile, drilling one level into each → re-tap Profile → pops Profile to its root (state of other tabs preserved). Switch back to Insurances → its drill-down is **restored** (run moved, not recreated).
  6. Select Home from a side tab → collapses to Home in the state Home was last in (side runs discarded).
  7. Chrome transition smoothness: switch between two bar-bearing screens → bar stays visually static, does not flash/double (this is where the disabled `isChromeCaller` guard from Task 6.2 would show — call it out explicitly so the tester watches for a double/jittery bar).
  8. Deep links: fire 3 representative links via `adb shell am start -a android.intent.action.VIEW -d "hedvig://…"` (use real patterns from Task 5.1) while logged in → each lands on the right screen with correct chrome. Repeat one while logged **out** → after login, it replays to the right screen.
  9. **Process death:** enable Developer Options → "Don't keep activities". Drill two levels into Payments, switch to Profile, drill one level. Background the app, return → **back stack and active tab restore exactly** (this is the payoff of Phase 3). Then a deep-drilled, rarely-used screen (e.g. terminate-insurance step 3) → background → return → restores without crash (catches an unregistered subtype that Phase 3 should already have caught).
  10. Config change: rotate / fold on a drill-down → state preserved.
- [ ] **Step 2:** Provide the exact `adb` commands for the deep-link and process-death steps so the human can copy-paste.
- [ ] **Step 3: Ledger** — `TASK 8.2 — PASS — manual matrix delivered (awaiting human run)`.

---

## Phase 9 — Process-death & config-change automated confirmation

### Task 9.1: Robolectric/SavedState restore test (if feasible)

**Files:** possibly add a test under `app/app/src/test/...`

- [ ] **Step 1:** Determine whether a Robolectric `SavedStateHandle`/`SavedStateRegistry` round-trip of the actual `rememberSerializable` list is feasible in this project's test setup (check for existing Robolectric usage). If yes, add a test that: builds a representative back stack → saves to a real `Bundle` via the production `SavedStateConfiguration` → restores → asserts equality. This is stronger than Phase 3's JSON round-trip because it exercises the actual SavedState encoder.
- [ ] **Step 2:** If Robolectric isn't wired up here, record `GAP` — Phase 3 (registration completeness) + Phase 8 step 9 (manual) together cover the risk; a Robolectric test is a nice-to-have follow-up.
- [ ] **Step 3: Ledger** — `TASK 9.1 — PASS|GAP|N/A`.

---

## Phase 10 — Synthesis & sign-off

### Task 10.1: Compile the verification report

**Files:** Finalize `docs/superpowers/plans/verification-findings.md`

- [ ] **Step 1:** Summarize the ledger into three buckets:
  - **Blocking (FAIL):** must fix before merge — e.g. unregistered subtype, missing deep link, unreachable key, broken build/test.
  - **Flags:** behavioral risks needing a human decision — e.g. the disabled `isChromeCaller` guard, chrome opt-in set differences.
  - **Gaps (non-blocking):** missing verification assets — e.g. absent per-feature serializer tests, no instrumented nav suite, no Robolectric restore test. Each with a one-line recommended follow-up.
- [ ] **Step 2:** For every `FAIL` you fixed during the run (test-only additions, missing `subclass(...)` registrations), list the exact files changed so the human can review them as a focused diff. **Do not commit.**
- [ ] **Step 3:** State the bottom line in one paragraph: does the migration preserve Nav2 behavior, yes/no/with-caveats, and what remains before it's merge-ready.
- [ ] **Step 4: Ledger** — `TASK 10.1 — PASS — report complete`.

### Task 10.2: Clean up scratch artifacts

**Files:** `docs/superpowers/plans/navkey-inventory.txt`, `deeplink-inventory.md`, `reachability.txt`

- [ ] **Step 1:** Either fold the useful inventories into the findings report or delete the scratch files. Leave the repo as you found it apart from (a) the findings report, (b) any test-only additions, (c) any `subclass(...)` registrations needed to fix a Phase 3 FAIL.
- [ ] **Step 2: Ledger** — `TASK 10.2 — PASS — cleanup done`.

---

## Self-review (run before declaring the plan ready)

- **Coverage:** build (all variants + native), static residue, **serialization completeness (the crash surface)**, back-stack math, deep links, chrome, reachability, behavioral parity, process death. Every spec section maps to a phase.
- **No production behavior changes:** the only code this plan writes is *tests* and *missing serializer registrations* (and only when a registration is genuinely missing — a correctness fix, not a behavior change). Everything else is read-and-confirm.
- **Long-running by design:** the full unit suite (4.4), full native compile (1.2), all-variant assemble (1.1), and the exhaustive serialization test (3.3) are the time sinks; the agent runs continuously through them without check-ins.
- **Highest-leverage task is 3.1/3.3:** if you only had time for one phase, it's serialization completeness — it's the failure mode that compiles, passes today's tests, and crashes on a real device.
