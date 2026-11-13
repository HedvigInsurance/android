# Sharing Presenters and Data with iOS Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Drive a SwiftUI claim history screen, first in an iOS shell app in the Android repo and then in Ugglan on an experiment branch, from the shared Kotlin `ClaimHistoryViewModel`, with no Compose UI in the iOS framework and no change to Android.

**Architecture:** Step 0 removes the data layer's edge to Compose resources, binds the DataStore-backed auth and demo classes per platform, and adds an opt-in guard against Compose UI in shared `commonMain`. Step 1 gives `feature-claim-history` iOS targets by moving its Compose UI to `jvmAndAndroidMain` and adds a narrow public facade in `nativeMain`. Step 2 builds a native Metro graph in `:umbrella` whose platform inputs come from Swift. Steps 3 to 5 consume it from a checked-in SwiftUI shell, compare three Swift bridges, and repeat the proof in Ugglan.

**Tech Stack:** Kotlin 2.4.21 Multiplatform, Metro 1.5.2 with metrox-viewmodel, Molecule 2.2.0, Apollo Kotlin 5.2.0, Ktor 3.6 Darwin, KMP-NativeCoroutines 1.0.6 (Swift package 1.0.5), SwiftUI on Xcode 26.4.1, iOS simulator `iPhone 17 Pro`.

**Spec:** `docs/superpowers/specs/2026-11-13-ios-sharing-design.md` in the docs worktree `/Users/stylianosgakis/hedvig/apps/android/.claude/worktrees/docs-desktop-claude-md`. Executors read it before their task. Design letters (A to K) and owner decision numbers (1 to 14) below refer to it.

## Global Constraints

Every task's requirements include this section.

**Workspace and git**

- Each task works in its own git worktree and branch, created by the orchestrator from the integration branch tip under `/Users/stylianosgakis/hedvig/apps/android/.claude/worktrees/<name>`. Finished branches are pushed and merged with `git merge --no-ff` into `feat/desktop-app-integration` by the orchestrator. No pull requests are opened for this work. No force pushes. Exceptions, stated in their tasks: bridge candidate branches (Tasks 13 to 15) are pushed and not merged; the Ugglan branch (Task 19) is local only.
- Verify `HEAD` is your branch before each commit (another session can switch a clone's branch).
- Pure file moves are their own commits made with `git mv` (R100 in `git show --stat -M`), before any edit commit. A file that is split moves first (whole file, R100), then the edit commit carves the new file out of it.
- Never stop Gradle daemons or kill processes the task did not start. One Gradle invocation at a time per agent, never in the background. At most two building agents run at once; each task states what it can run in parallel with (disjoint files) or that it cannot.
- First command in a fresh worktree: `./gradlew downloadOctopusApolloSchemaFromIntrospection downloadStrings`. Do not use `git stash`. Implementers do not dispatch subagents.
- AGP 9: Android library modules are release-only. KMP modules verify with `compileAndroidMain`, `compileKotlinJvm`, `jvmTest`; the app with `:app:assembleDebug`.

**Android and desktop do not change**

- Android and desktop behaviour must not change. Where a task touches Android-compiled code it runs recipe P1 and P2 (below) against its base and states every binding difference exactly. When DI, serialization or keep rules are touched it also runs P3 (the minified `:app:assembleStaging`).
- Bind per platform in Metro. Never put a platform-overridable `@ContributesBinding` default in `commonMain`.
- Anti-goals (design "Anti-goals"): no Compose UI or Compose resources linked into the iOS framework; no Swift-only annotations, wrapper types, platform conditionals or second state model in `commonMain` presenters; no Swift-facing navigation port; only what the claim history proof needs becomes public.

**iOS proof**

- Where a module gains or changes an iOS target: P4 (the module's `compileKotlinIosSimulatorArm64` and `compileKotlinIosArm64`, and `:umbrella:linkDebugFrameworkIosSimulatorArm64`). Size (P5) is recorded after every design step in the size log `/Users/stylianosgakis/hedvig/apps/android/.superpowers/sdd/2026-11-14-ios-sharing/size-log.md`, and by every task that changes what the umbrella links.
- No PR CI job compiles an iOS target, so every iOS compile here is a local run on this Mac.

**Owner decisions that bind every task**

- Experiment-only skip rule (decision 12): a piece may be skipped and reported when sharing it would make Android's `commonMain` much worse. A skip is never shipped around. Every skip goes into the ledger with what was skipped, why sharing it would have made `commonMain` much worse, and what completing it on iOS would take.
- iOS is not second class (decisions 12 and 13): anything that would later ship to Ugglan through the umbrella must be complete and correct on iOS. A piece that cannot be both good for Android and complete on iOS is not shared, or the work stops. Size is reported, never a stop condition; a step adding more than about 3 MB, or any Compose UI or Skiko symbol, is called out.
- Interim defaults until the owner answers open questions 1 and 2: on native, Kotlin's logout on `UNAUTHENTICATED` is off, and there is no Kotlin-side Datadog tracing. The shell (Tasks 9 to 16) runs on these defaults. Step 5 (Ugglan) is gated on the owner answering open questions 1 and 2 and begins with the stop-and-ask Task 17.
- The Ugglan work happens on an experiment branch in the Ugglan repo, not for merge. Ugglan's local umbrella loop reads the `android/` clone specifically (`$UGGLAN_ROOT/../android`), so it needs the owner's go-ahead to check out the integration work in `android/`. Task 17 is that gate; no task performs it as an action.

**Safety**

- No writes outside the staging test account; nothing irreversible. Claim history is read-only.
- The owner scans the BankID QR code for every login (staging test member 666227461, or a member the owner names). An agent never drives the owner's phone, its pickers or its settings, and never changes this Mac's network or system settings.
- Simulator builds that will run are signed: plain `xcodebuild build` for the simulator signs with "Sign to Run Locally". Never pass `CODE_SIGNING_ALLOWED=NO` to a build that will be installed: it strips the Keychain entitlement and token writes fail with `-34018`. `xcodebuild test` runs with `-parallel-testing-enabled NO`.

**Writing**

- No em-dash and no spaced dash in code comments, commit messages or docs. Comments describe current code only (`CLAUDE.md`, "Comments"); no history, no "for iOS".
- Never make an import-only change to a line you are not otherwise editing. Run `./gradlew ktlintFormat` (or the module-scoped task) before committing; build-logic is formatted by hand.

## Review Focus

The five conditions the spec implies that no compile catches, most likely first. Each has a test or check in the owning task.

1. **Logout, then login as another member, shows the previous member's claims** from the Kotlin normalized cache, because the cache listener is missing from the native graph or the session call is not wired. Expected: the second member sees only their own claims. Pinned by Task 10 (`sessionChanged(null) clears the Apollo store`) and the member switch in Tasks 12 and 19.
2. **Leaving the screen does not stop the presenter**, because the Swift owner of the screen model is retained (closure cycle, `NavigationStack` keeping the view) and `close()` never runs. Expected: after the screen is popped the Kotlin ViewModel is cleared and no further `ClaimsHistory` request appears. Pinned by Task 10 (`close stops requests`) and the deinit and log check in Task 12, per candidate in Tasks 13 to 15.
3. **A Kotlin exception crosses into Swift and terminates the app**, for example `MoleculeViewModel.emit` throwing on a full event buffer when Retry is tapped more than 20 times while nothing collects. Expected: no crash, the extra reloads are dropped and logged. Pinned by Task 10 (`many reloads before observing do not throw`) and the exception probe in Tasks 13 to 15.
4. **State stalls while the app is backgrounded** because `DisplayLinkClock` stops with the display link, leaving the screen on Loading after return. Expected: on return to the foreground the screen shows the latest state within a frame or two. Pinned by the background check in Task 12.
5. **Ugglan's production configuration makes Kotlin pick the staging auth environment or log response bodies**, because `CommonHedvigBuildConstants.isProduction` is `buildType == "release" && applicationId == "com.hedvig.app"` and Ugglan's bundle id and build type are its own. Expected: a production Ugglan build resolves `isProduction == true` and production URLs. Pinned by the `AppBuildConfig` mapping check in Task 19.

---

## Orchestration

**Run directory** (gitignored, shared by all tasks): `/Users/stylianosgakis/hedvig/apps/android/.superpowers/sdd/2026-11-14-ios-sharing/` (below: `$RUN`). It holds `ledger.md`, `size-log.md`, `kit/` (proof scripts from Task 1), `baselines/<sha>/` (graph and test baselines per integration tip), `bridge-comparison.md`, `parity.md`, `owner-answers.md`, and per-task briefs and reports.

**Worktree per task**, created by the orchestrator:

```bash
git -C /Users/stylianosgakis/hedvig/apps/android fetch origin feat/desktop-app-integration
git -C /Users/stylianosgakis/hedvig/apps/android worktree add -b ios/<slug> .claude/worktrees/ios-<slug> origin/feat/desktop-app-integration
cp /Users/stylianosgakis/hedvig/apps/android/{local.properties,lokalise.properties} /Users/stylianosgakis/hedvig/apps/android/.claude/worktrees/ios-<slug>/
```

**Dispatch brief** contains: the task text, the Global Constraints, the "Proof recipes" section, the worktree path, `$RUN`, and the instruction to read `CLAUDE.md` and the spec first.

**After each task** the orchestrator reads the full diff, re-runs the task's verification itself, pushes, merges with `git merge --no-ff` into `feat/desktop-app-integration` in `/Users/stylianosgakis/hedvig/apps/android/.claude/worktrees/desktop-integration`, pushes that, runs P2 on the new tip into `$RUN/baselines/<new sha>/`, and updates the ledger. A report is a claim, not proof.

**After the last task of each design step merges**, the orchestrator runs P5 on the integration tip and appends a row labelled with the step.

**Phase A** (Tasks 6, 9, 11, 19): the implementer first writes `$RUN/task-<n>-proposal.md` (inventory and proposal, as the task specifies) and stops. The orchestrator reviews it, may amend names in later tasks' Interfaces blocks to match, and only then sends the implementer on to the code steps.

**Gates** (Task 17) are run by the orchestrator with the owner, not dispatched.

### Order and parallelism

| Step | Tasks | Runs when |
|---|---|---|
| 0 | 1 | first, alone |
| 0 | 2, 3, 4 | after 1; any two at once (disjoint files) |
| 1 | 5, then 6 | 5 after 4; 6 after 5 |
| 2 | 7, 8 | after 1; alongside 5 or 6 (disjoint files), at most two building |
| 2 | 9 | after 3, 6, 7, 8 |
| 3 | 10, 11 | after 9; together |
| 3 | 12 | after 11 |
| 4 | 13, 14, then 15 | after 12; two at once |
| 4 | 16 | after 13 to 15 |
| 5 | 17 (gate) | after 16 |
| 5 | 18, then 19 | after 17 |
| | 20 | last |

### Ledger

| Task | Branch | Merged at | Status | Graph difference | Skips | Size row |
|---|---|---|---|---|---|---|

---

## Proof recipes

Tasks reference these by name. "Base" is the integration commit the task's branch was created from; base artifacts are read from `$RUN/baselines/<base sha>/` when present, otherwise produced in the worktree before the first edit.

**P1: Android and desktop build and tests.**
`./gradlew checkCommonMainImports :app:assembleDebug :app-desktop:compileKotlinJvm :app-desktop:jvmTest`, then `./gradlew test ktlintCheck`. Expected: BUILD SUCCESSFUL both times. Test count: `$RUN/kit/testcount.sh <worktree>` sums the `tests=` attribute of every `build/test-results/**/TEST-*.xml`; it equals the base count plus exactly the tests the task added, which the report names.

**P2: Android Metro graph.**
`JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :app:compileReleaseKotlin --rerun` (confirm `> Task :app:compileReleaseKotlin` executed, not `FROM-CACHE`), then `javap -p -c` of `app/app/build/intermediates/built_in_kotlinc/release/compileReleaseKotlin/classes/com/hedvig/android/app/di/AppGraph$Impl.class` and `AppGraph$Impl$ActivityRetainedGraphImpl.class` into `<prefix>-AppGraph$Impl.javap` and `<prefix>-AppGraph$Impl$ActivityRetainedGraphImpl.javap`, and `$RUN/kit/graphsets.sh <prefix>`. Expected: 87 ViewModel map entries (48 `getViewModelProviders`, 37 `getManualAssistedFactoryProviders`, 2 `getAssistedFactoryProviders`) and 21 deep link matcher providers, the same sets as base. Registered keys: copy `$RUN/kit/TmpEntryRegistrationDumpTest.kt` into `app/app/src/test/kotlin/com/hedvig/android/app/`, run `./gradlew :app:testDebugUnitTest --tests "*TmpEntryRegistrationDumpTest*"`, delete the file (never commit it); expected 111 keys, identical to base after stripping identity hashes. `diff` of the two `.javap` files against base: either empty, or exactly the difference the task states under "Expected graph difference".

**P3: Minified staging.**
`./gradlew :app:assembleStaging` at base and at the tip. Compare `app/app/build/outputs/mapping/staging/mapping.txt` class count and obfuscated names, and the class list from `apkanalyzer dex packages --defined-only`. Expected: identical, or differences limited to the classes the task renamed or moved, each named in the report.

**P4: iOS compile and link.**
`./gradlew :<module>:compileKotlinIosSimulatorArm64 :<module>:compileKotlinIosArm64 :umbrella:linkDebugFrameworkIosSimulatorArm64`. Expected: BUILD SUCCESSFUL.

**P5: Shipped size.**
`./gradlew :umbrella:linkReleaseFrameworkIosArm64`, then `$RUN/kit/size.sh` on `app/umbrella/build/bin/iosArm64/releaseFramework/HedvigShared.framework/HedvigShared`: shipped bytes as `umbrella.yml` computes them (`size -m`, sum of `__TEXT` and non-zerofill `__DATA` sections), `nm | grep -c kfun:`, and `nm | grep -cE 'skiko|kfun:androidx\.compose\.(ui|foundation|material)|kfun:org\.jetbrains\.compose\.resources'` (expected 0). Append a row to `size-log.md`: step, task, commit, shipped bytes, delta against the previous row, running delta against the Task 1 baseline, `kfun` count, UI symbol count, callout (more than about 3 MB in one step, or any UI symbol).

---

## Step 0: foundation fixes

### Task 1: Baselines and proof kit

No repository change. Runs alone, first.

**Files:**
- Create: `$RUN/kit/graphsets.sh` (copy of `/private/tmp/claude-501/-Users-stylianosgakis-hedvig-apps/36e261de-9171-41a2-b878-39021f480114/scratchpad/b/graphsets.sh`), `$RUN/kit/TmpEntryRegistrationDumpTest.kt` (copy of `.../scratchpad/b/TmpEntryRegistrationDumpTest.kt` in the same directory), `$RUN/kit/size.sh`, `$RUN/kit/testcount.sh`
- Create: `$RUN/baselines/99815f5a70/` (P1 count, P2 outputs, P3 mapping and class list), `$RUN/size-log.md`, `$RUN/ledger.md`, `$RUN/parity.md`

**Interfaces:**
- Produces: the four kit scripts with the arguments P1, P2 and P5 use; the base artifacts for `99815f5a70`; the size baseline row.

- [ ] **Step 1: Copy the two kit files** and read them. If either source file is gone, rebuild it from the description in P2 (`graphsets.sh` extracts `ProvideIntoSet*DeepLinkMatcherProviderMetroFactory.create` names and the classes loaded in the three ViewModel map getters; the dump test is described in `task-18-report.md` line 617 of `/Users/stylianosgakis/hedvig/apps/android/.claude/worktrees/desktop-app-design/.superpowers/sdd/2026-11-12-desktop-app/`) and say so.
- [ ] **Step 2: Write `size.sh` and `testcount.sh`** as P5 and P1 describe. `size.sh <binary>` prints three lines: `shipped=<bytes>`, `kfun=<count>`, `ui_symbols=<count>`.
- [ ] **Step 3: Run P1, P2 and P3** in a worktree at `99815f5a70`. Expected: 87 / 21 / 111; record the test count.
- [ ] **Step 4: Check the data layer's iOS classpath today.** `./gradlew :network-clients:dependencies --configuration iosSimulatorArm64CompileKlibraries | grep -c components-resources`. Expected: 1 or more (this is the edge Task 2 removes). Record the number.
- [ ] **Step 5: Run P5.** Write the first size-log row labelled `baseline 99815f5a70`. Seed `parity.md` with the rows of design table G and a "verified" column left empty.

---

### Task 2: `Language.label` leaves `language-core`

Design A, step 0 item 1. Parallel with Tasks 3 and 4.

`Language.label` has two callers, both Compose UI files. No UI module both features depend on may depend on `language-core` without a cycle (`core-resources` is reached from `core-common-public`'s `androidMain`), so each caller gets its own `internal` copy next to it, in its own package so the import line goes away.

**Files:**
- Move: `app/language/language-core/src/commonMain/kotlin/com/hedvig/android/language/LanguageLabel.kt` to `app/feature/feature-login/src/commonMain/kotlin/com/hedvig/android/feature/login/marketing/LanguageLabel.kt`
- Create: `app/feature/feature-profile/src/commonMain/kotlin/com/hedvig/android/feature/profile/settings/LanguageLabel.kt`
- Modify: `app/feature/feature-login/src/commonMain/kotlin/com/hedvig/android/feature/login/marketing/MarketingDestination.kt:58` (drop `import com.hedvig.android.language.label`)
- Modify: `app/feature/feature-profile/src/commonMain/kotlin/com/hedvig/android/feature/profile/settings/SettingsDestination.kt:41` (same)
- Modify: `app/language/language-core/build.gradle.kts` (drop `implementation(projects.coreResources)` from `commonMain`)

**Interfaces:**
- Produces: `internal val Language.label: StringResource` in packages `com.hedvig.android.feature.login.marketing` and `com.hedvig.android.feature.profile.settings`. `language-core` has no dependency on `core-resources` in any source set.

- [ ] **Step 1: Failing check.** Task 1 step 4's command. Expected now: 1 or more.
- [ ] **Step 2: Move commit.** `git mv` the file to feature-login; commit alone ("Move Language.label next to its marketing screen caller").
- [ ] **Step 3: Edit commit.** Change the moved file's package and make it `internal`; create the feature-profile copy with the same body; remove the two imports; drop the dependency.
- [ ] **Step 4: Check passes.** Same command. Expected: `0`. If not 0, `./gradlew :network-clients:dependencyInsight --configuration iosSimulatorArm64CompileKlibraries --dependency components-resources` names the path. If the path is one more single `commonMain` edge to `core-resources` that only a UI file needs, fix it the same way in this task; otherwise stop and report the path.
- [ ] **Step 5: Proof.** P1, P2 (expected graph difference: none), P4 for `language-core`, `core-build-constants`, `network-clients`, `auth-core-public`.
- [ ] **Step 6: Commit** and report the test count and the before and after classpath counts.

---

### Task 3: Auth and demo bindings per platform

Design A, step 0 item 2. Parallel with Tasks 2 and 4.

`auth-core-public` and `core-demo-mode` have JVM and iOS targets and no Android target, so Android consumes their JVM variant: a binding moved from `commonMain` to `jvmMain` leaves Android's graph as it is.

Deviation from the design text, decided here: the design moves `AuthTokenServiceImpl` to `jvmMain` and also has the native graph bind it (F). A `jvmMain` class is invisible to `nativeMain` and to `:umbrella`, so that is unbuildable. `AuthTokenServiceImpl` is bound on every platform and overridden on none, so it stays in `commonMain` with its `@ContributesBinding`, unchanged. This is not a platform-overridable default.

**Files:**
- Move: `app/auth/auth-core-public/src/commonMain/kotlin/com/hedvig/android/auth/storage/AuthTokenStorage.kt` to `app/auth/auth-core-public/src/jvmMain/kotlin/com/hedvig/android/auth/storage/DataStoreAuthTokenStorage.kt`
- Create: `app/auth/auth-core-public/src/commonMain/kotlin/com/hedvig/android/auth/storage/AuthTokenStorage.kt`
- Move: `app/auth/auth-core-public/src/commonMain/kotlin/com/hedvig/android/auth/AndroidAccessTokenProvider.kt` to `app/auth/auth-core-public/src/jvmMain/kotlin/com/hedvig/android/auth/AndroidAccessTokenProvider.kt`
- Move: `app/auth/auth-core-public/src/commonMain/kotlin/com/hedvig/android/auth/LogoutUseCase.kt` to `app/auth/auth-core-public/src/jvmMain/kotlin/com/hedvig/android/auth/LogoutUseCaseImpl.kt`
- Create: `app/auth/auth-core-public/src/commonMain/kotlin/com/hedvig/android/auth/LogoutUseCase.kt`
- Move: `app/core/core-demo-mode/src/commonMain/kotlin/com/hedvig/android/core/demomode/DemoManager.kt` to `app/core/core-demo-mode/src/jvmMain/kotlin/com/hedvig/android/core/demomode/DataStoreDemoManager.kt`
- Create: `app/core/core-demo-mode/src/commonMain/kotlin/com/hedvig/android/core/demomode/DemoManager.kt`
- Modify: `app/auth/auth-core-public/build.gradle.kts`, `app/core/core-demo-mode/build.gradle.kts` (the two DataStore dependencies move from `commonMain` to `jvmMain`)
- Modify (tests): `app/auth/auth-core-public/src/jvmTest/kotlin/com/hedvig/android/auth/AuthTokenServiceImplTest.kt:131`, `.../storage/AuthTokenStorageTest.kt:47`, `.../interceptor/AndroidAccessTokenProviderTest.kt:196`, `app/app-shell/src/jvmTest/kotlin/com/hedvig/android/app/navigation/SessionReconcilerTest.kt:101` (construct `DataStoreAuthTokenStorage`)

**Interfaces:**
- Produces, `commonMain`, package `com.hedvig.android.auth.storage`:

```kotlin
interface AuthTokenStorage {
  fun getTokens(): Flow<AuthTokens?>
  suspend fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken)
  suspend fun clearTokens()
}
```

- `jvmMain`: `@Inject @SingleIn(AppScope::class) @ContributesBinding(AppScope::class) class DataStoreAuthTokenStorage(dataStore: DataStore<Preferences>, clock: Clock = Clock.System) : AuthTokenStorage`, public (the `:app-shell` test constructs it), body unchanged.
- `commonMain` `interface LogoutUseCase { fun invoke() }` and `interface DemoManager` (members unchanged); `jvmMain` `LogoutUseCaseImpl`, `AndroidAccessTokenProvider`, `DataStoreDemoManager` keep their classes, visibility and annotations.
- On native, after this task nothing binds `AuthTokenStorage`, `DemoManager` or `LogoutUseCase`; Task 9 does.

- [ ] **Step 1: Move commits.** The four `git mv`s, one commit, R100 each.
- [ ] **Step 2: Edit commit.** Carve the interfaces into the new `commonMain` files, rename the storage class, add its `@ContributesBinding`, move the build dependencies, update the four test files.
- [ ] **Step 3: Tests.** `./gradlew :auth-core-public:jvmTest :core-demo-mode:jvmTest :app-shell:jvmTest`. Expected: PASS, same counts as base.
- [ ] **Step 4: Proof.** P1, P2, P3, P4 for `auth-core-public` and `core-demo-mode`. Expected graph difference: the `AuthTokenStorage` key is satisfied by a binding to `DataStoreAuthTokenStorage` where it was `AuthTokenStorage`'s own constructor; references to `AuthTokenStorage`'s generated factory become `DataStoreAuthTokenStorage`'s; every other key and class unchanged; 87 / 21 / 111.
- [ ] **Step 5: Device check (owner).** Install the debug build on the Pixel; the owner logs in by BankID, the agent force-stops and relaunches (`adb shell am force-stop com.hedvig.dev.app`, then launch), the app is still logged in; the owner logs out. Record the result.
- [ ] **Step 6: Commit.**

---

### Task 4: Guard against Compose UI in shared `commonMain`

Design A, step 0 item 3. Parallel with Tasks 2 and 3. No module opts in here; Task 5 is the first.

**Files:**
- Modify: `build-logic/convention/src/main/kotlin/com/hedvig/android/CommonMainImportGuard.kt` (`forbiddenEntryOf` and `findForbiddenImports` take a `forbiddenNames: List<String> = forbiddenCommonMainNames` parameter; defaults keep today's behaviour)
- Create: `build-logic/convention/src/main/kotlin/com/hedvig/android/SharedCommonMainGuard.kt`
- Modify: `build-logic/convention/src/main/kotlin/com/hedvig/android/HedvigGradlePluginExtension.kt` (new `sharedWithIos()`; `ViewModelsHandler.configure` places `metro.viewmodel.compose`)
- Test: `build-logic/convention/src/test/kotlin/com/hedvig/android/SharedCommonMainGuardTest.kt`

**Interfaces:**
- Produces, in `SharedCommonMainGuard.kt`:
  - `val forbiddenSharedCommonMainNames: List<String>` = `androidx.compose.ui`, `androidx.compose.foundation`, `androidx.compose.material`, `androidx.compose.material3`, `com.hedvig.android.design.system`, `hedvig.resources`, `org.jetbrains.compose.resources`, `coil`, `coil3`, `androidx.paging.compose`, `androidx.navigation3.ui`
  - `fun isForbiddenSharedCommonMainDependency(dependency: String, projectDirectory: String? = null): Boolean`, where `dependency` is a project path (`:design-system-hedvig`) or `group:name`, and `projectDirectory` is a project dependency's directory relative to the root (captured at configuration time). Forbidden: project paths `:core-resources`, `:navigation-compose`, `:compose-ui`, `:core-icons`, `:placeholder`, `:pullrefresh`, any starting with `:design-system-`, and any project whose directory is under `app/ui/`; external groups `org.jetbrains.compose.ui`, `org.jetbrains.compose.foundation`, `org.jetbrains.compose.material`, `org.jetbrains.compose.material3`, `org.jetbrains.compose.components`, `io.coil-kt`, `io.coil-kt.coil3`, and modules `androidx.paging:paging-compose`, `org.jetbrains.androidx.navigation3:navigation3-ui`.
  - A per-module task named `checkCommonMainImports` (same name as the root task, so the house command `./gradlew checkCommonMainImports` runs it), registered by `sharedWithIos()`: fails with one line per violation, `<path>:<line>: <import>` for imports in `src/commonMain/**/*.kt` and `commonMain declares <dependency>` for declared `commonMainImplementation` and `commonMainApi` dependencies. Configuration-cache compatible (declared dependencies captured at configuration time as strings). The module's `check` depends on it.
- `HedvigGradlePluginExtension.sharedWithIos()`: registers that task; fails the configuration with a `GradleException` if the module has no `jvmAndAndroidMain` source set, or if `viewModels()` was already called ("call sharedWithIos() before viewModels()"). For an opted module, `viewModels()` adds `metro.viewmodel.compose` to `jvmAndAndroidMainImplementation`; `metro.viewmodel` stays in `commonMainImplementation`. For every other module `viewModels()` is unchanged.

- [ ] **Step 1: Write the failing tests** in `SharedCommonMainGuardTest`:
  - `flags Compose UI, design system, resources, Coil, paging and nav3 UI imports`: a source with imports of `androidx.compose.ui.Modifier`, `androidx.compose.foundation.layout.Column`, `androidx.compose.material3.Text`, `com.hedvig.android.design.system.hedvig.HedvigText`, `hedvig.resources.Res`, `org.jetbrains.compose.resources.StringResource`, `coil3.compose.AsyncImage`, `androidx.paging.compose.LazyPagingItems`, `androidx.navigation3.ui.NavDisplay`, `androidx.compose.runtime.Composable`, `androidx.lifecycle.ViewModel`, `com.hedvig.android.molecule.public.MoleculeViewModel`; `findForbiddenImports(source, forbiddenNames = forbiddenSharedCommonMainNames)` returns exactly the first nine with their line numbers.
  - `flags UI dependencies and allows the shared layer`: `isForbiddenSharedCommonMainDependency` is true for `:design-system-hedvig`, `:core-resources`, `:navigation-compose`, `org.jetbrains.compose.ui:ui-tooling-preview`, `org.jetbrains.compose.components:components-resources`, `io.coil-kt.coil3:coil-compose`, `androidx.paging:paging-compose`, and `:cross-sells` with directory `app/ui/cross-sells`; false for `:molecule-public`, `:apollo-core`, `:core-common-public`, `:navigation-common`, `org.jetbrains.compose.runtime:runtime`, `com.apollographql.apollo:apollo-runtime`, `androidx.compose:compose-bom`.
  - Every existing `CommonMainImportGuardTest` case still passes unchanged.
- [ ] **Step 2: Run** `./gradlew -p build-logic :convention:test`. Expected: FAIL, unresolved references.
- [ ] **Step 3: Implement** the list, the predicate, the parameterised lookup, the task and `sharedWithIos()`.
- [ ] **Step 4: Run** `./gradlew -p build-logic :convention:test`. Expected: PASS.
- [ ] **Step 5: Negative check, not committed.** Add `sharedWithIos()` before `viewModels()` in `app/feature/feature-help-center/build.gradle.kts` (it has UI in `commonMain`); `./gradlew :feature-help-center:checkCommonMainImports` must FAIL listing its UI imports and dependencies; `./gradlew :feature-help-center:dependencies --configuration jvmAndAndroidMainImplementation` lists `metrox-viewmodel-compose` and `commonMainImplementation` does not. Swap the order of the two calls: configuration fails with the ordering message. `git checkout -- app/feature/feature-help-center/build.gradle.kts`.
- [ ] **Step 6: Proof.** `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` identical to base; P1; P2 (expected graph difference: none).
- [ ] **Step 7: Commit.**

---

## Step 1: `feature-claim-history` for iOS

### Task 5: Split the module by source set, add iOS targets and the partner flag

Design A step 1, E (partner flag). After Task 4. Parallel with Tasks 7 and 8.

**Files:**
- Modify: `app/feature/feature-claim-history/build.gradle.kts`
- Move: `app/feature/feature-claim-history/src/commonMain/kotlin/com/hedvig/android/feature/claimhistory/ClaimHistoryDestination.kt` and `.../claimhistory/nav/ClaimHistoryDestination.kt` to the same paths under `src/jvmAndAndroidMain/kotlin/`
- Modify: `app/feature/feature-claim-history/src/commonMain/kotlin/com/hedvig/android/feature/claimhistory/GetClaimsHistoryUseCase.kt`
- Modify: the moved `ClaimHistoryDestination.kt` (preview data gains `isPartnerClaim = false`)
- Test: `app/feature/feature-claim-history/src/commonTest/kotlin/com/hedvig/android/feature/claimhistory/GetClaimsHistoryUseCaseTest.kt`

**Interfaces:**
- Consumes: `sharedWithIos()` (Task 4).
- Produces: `internal data class ClaimHistory(val id: String, val claimType: String?, val submittedAt: Instant?, val outcome: ClaimOutcome?, val isPartnerClaim: Boolean)`; regular claims `false`, partner claims `true`. `ClaimHistoryKey` and `claimHistoryEntries` keep package, name and signature, now in `jvmAndAndroidMain`. The module compiles for `iosArm64` and `iosSimulatorArm64`.
- Build file shape: plugins `hedvig.multiplatform.library.ios`, `hedvig.multiplatform.library.android`, `hedvig.gradle.plugin`; `hedvig { apollo("octopus"); serialization(); compose(); navKeys(); sharedWithIos(); viewModels() }`. `commonMain`: `apollo.api`, `apollo.runtime`, `arrow.core`, `jetbrains.compose.runtime`, `apolloCore`, `apolloOctopusPublic`, `coreCommonPublic`, `moleculePublic` (add `kotlinx.datetime` if the iOS compile does not see it transitively). `jvmAndAndroidMain`: `jetbrains.compose.ui.tooling.preview`, `kotlinx.serialization.core`, `coreResources`, `designSystemHedvig`, `navigationCommon`, `navigationCompose`. `commonTest`: `assertK`, `coroutines.test`, `ktor.client.mock`, `apollo.engine.ktor`.

- [ ] **Step 1: Write the failing test** `GetClaimsHistoryUseCaseTest`, an `ApolloClient` with `.ktorClient(HttpClient(MockEngine { respond(json, OK, contentType JSON) }))` and server URL `https://example.test/graphql`:
  - `regular and partner claims are merged newest first and carry the partner flag`: one claim (`submittedAt` `2026-10-01T10:00:00Z`, outcome `PAID`) and one partner claim (`submittedAt` `2026-10-05`); expect `Right` with ids `[partner-1, claim-1]`, `isPartnerClaim` `[true, false]`, outcomes `[UNKNOWN, PAID]`.
  - `a GraphQL error is a Left`: a body with `errors` and no `data`; expect `Left`.
  The JSON holds every non-null field of `ClaimFragment` and `PartnerClaimFragment` (`app/apollo/apollo-octopus-public/src/commonMain/graphql/com/hedvig/android/apollo/octopus/graphql/`); nullable fields are `null`.
- [ ] **Step 2: Run** `./gradlew :feature-claim-history:jvmTest`. Expected: FAIL, `isPartnerClaim` unresolved.
- [ ] **Step 3: Move commit.** The two `git mv`s alone.
- [ ] **Step 4: Edit commit.** Build file, the field, the preview data.
- [ ] **Step 5: Run** `./gradlew :feature-claim-history:jvmTest :feature-claim-history:checkCommonMainImports`. Expected: PASS.
- [ ] **Step 6: Spike, KSP metadata pass with UI in `jvmAndAndroidMain`.** Run P4 for `feature-claim-history`. Decision rule:
  - Passes: record "metadata pass works with the split".
  - `kspCommonMainKotlinMetadata` fails to resolve a constructor type of `ClaimHistoryViewModel`: the type's module lacks iOS or is only in `jvmAndAndroidMain`; fix the dependency placement in the build file.
  - The processor or the generated code fails for a reason in `ViewModelsHandler`: fix it there if the fix is under about 30 lines and changes nothing for modules without `sharedWithIos()` (prove with Task 4 step 6's dependency report); otherwise stop and report the error (design "Risks").
- [ ] **Step 7: Proof.** P1 (count: base plus the two new tests, run on JVM), P2 (expected graph difference: none; `ClaimHistoryKey` still among the 111), P3 (serialization moved source set), `./gradlew :app:test --tests "*SerializationTest*" --tests "*AnalyticsNameTest*"`. Owner's eyes, optional: `./gradlew :app-desktop:run` and open claim history with the desktop's stored session.
- [ ] **Step 8: Commit.**

---

### Task 6: Public facade in `nativeMain` (Phase A)

Design E. After Task 5. Parallel with Tasks 7 and 8.

**Phase A deliverable** (`$RUN/task-6-proposal.md`): the facade's Kotlin declarations as below or amended with reasons; how `observe` delivers on the main thread and what it does after `close()`; whether `NSDate?` resolves in `nativeMain` (all native targets here are Apple, so the platform libraries are commonised; check by compiling a one-line probe, not committed) and, if not, the fallback `submittedAtEpochMillis: Long?`; the Swift reading of each declaration as it will appear (from the plain Objective-C export rules).

**Files:**
- Create: `app/feature/feature-claim-history/src/nativeMain/kotlin/com/hedvig/android/feature/claimhistory/ios/ClaimHistoryScreenModel.kt`
- Create: `app/feature/feature-claim-history/src/nativeMain/kotlin/com/hedvig/android/feature/claimhistory/ios/ClaimHistoryScreenState.kt`
- Modify: `app/feature/feature-claim-history/build.gradle.kts` (`nativeMain` dependencies if any are needed beyond `commonMain`)

**Interfaces:**
- Consumes: `ClaimHistoryViewModel`, `ClaimHistoryUiState`, `ClaimHistoryEvent.Reload`, `ClaimHistory` (internal, same module); `dev.zacsweers.metrox.viewmodel.MetroViewModelFactory`.
- Produces, package `com.hedvig.android.feature.claimhistory.ios`:

```kotlin
class ClaimHistoryScreenModel(viewModelFactory: MetroViewModelFactory) {
  val currentState: ClaimHistoryScreenState
  fun observe(onState: (ClaimHistoryScreenState) -> Unit): StateSubscription
  fun reload()
  fun close()
}
class StateSubscription { fun cancel() }
sealed interface ClaimHistoryScreenState {
  data object Loading : ClaimHistoryScreenState
  data object Empty : ClaimHistoryScreenState
  data object Failed : ClaimHistoryScreenState
  data class Content(val items: List<ClaimHistoryItem>) : ClaimHistoryScreenState
}
class ClaimHistoryItem(
  val id: String,
  val claimType: String?,
  val submittedAt: NSDate?,
  val outcome: ClaimHistoryOutcome,
  val isPartnerClaim: Boolean,
)
enum class ClaimHistoryOutcome { Paid, NotCompensated, NotCovered, Unresponsive, Unknown }
```

- Behaviour: the model owns a `ViewModelStore` and gets the ViewModel with `ViewModelProvider.create(store, viewModelFactory)[ClaimHistoryViewModel::class]`; it registers `addCloseable` on the ViewModel that logs `ClaimHistoryViewModel cleared` through `logcat`. `observe` collects `uiState` on `Dispatchers.Main` and calls `onState` with the mapped state (`NoHistory` to `Empty`, `Error` to `Failed`, outcome `null` to `Unknown`). `reload()` emits `Reload`; an `IllegalStateException` from a full event buffer is caught and logged at WARN, never thrown to Swift. `close()` cancels every subscription and clears the store; calls after `close()` are no-ops. No Arrow, Apollo or kotlinx-datetime type in any public signature.

- [ ] **Step 1: Phase A proposal**, then stop for review.
- [ ] **Step 2: Implement** the two files.
- [ ] **Step 3: Proof.** P4 for `feature-claim-history`; P1 (no Android source changed, but the build file did); `:feature-claim-history:checkCommonMainImports`.
- [ ] **Step 4: Commit.** Runtime tests of this facade are Task 10's (owner decision 14 puts the iOS simulator test with the shell).

---

## Step 2: native graph

### Task 7: Native interim defaults and header parity

Design G. Parallel with Tasks 5, 6, 8.

**Files:**
- Move: `app/network/network-clients/src/mobileMain/kotlin/com/hedvig/android/network/clients/di/NetworkMetroProviders.mobile.kt` to `app/network/network-clients/src/androidMain/kotlin/com/hedvig/android/network/clients/di/DatadogKtorPlugin.android.kt`
- Create: `app/network/network-clients/src/nativeMain/kotlin/com/hedvig/android/network/clients/di/DatadogKtorPlugin.native.kt` (no-op actual of `installDatadogKtorPlugin`)
- Modify: `app/network/network-clients/build.gradle.kts` (remove the `mobile` group; `datadog.sdk.ktor` and `projects.datadogCore` move to `androidMain`)
- Create: `app/network/network-clients/src/commonMain/kotlin/com/hedvig/android/network/clients/apollo/UnauthenticatedLogoutPolicy.kt` (`internal expect val logsOutOnUnauthenticated: Boolean`), actuals in `src/androidMain/.../apollo/UnauthenticatedLogoutPolicy.android.kt` (`true`), `src/jvmMain/...jvm.kt` (`true`), `src/nativeMain/...native.kt` (`false`)
- Modify: `app/network/network-clients/src/commonMain/kotlin/com/hedvig/android/network/clients/apollo/LoggingInterceptor.kt:28-33` (add the logout interceptor only when `logsOutOnUnauthenticated`)
- Create: `app/network/network-clients/src/commonMain/kotlin/com/hedvig/android/network/clients/di/PlatformHeaders.kt` (`internal expect fun HeadersBuilder.appendPlatformHeaders()`), no-op actuals in `androidMain` and `jvmMain`, `nativeMain` appends `Hedvig-TimeZone` = `TimeZone.currentSystemDefault().id`; call it at the end of `commonHeaders` in `NetworkMetroProviders.kt`
- Create: `app/core/core-build-constants/src/commonMain/kotlin/com/hedvig/android/core/buildconstants/UserAgent.kt` (`internal expect fun platformUserAgent(languageBCP47: String, appBuildConfig: AppBuildConfig): String`); `src/jvmAndAndroidMain/.../UserAgent.jvmAndAndroid.kt` holds today's `makeUserAgent` body unchanged; `src/nativeMain/.../UserAgent.native.kt` returns `"<applicationId> <versionName> (iOS <osReleaseVersion>)"`
- Modify: `app/core/core-build-constants/src/commonMain/kotlin/com/hedvig/android/core/buildconstants/CommonHedvigBuildConstants.kt` (`userAgent` calls `platformUserAgent`; private `makeUserAgent` removed)
- Test: `app/network/network-clients/src/nativeTest/kotlin/com/hedvig/android/network/clients/apollo/NativeInterceptorsTest.kt`, `app/core/core-build-constants/src/nativeTest/kotlin/com/hedvig/android/core/buildconstants/NativeUserAgentTest.kt`

**Interfaces:**
- Produces: on native, the Apollo chain is `[LoggingInterceptor]` and no Datadog Ktor plugin is installed; `Hedvig-TimeZone` is sent; `User-Agent` matches iOS. On Android and JVM all three are unchanged.

- [ ] **Step 1: Write the failing native tests.**
  - `NativeInterceptorsTest.on native only the logging interceptor is added`: `ApolloClient.Builder().serverUrl("https://example.test").addHedvigInterceptors(fakeAuthTokenService, fakeDemoManager).build().interceptors.map { it::class.simpleName }` equals `["LoggingInterceptor"]` (fakes written inline in the test file).
  - `NativeUserAgentTest.user agent matches the iOS format`: an `AppBuildConfig` with `applicationId = "com.hedvig.test"`, `versionName = "14.6.3"`, `osReleaseVersion = "26.4"`; `platformUserAgent("en-SE", config)` equals `"com.hedvig.test 14.6.3 (iOS 26.4)"`.
- [ ] **Step 2: Run** `./gradlew :network-clients:iosSimulatorArm64Test :core-build-constants:iosSimulatorArm64Test`. Expected: FAIL (compile errors on the new declarations).
- [ ] **Step 3: Move commit** (the Datadog file), then the edit commit.
- [ ] **Step 4: Run** the same command. Expected: PASS.
- [ ] **Step 5: Spike, `apollographql-client-name` on iOS.** Find Apollo iOS sources in a resolved Swift package checkout (`find ~/Library/Developer/Xcode/DerivedData -path '*SourcePackages/checkouts/apollo-ios*' -name '*.swift' | xargs grep -ln 'apollographql-client'`). Decision rule: header names and the `iOS:<bundleId>` value confirmed: add `internal expect fun platformClientName(appPackageId: String): String` (Android and JVM return `appPackageId`, native returns `"iOS:$appPackageId"`) with a native test pinning it. Not confirmable from source: keep Kotlin's value and mark the row "unverified" in `parity.md`.
- [ ] **Step 6: Proof.** P1 (the existing `ApolloInterceptorsTest`, `CommonHedvigBuildConstantsTest` and `DesktopGraphTest` interceptor order pass unchanged), P2 (expected graph difference: none), P4 for both modules. Update `parity.md` rows `User-Agent`, `Hedvig-TimeZone`, `Logout on UNAUTHENTICATED`, `Datadog tracing`, `apollographql-client-name` with "implemented, verify in Task 12".
- [ ] **Step 7: Commit.**

---

### Task 8: `apollo-auth-listeners` compiles for iOS

Design F. Parallel with Tasks 5, 6, 7.

**Files:**
- Modify: `app/apollo/apollo-auth-listeners/build.gradle.kts` (plugins `hedvig.multiplatform.library.ios`, `hedvig.gradle.plugin`; the five dependencies into `commonMain`)
- Move: `app/apollo/apollo-auth-listeners/src/main/kotlin` to `app/apollo/apollo-auth-listeners/src/commonMain/kotlin`

**Interfaces:**
- Produces: `ApolloNormalizedCacheAuthEventListener` (unchanged, `@ContributesIntoSet(AppScope::class)`) available to native graphs.

- [ ] **Step 1: Move commit**, then the build file commit.
- [ ] **Step 2: Proof.** P4 for `apollo-auth-listeners`; P1; P2 (expected graph difference: none; the listener is still a member of the Android `Set<AuthEventListener>`); P3.
- [ ] **Step 3: Commit.** Add `.android` only if `:app` stops resolving the module, and say so.

---

### Task 9: Native Metro graph in `:umbrella` (Phase A)

Design F. After Tasks 3, 6, 7, 8. Runs alone among building tasks that touch `:umbrella`.

**Phase A deliverable** (`$RUN/task-9-proposal.md`):
- An inventory of every binding the graph needs, produced by compiling a scratch `IosGraph` that exposes only `metroViewModelFactory` (scratch commit, not kept) and reading Metro's missing-binding errors. For each: who supplies it (graph provider, existing module contribution, new `nativeMain` adapter, Swift input) and why.
- The Swift inputs, each with its Kotlin interface and signature, applying design F's rule that Swift never produces a Kotlin `Flow`. `FeatureManager` and `EventTrackingClient` appear only if the inventory shows something reached injects them.
- The export list, decided by building the debug framework with only `feature-claim-history` added and reading `HedvigShared.h`: an input interface that appears in the header with its members through `createIosGraphHolder`'s signature is not exported; one that does not is exported, and its module named.
- `commonMain` contributions that reach this graph and should not (design F, recon section 5), with what happens to each.

**Files:**
- Modify: `app/umbrella/build.gradle.kts` (`projects.featureClaimHistory` joins `projectsToExport`; the graph's dependencies in `nativeMain`)
- Create: `app/umbrella/src/nativeMain/kotlin/com/hedvig/android/umbrella/IosGraph.kt`, `IosGraphHolder.kt`, `IosViewModelFactory.kt`, `IosMetroProviders.kt`
- Move: `app/auth/auth-core-public/src/jvmMain/kotlin/com/hedvig/android/auth/di/AuthMetroProviders.kt` to `src/commonMain/kotlin/com/hedvig/android/auth/di/AuthMetroProviders.kt` (one provider for every platform; the engine it takes is bound per platform)
- Create: `app/auth/auth-core-public/src/nativeMain/kotlin/com/hedvig/android/auth/storage/IosTokenStore.kt`, `NativeAuthTokenStorage.kt`
- Create: `app/core/core-demo-mode/src/nativeMain/kotlin/com/hedvig/android/core/demomode/IosDemoMode.kt`, `NativeDemoManager.kt`
- Create: `app/auth/auth-core-public/src/nativeMain/kotlin/com/hedvig/android/auth/NativeLogoutUseCase.kt` only if the inventory shows `LogoutUseCase` is reached

**Interfaces:**
- Consumes: `AuthTokenStorage`, `DemoManager` interfaces (Task 3); `ClaimHistoryScreenModel` (Task 6); native defaults (Task 7); the listener (Task 8).
- Produces (names proposed; Phase A may amend, and the orchestrator then updates Tasks 10 to 19):

```kotlin
// auth-core-public nativeMain, package com.hedvig.android.auth.storage
interface IosTokenStore {
  suspend fun read(): IosStoredTokens?
  suspend fun write(tokens: IosStoredTokens)
  suspend fun clear()
}
class IosStoredTokens(
  val accessToken: String,
  val accessTokenExpiresAtEpochSeconds: Long,
  val refreshToken: String,
  val refreshTokenExpiresAtEpochSeconds: Long,
)
// core-demo-mode nativeMain, package com.hedvig.android.core.demomode
interface IosDemoMode {
  fun isDemoMode(): Boolean
  fun setDemoMode(demoMode: Boolean)
}
// umbrella nativeMain, package com.hedvig.android.umbrella
fun createIosGraphHolder(
  appBuildConfig: AppBuildConfig,
  accessTokenFetcher: AccessTokenFetcher,
  tokenStore: IosTokenStore,
  deviceIdFetcher: DeviceIdFetcher,
  languageStorage: LanguageStorage,
  demoMode: IosDemoMode,
): IosGraphHolder
class IosGraphHolder {
  fun claimHistoryScreenModel(): ClaimHistoryScreenModel
  fun sessionChanged(tokens: SessionTokens?)
}
class SessionTokens(val accessToken: String, val refreshToken: String)
```

- `IosGraph`: `@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class]) internal interface IosGraph : ViewModelGraph`, with `val initializables: Set<Initializable>`, `val authEventStorage: AuthEventStorage`, and for Task 10's tests `val authEventListeners: Set<AuthEventListener>`, `val apolloClient: ApolloClient`, `val memberIdService: MemberIdService`; `IosGraphHolder` keeps it as `internal val graph: IosGraph`; a `@DependencyGraph.Factory` taking the inputs as `@Provides` parameters. `IosViewModelFactory` is `DesktopViewModelFactory`'s shape (`app/app-desktop/src/jvmMain/kotlin/com/hedvig/android/desktop/DesktopBindings.kt`) in `nativeMain`. `IosMetroProviders` provides `kotlin.time.Clock` (`Clock.System`), `TimeZone` (`currentSystemDefault()`) and `@AuthHttpClientEngine HttpClientEngine` (`Darwin.create()`).
- `createIosGraphHolder` creates the graph with `createGraphFactory<IosGraph.Factory>()`, runs every `Initializable` before returning, and never asks Swift to call them.
- `sessionChanged(tokens)`: non-null calls `AuthEventStorage.loggedIn(accessToken, refreshToken)`, null calls `loggedOut()`; both make `NativeAuthTokenStorage` re-read Swift's store. It never writes tokens.
- `NativeAuthTokenStorage(store: IosTokenStore, clock: Clock) : AuthTokenStorage`, `@Inject @SingleIn(AppScope::class) @ContributesBinding(AppScope::class)`: `getTokens()` re-reads `store.read()` on collection and after every write, clear or `sessionChanged`; `updateTokens` converts `expiryInSeconds` to absolute epoch seconds with `clock`. `NativeDemoManager(demoMode: IosDemoMode) : DemoManager`, bound the same way: `isDemoMode()` re-reads on collection and after `setDemoMode` and `sessionChanged`, `distinctUntilChanged`.
- `AccessTokenFetcher` comes from Swift; `AndroidAccessTokenProvider` is not bound on native. `AuthTokenServiceImpl` is bound over `NativeAuthTokenStorage`. No `Backstack`, deep link matcher, `ImageLoader` or serializers module in the graph.

- [ ] **Step 1: Phase A proposal**, then stop for review.
- [ ] **Step 2: Move commit** (`AuthMetroProviders.kt`), then the edit commits.
- [ ] **Step 3: Link.** `./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64`. Expected: BUILD SUCCESSFUL. A missing binding is a compile error: supply it as the proposal says, or return to the orchestrator if the proposal did not foresee it.
- [ ] **Step 4: Header check.** In `app/umbrella/build/bin/iosSimulatorArm64/debugFramework/HedvigShared.framework/Headers/HedvigShared.h`: `ClaimHistoryScreenModel`, `ClaimHistoryScreenState`, `IosGraphHolder`, `createIosGraphHolder` and every input protocol with its members are present; `grep -ciE 'compose.?ui|skiko|DesignSystem|ClaimHistoryKey'` prints 0. Record the header line count.
- [ ] **Step 5: Proof.** P1, P2 (expected graph difference: none; `AuthMetroProviders` is the same contribution from another source set), P3, P4 for `auth-core-public`, `core-demo-mode`, `feature-claim-history`, P5 (this is the first task where the umbrella links the data layer: call out the delta per Global Constraints).
- [ ] **Step 6: Commit.**

---

## Step 3: shell and smoke test

### Task 10: iOS simulator smoke test

Design K, owner decision 14. After Task 9. Parallel with Task 11.

Clarification of K, decided here: the graph lives in `:umbrella`, which `feature-claim-history` cannot see, and the ViewModel and use case are `internal` to `feature-claim-history`, which `:umbrella` cannot construct. So K splits by what each module can see: the facade and ViewModel test in `feature-claim-history` (friend of its internals) and the graph test in `:umbrella`. Neither joins the `test` lifecycle task; both are run by name on macOS.

**Files:**
- Test: `app/feature/feature-claim-history/src/nativeTest/kotlin/com/hedvig/android/feature/claimhistory/ios/ClaimHistoryScreenModelTest.kt`
- Test: `app/umbrella/src/nativeTest/kotlin/com/hedvig/android/umbrella/IosGraphTest.kt`
- Modify: both modules' `build.gradle.kts` (`nativeTest` or `commonTest` dependencies: `coroutines.test`, `ktor.client.mock`, `apollo.engine.ktor`, `assertK`)

**Interfaces:**
- Consumes: Tasks 6 and 9. The facade test builds `ClaimHistoryScreenModel` over a test `MetroViewModelFactory` whose `viewModelProviders` map `ClaimHistoryViewModel::class` to a ViewModel over `GetClaimsHistoryUseCase(apolloClient)`, `apolloClient` on a `MockEngine` that counts requests and answers from a queue (Task 5's JSON).

- [ ] **Step 1: Spike, can the presenter run in a K/N test binary?** `MoleculeViewModel` runs on `Dispatchers.Main + DisplayLinkClock`. Write the first facade test (below) and run `./gradlew :feature-claim-history:iosSimulatorArm64Test`. Decision rule:
  - It emits: keep going.
  - It hangs or times out: drive the main run loop while waiting (`NSRunLoop.mainRunLoop.runUntilDate` in short slices up to 10 s) with `Dispatchers.setMain` left alone. If that emits, keep that helper in the test file.
  - Still nothing: the display link does not tick in the test process. Record that as a finding, keep only the tests that need no emission (construction, `close()` idempotence, the reload overflow test), and move the emission checks to Task 12's manual list. Do not change `molecule-public` or the ViewModel for the test.
- [ ] **Step 2: Write the facade tests** in `ClaimHistoryScreenModelTest`:
  - `loading then content`: first observed state `Loading`, then `Content` with ids `[partner-1, claim-1]` and `isPartnerClaim` `[true, false]`.
  - `loading then failed, and reload recovers`: queue an error body then a success body; `Failed`, then after `reload()` `Content`.
  - `empty history is Empty`: both lists empty.
  - `close stops requests`: after the first `Content`, `close()`, then `reload()`; after 6 s (past `WhileSubscribed(5.seconds)`) the request count is unchanged and no further state is delivered.
  - `many reloads before observing do not throw`: 25 `reload()` calls on a fresh model, no exception.
  - `state is delivered on the main thread`: `NSThread.isMainThread` true inside `onState`.
- [ ] **Step 3: Write the graph tests** in `IosGraphTest`, with Kotlin fakes of every input:
  - `the graph builds and resolves the claim history screen model`: `createIosGraphHolder(...)` returns; `claimHistoryScreenModel()` returns; `close()` on it does not throw. No request is made (the fake `AccessTokenFetcher` records calls; none before observing).
  - `the auth listeners include the Apollo cache listener`: the graph's `Set<AuthEventListener>` (exposed on `IosGraph` for the test) contains `ApolloNormalizedCacheAuthEventListener`.
  - `sessionChanged(null) clears the Apollo store`: write a record into the graph's `ApolloClient` store, call `sessionChanged(null)`, wait for the broadcaster, the store is empty.
  - `the token storage reads what Swift stored`: the fake `IosTokenStore` holds tokens; `MemberIdService.getMemberId()` first value is the member id encoded in the fake access token's `sub`.
- [ ] **Step 4: Run** `./gradlew :feature-claim-history:iosSimulatorArm64Test :umbrella:iosSimulatorArm64Test`. Expected: PASS (or the reduced set step 1 allowed, with the finding recorded).
- [ ] **Step 5: Proof.** P1 (the build files changed). Commit.

---

### Task 11: iOS shell: project, inputs, login and logout (Phase A)

Design B. After Task 9. Parallel with Task 10.

**Phase A deliverable** (`$RUN/task-11-proposal.md`): the Swift file list and each type's responsibility; the Keychain wrapper's item attributes; the refresh rule (refresh through `authlib`'s `exchange(RefreshTokenGrant)` when the access token expires within 60 s, one in-flight refresh shared by concurrent callers); how login calls `authlib` (through KMPNativeCoroutinesAsync, exact `1.0.5` like Ugglan, for `observeLoginStatus`); where the graph holder lives; the build settings copied from `micro-apps/umbrella-consumer/umbrella-consumer.xcodeproj/project.pbxproj` and the ones changed.

`micro-apps/umbrella-consumer`'s Swift conformers are a starting point; its hardcoded access token is never copied. Decided here: no demo switch. Claim history has no Kotlin demo implementation and native logout on `UNAUTHENTICATED` is off, so a switch would change nothing on screen. The shell's strings are English literals in Swift.

**Files:**
- Create: `app/app-ios/HedvigIosShell.xcodeproj/project.pbxproj` (`objectVersion = 77`), `app/app-ios/HedvigIosShell.xcodeproj/xcshareddata/xcschemes/HedvigIosShell.xcscheme` (shared, so `xcodebuild -scheme` finds it)
- Create: `app/app-ios/HedvigIosShell/HedvigIosShellApp.swift`, `RootView.swift`, `Kotlin/ShellGraph.swift` (builds the holder once), `Kotlin/ShellInputs.swift` (`AppBuildConfig`: staging flavour, `platformName = "ios"`, bundle id `com.hedvig.iosshell`, version from `Bundle.main`, `osReleaseVersion` from `UIDevice`; `DeviceIdFetcher`: a UUID kept in `UserDefaults`; `LanguageStorage`: fixed `en-SE`; `IosDemoMode`: always false), `Auth/KeychainTokenStore.swift` (`IosTokenStore` over the Keychain), `Auth/ShellAccessTokenFetcher.swift`, `Auth/LoginModel.swift`, `Auth/LoginView.swift`, `Auth/QrCodeImage.swift` (Core Image `CIQRCodeGenerator`), `Info.plist`, `Assets.xcassets`
- Create: `app/app-ios/.gitignore` (`xcuserdata/`, `build/`)

**Interfaces:**
- Consumes: `createIosGraphHolder`, `IosGraphHolder.sessionChanged`, `IosTokenStore`, `IosStoredTokens`, `SessionTokens` (Task 9); `NetworkAuthRepository`, `startLoginAttempt`, `observeLoginStatus`, `exchange` (authlib, as exported today); `IosLogcatLogger.companion.install()`.
- Produces: a signed simulator app `com.hedvig.iosshell` that logs in by QR, keeps the session across relaunch, logs out, and calls `sessionChanged` on login (with the exchanged tokens) and on logout (`nil`). `RootView` has a `NavigationStack` with a "Claim history" row that Task 12 fills in.
- Build phase: Run Script before Compile Sources, `cd "$SRCROOT/../.."`, `export JAVA_HOME="$(/usr/libexec/java_home -v 21)"`, `./gradlew :umbrella:embedAndSignAppleFrameworkForXcode`, keeping umbrella-consumer's `OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED` guard and `ENABLE_USER_SCRIPT_SANDBOXING = NO`. Signing: automatic style, no team, "Sign to Run Locally" for the simulator. If the link cannot find `HedvigShared`, add `FRAMEWORK_SEARCH_PATHS = $(SRCROOT)/../umbrella/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)` and `OTHER_LDFLAGS = -framework HedvigShared`.

- [ ] **Step 1: Phase A proposal**, then stop for review.
- [ ] **Step 2: Create the project and the Swift files.**
- [ ] **Step 3: Build.** `xcodebuild -project app/app-ios/HedvigIosShell.xcodeproj -scheme HedvigIosShell -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 17 Pro' -derivedDataPath app/app-ios/build build`. Expected: `** BUILD SUCCEEDED **`. This is the command that proves the shell builds.
- [ ] **Step 4: Install and launch.** `xcrun simctl boot "iPhone 17 Pro"` (an "already booted" error is fine), `xcrun simctl install booted app/app-ios/build/Build/Products/Debug-iphonesimulator/HedvigIosShell.app`, `xcrun simctl launch booted com.hedvig.iosshell`, with `xcrun simctl spawn booted log stream --level debug --predicate 'process == "HedvigIosShell"'` running. Expected: the QR code renders.
- [ ] **Step 5: Login (owner scans).** Tell the orchestrator the QR is up; the owner scans. Expected log: the Keychain write reports `OSStatus 0`. Decision rule: `-34018` means the ad hoc signature lacks the Keychain entitlement; stop and ask the owner whether to set `DEVELOPMENT_TEAM = 4MS87FK8B3` (the team umbrella-consumer uses), which needs the owner's Apple account on this Mac.
- [ ] **Step 6: Relaunch and logout.** `xcrun simctl terminate booted com.hedvig.iosshell`, launch again: still logged in, no new QR. Log out: the QR screen returns and the log shows `sessionChanged` with no tokens.
- [ ] **Step 7: Commit.** No Kotlin changed, so no Gradle proof beyond the build phase.

---

### Task 12: Shell claim history screen and lifetime

Design B, C, goals 1 and 3. After Task 11. Not parallel with Tasks 13 to 15 (they branch from it).

**Files:**
- Create: `app/app-ios/HedvigIosShell/ClaimHistory/ClaimHistoryView.swift`, `ClaimHistory/ClaimHistoryScreenOwner.swift`
- Modify: `app/app-ios/HedvigIosShell/RootView.swift`, `app/app-ios/HedvigIosShell.xcodeproj/project.pbxproj`

**Interfaces:**
- Consumes: `IosGraphHolder.claimHistoryScreenModel()`, `ClaimHistoryScreenModel`, `StateSubscription`, `ClaimHistoryScreenState`, `ClaimHistoryItem` (Tasks 6 and 9).
- Produces: `@MainActor @Observable final class ClaimHistoryScreenOwner` holding one screen model and one subscription, exposing a Swift enum mirror of the state, `reload()`, and an `isolated deinit` that cancels the subscription, calls `close()` and logs `ClaimHistoryScreenOwner deinit`. The view owns it with `@State`, renders Loading, Empty, Failed with a Retry button, and a list (title from `claimType` or "Claim", date from `submittedAt`, outcome, "Partner" when `isPartnerClaim`). This is bridge candidate 1 (plain export).

- [ ] **Step 1: Build and run** as Task 11 steps 3 and 4, logged in.
- [ ] **Step 2: Content and empty.** Open Claim history with the logged-in member; record which state shows and the item count. Ask the owner for a member with no claims and one with partner claims if the current one lacks either (design "Verification logistics"); the owner scans for each.
- [ ] **Step 3: Leaving stops the work (goal 3).** Open the screen, go back, wait 10 s. Expected log, in order: `ClaimHistoryScreenOwner deinit`, `ClaimHistoryViewModel cleared`, and no `ClaimsHistory` request line from the Ktor logger after the deinit. Record the log excerpt.
- [ ] **Step 4: Background check.** Open the screen, tap Retry, then `xcrun simctl launch booted com.apple.Preferences`, wait 10 s, `xcrun simctl launch booted com.hedvig.iosshell`. Record from log timestamps whether the state after Retry arrived during the background or only on return, and that the screen does not stay on Loading.
- [ ] **Step 5: Headers.** From the Ktor log of one request, fill `parity.md`'s "verified" column for every Kotlin-side row (`User-Agent`, `Hedvig-App-Version`, `hedvig-device-id`, `Hedvig-TimeZone`, `Accept-Language`, `hedvig-language`, `X-*`, `apollographql-client-*`, `Authorization` present, no Datadog headers).
- [ ] **Step 6: Owner's eyes.** Error and retry: the owner switches this Mac's network off, opens the screen (Failed), switches it on, taps Retry (Content). Member switch (goal 4): log out, the owner logs in as another member, the list shows only that member's claims.
- [ ] **Step 7: Commit.** The step 3 size row is the orchestrator's (no Kotlin changed in Tasks 10 to 12).

---

## Step 4: bridge comparison

Each candidate is measured on the same base (the integration tip after Task 12), the same simulator and the same criteria, written into `$RUN/bridge-comparison.md` under a heading per candidate:

- **Sealed state and events in Swift**: the Swift `switch` as written, and whether the compiler enforces exhaustiveness.
- **Lifetime**: the Task 12 step 3 log check, plus a weak reference to the screen owner that is `nil` 1 s after pop (logged).
- **Main thread**: `dispatchPrecondition(condition: .onQueue(.main))` inside the state callback does not trap.
- **Kotlin exception**: a probe on the candidate branch only, `fun throwForBridgeCheck()` on `IosGraphHolder`, called from a debug button; record whether the app terminates, and the same with `@Throws(IllegalStateException::class)`.
- **Build time**: median of three `./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64 --rerun` runs, and of three incremental `xcodebuild` runs after touching `ClaimHistoryView.swift`.
- **Size**: P5 shipped delta against Task 12's row.
- **Kotlin-side code per screen**: lines of facade and adapter code, annotations, and `grep -c ClaimHistory` in `HedvigShared.h`.

### Task 13: Candidate 1, plain Objective-C export

After Task 12. Parallel with Task 14. Branch pushed, not merged (the probe stays off the integration branch).

**Files:** the probe in `app/umbrella/src/nativeMain/kotlin/com/hedvig/android/umbrella/IosGraphHolder.kt` and a debug button in `app/app-ios/HedvigIosShell/RootView.swift`, both on this branch only.

- [ ] **Step 1: Add the probe**, build and run as Task 11 steps 3 and 4.
- [ ] **Step 2: Measure every criterion** and write the candidate 1 section with the Swift code that shows each.
- [ ] **Step 3: Commit, push the branch**, record its tip in the ledger.

### Task 14: Candidate 2, KMP-NativeCoroutines

After Task 12. Parallel with Task 13. Branch pushed, not merged.

**Files:**
- Create: `app/umbrella/src/nativeMain/kotlin/com/hedvig/android/umbrella/ClaimHistoryScreenModelFlows.kt`: a class wrapping `ClaimHistoryScreenModel` with `@NativeCoroutinesState val state: StateFlow<ClaimHistoryScreenState>`, so annotations sit on this wrapper only, never in `commonMain` presenters
- Modify: `app/feature/feature-claim-history/src/nativeMain/kotlin/com/hedvig/android/feature/claimhistory/ios/ClaimHistoryScreenModel.kt` (a public `states: StateFlow<ClaimHistoryScreenState>` for the wrapper to read)
- Create: `app/app-ios/HedvigIosShell/ClaimHistory/ClaimHistoryScreenOwnerFlows.swift` (`for try await state in asyncSequence(for: wrapper.stateFlow)` inside a `Task` cancelled in deinit), and the view switched to it

- [ ] **Step 1: Implement, build, run.**
- [ ] **Step 2: Measure every criterion**, plus whether the Swift package `1.0.5` and Kotlin `1.0.6` mismatch builds and behaves (Ugglan ships that pair).
- [ ] **Step 3: Commit, push the branch.**

### Task 15: Candidate 3, SKIE (spike first)

After Tasks 13 and 14 (two building agents at most). Branch pushed, not merged.

- [ ] **Step 1: Availability.** Find the SKIE release notes or compatibility table for Kotlin 2.4.21 (Touchlab's documentation or GitHub releases). Decision rule: no release supports 2.4.21: write "SKIE: not available for Kotlin 2.4.21" with the source and the newest supported Kotlin version into the comparison, and stop the task. Otherwise continue.
- [ ] **Step 2: Apply** `co.touchlab.skie` at that version to `app/umbrella/build.gradle.kts` only. Decision rule: configuration or link fails because of the Kotlin version: record the error, stop. It links: continue.
- [ ] **Step 3: Swift.** `ClaimHistoryScreenOwnerSkie.swift` consuming the `states` flow as an `AsyncSequence` and the state with `switch onEnum(of: state)`. Rewrite the shell's login calls if SKIE changes their shape, and count that as cost.
- [ ] **Step 4: Measure every criterion.**
- [ ] **Step 5: Swift export note.** One paragraph on whether Kotlin 2.4.21's Swift export supports suspend functions, `Flow` and sealed classes, from the Kotlin documentation for 2.4.x, with links. It is not pursued here (design D).
- [ ] **Step 6: Commit, push the branch.**

### Task 16: Bridge recommendation and the step 4 report

After Tasks 13 to 15. No build.

**Files:**
- Create: `docs/superpowers/reports/2026-11-ios-sharing-experiment-report.md` in the docs worktree `/Users/stylianosgakis/hedvig/apps/android/.claude/worktrees/docs-desktop-claude-md`

- [ ] **Step 1: Recommendation.** One section: a table of the criteria by candidate, the recommended bridge with the two or three reasons that decide it, what it costs per screen, and what would change the call (for example: public state types in `commonMain` reading well under one bridge, design E). It is a recommendation to the owner, not a decision.
- [ ] **Step 2: Step 4 edition of the report**, with the sections Task 20 lists, filled as of step 4, and "step 5 not started".
- [ ] **Step 3: Commit** in the docs worktree. Hand the report's path to the orchestrator for the gate.

---

## Step 5: Ugglan

### Task 17: Gate before the Ugglan step (stop and ask)

Run by the orchestrator with the owner. No code. Nothing in step 5 starts until every answer below is recorded verbatim in `$RUN/owner-answers.md`.

- [ ] **Step 1: Ask the open questions** with the report from Task 16: (1) Kotlin logout on `UNAUTHENTICATED` on iOS, routed to Ugglan's force logout, or off? (2) Kotlin Datadog Ktor plugin on iOS, or no Kotlin-side tracing? (3) delete `micro-apps/umbrella-consumer` and its committed token? And which bridge Ugglan uses (plain export until the owner has seen the recommendation and says otherwise). Wait.
- [ ] **Step 2: The `android/` clone.** Show the owner `git -C /Users/stylianosgakis/hedvig/apps/android branch --show-current` and `git -C /Users/stylianosgakis/hedvig/apps/android status --short`. If `android/` is on other work, surface the conflict and stop. Ask for the go-ahead to run `git -C /Users/stylianosgakis/hedvig/apps/android switch -c experiment/ugglan-local-umbrella origin/feat/desktop-app-integration` after Task 18 merges. The integration branch itself cannot be checked out there, since the `desktop-integration` worktree of the same repository holds it.
- [ ] **Step 3: The `ugglan/` clone and Xcode.** Ugglan is on `fix/firebase-user-id-after-consent`. Ask for the go-ahead to create `experiment/kmp-claim-history` in `/Users/stylianosgakis/hedvig/apps/ugglan`, and from which base: `ff89c395e` (the last commit before #2788, buildable on Xcode 26.4.1 with the local `ToolbarButtonView` patch, never committed) or `origin/master` if Xcode 27.2 is installed by then. Ask the owner to close Xcode before Task 19 runs `use-local-umbrella.sh`.
- [ ] **Step 4: Decision.** All answers given and both go-aheads granted: Tasks 18 and 19 proceed. Otherwise the experiment ends after step 4; Task 20 runs with "step 5 not run" and the reason.

### Task 18: Apply the owner's answers on native

After Task 17. Android repo worktree as usual. Each answer's branch below is the whole requirement for it; what ships to Ugglan must be complete on iOS.

**Files:** depending on the answers: `app/network/network-clients/src/nativeMain/kotlin/com/hedvig/android/network/clients/apollo/UnauthenticatedLogoutPolicy.native.kt`, `app/network/network-clients/src/nativeMain/kotlin/com/hedvig/android/network/clients/di/DatadogKtorPlugin.native.kt`, `app/network/network-clients/build.gradle.kts`, `app/umbrella/src/nativeMain/kotlin/com/hedvig/android/umbrella/IosGraph.kt` and `IosGraphHolder.kt`, `micro-apps/umbrella-consumer/`.

- [ ] **Question 1, logout routed to Ugglan:** the native policy becomes `true`, and the native unauthenticated path calls a new Swift input `IosForcedLogout { fun forcedLogout() }` (a `createIosGraphHolder` parameter) instead of clearing Swift's token store, so Ugglan's `forceLogoutHook` owns the logout. Native test: an unauthenticated response calls `forcedLogout()` once and `IosTokenStore.clear()` never. **Off:** nothing changes; the interim default becomes the decision.
- [ ] **Question 2, Kotlin Datadog on iOS:** first read the `dd-sdk-kotlin-multiplatform-ktor3` 1.8.0 metadata (its cinterop of `DatadogCore` and `DatadogCrashReporting`, and the iOS SDK version it is built against) and compare with Ugglan's `dd-sdk-ios` 3.17.0. Decision rule: compatible: restore the plugin on native (the Android file's body in a `nativeMain` actual) and state that Ugglan's App target must link the Datadog frameworks next to the umbrella; not compatible or unknown: stop and report to the owner. **No tracing:** nothing changes.
- [ ] **Question 3, delete:** `git rm -r micro-apps/umbrella-consumer`; tell the owner the token stays in history. **Keep:** nothing.
- [ ] **Proof** for whatever changed: P1, P2 (expected graph difference: none), P4 for `network-clients`, P5. Commit.

### Task 19: Ugglan's claim history on the experiment branch (Phase A)

Design H, goals 2 to 4. After Tasks 17 and 18. Works in `/Users/stylianosgakis/hedvig/apps/ugglan` on `experiment/kmp-claim-history`, local only, never pushed, never merged. Uses the bridge the owner chose in Task 17.

**Phase A deliverable** (`$RUN/task-19-proposal.md`):
- Build feasibility first, before any edit: in `android/`, `git switch -c experiment/ugglan-local-umbrella origin/feat/desktop-app-integration` (the go-ahead from Task 17); in `ugglan/`, create the branch from the agreed base, apply the `ToolbarButtonView` patch if the base needs it, run `scripts/use-local-umbrella.sh`, then build the Ugglan app scheme for the simulator with plain `xcodebuild build` (signed). Decision rule: it does not build for a reason outside this work: the experiment ends at the shell; write the reason and stop.
- The Swift protocol in the Claims module and its Swift-native state; the App-target implementation over the facade; the demo implementation over `FetchClaimClientDemo.getHistoryClaims()`; the DI registration in `Projects/App/Sources/AppDelegate+DI.swift` (non-demo branch the Kotlin one, demo branch the Swift one).
- The Swift inputs: `AccessTokenFetcher` (`TokenRefresher.shared.refreshIfNeeded()`, then the Keychain token), `IosTokenStore` over `KeychainHelper` (service `oAuthorizationToken`, `OAuthorizationToken` dates as epoch seconds), `DeviceIdFetcher` (`UserDefaults` key `hedvig-device-identifier`), `LanguageStorage` (`Localization.Locale`), `IosDemoMode` (`UgglanStore.isDemoMode`; what `setDemoMode(false)` from Kotlin's logout listener does to it), `AppBuildConfig` from `Bundle.main` and `UIDevice`, plus any input Task 18 added.
- The `AppBuildConfig` mapping check (Review Focus 5): for Ugglan's staging and production configurations, the values Kotlin derives for `isProduction`, `urlGraphqlOctopus` and the auth environment; a mismatch for production is listed as a finding with the fix.
- Where `sessionChanged` is called: next to `ApolloClient.handleAuthTokenSuccessResult` in `AuthenticationClientAuthLib.exchange(code:)` (`Projects/App/Sources/Service/OctopusClientsImplementation/AuthenticationClientAuthLib.swift:226`, the login exchange, not the refresh at 245) and next to `ApolloClient.deleteToken()` in `AppDelegate.logout()` (`Projects/App/Sources/AppDelegate.swift:62`).

**Files** (Ugglan, paths relative to `ugglan/`): `Projects/Claims/Sources/Views/ClaimHistoryScreen.swift` (gains the loading and error states it lacks), `Projects/Claims/Sources/ClaimStore.swift` (drop `historyClaims` and `fetchHistoryClaims`; their only other reader is `Projects/Claims/Tests/FetchClaimsTests.swift`, updated), a new protocol and demo file under `Projects/Claims/Sources/Service/`, a new App-target file under `Projects/App/Sources/Service/OctopusClientsImplementation/`, `Projects/App/Sources/AppDelegate+DI.swift`, `Projects/App/Sources/AppDelegate.swift`, `AuthenticationClientAuthLib.swift`, `Projects/Profile/Sources/Navigation/ProfileNavigation.swift` (a tap routes to `ClaimDetailView(claim: nil, type: .claim(id:))` or `.partnerClaim(id:)` by the partner flag).

- [ ] **Step 1: Phase A proposal** (with the feasibility result), then stop for review.
- [ ] **Step 2: Implement.** Never commit a project generation made in local mode, the `.local-umbrella` marker, or the `ToolbarButtonView` patch; never `git add -A`.
- [ ] **Step 3: Build** the Ugglan app scheme for the simulator, signed. Expected: `** BUILD SUCCEEDED **`.
- [ ] **Step 4: The step 3 checks in Ugglan** (owner scans, owner's network toggle): loading, failed and retry, empty, content; leaving the screen clears the ViewModel and no request follows; logout then another member; headers against `parity.md`, including the same `hedvig-device-id` from both clients.
- [ ] **Step 5: Partner route and demo mode.** A partner row opens the partner claim detail; demo mode shows the Swift demo data.
- [ ] **Step 6: Revert the loop.** `scripts/use-released-umbrella.sh` in `ugglan/`; commit the Swift changes locally on the experiment branch. Report the branch tip and that `android/` is on `experiment/ugglan-local-umbrella`, for the owner to switch back.

### Task 20: What the experiment reports

Last. Updates the report Task 16 created; commits in the docs worktree.

- [ ] **Step 1: Write the final edition** of `docs/superpowers/reports/2026-11-ios-sharing-experiment-report.md` with these sections, each from the run directory's records, not from memory:
  - Goals 1 to 7, each with the result and exactly how it was checked (command, log excerpt, or owner observation).
  - Size table: every `size-log.md` row, per-step delta and running total, with the callouts.
  - Bridge recommendation (Task 16), plus what Ugglan used and how it felt there.
  - Client parity table as verified: `parity.md` with the "verified" column, and which rows rest on reading rather than observation.
  - Every skip: what, why sharing it would have made Android's `commonMain` much worse, what completing it on iOS would take.
  - Findings from the design's risk list: the KSP metadata pass (Task 5), the display link in tests and in the background (Tasks 10 and 12), the lifetime per bridge, the deviations this plan made from the design text (Task 3's `AuthTokenServiceImpl`, Task 10's split of K), and anything else that contradicted the design.
  - What was not verified, and why.
- [ ] **Step 2: Check** that every number in the report has its source in `$RUN`, and that the report has no em-dash. Commit.
