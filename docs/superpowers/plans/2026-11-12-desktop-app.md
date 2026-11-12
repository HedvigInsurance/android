# Hedvig App on Desktop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Run the real Hedvig app as a desktop (JVM) application with BankID QR login, by moving every feature into `commonMain` and sharing the app shell between an Android host and a desktop host.

**Architecture:** Android-only modules become KMP modules with JVM and Android targets, one module per PR, behaviour-neutral on Android. The Android-free part of `:app` moves to a shared `:app-shell`; `:app` and a new `:app-desktop` are thin hosts with their own Metro graphs. Platform differences live in per-platform Metro bindings and `expect`/`actual` seams.

**Tech Stack:** Kotlin 2.4.21 Multiplatform, Compose Multiplatform 1.12.2, Navigation 3 1.2.0, Metro 1.5.2, Molecule, Apollo 5.2, Ktor 3.6, Room 2.8.5, AGP 9.

**Spec:** `docs/superpowers/specs/2026-11-12-desktop-app-design.md`. Executors read it before their task.

## Global Constraints

- Android behaviour does not change. No added indirection in feature code whose only purpose is desktop.
- Feature code (screens, presenters, ViewModels, use cases, nav keys) goes to `commonMain`. `jvmAndAndroidMain` is not an acceptable home for it.
- `commonMain` imports nothing from `java.*`, `javax.*`, `android.*`, or Android-only `androidx.*` artifacts. Third-party libraries used from `commonMain` must publish iOS artifacts, otherwise the usage goes behind a seam.
- Bind per platform in Metro. Never put a platform-overridable `@ContributesBinding` default in `commonMain`.
- No module loses its iOS targets. `:umbrella` exports stay `authlib` and `logging-public`.
- Converted modules use `hedvig.multiplatform.library` + `hedvig.multiplatform.library.android`. Module names, package names and class names do not change. Files move with `git mv`.
- Existing unit tests move to `src/jvmTest` unchanged.
- A skipped piece of functionality shows "Not available on desktop". It never crashes and is never a silent no-op. Every skip is reported for the skip register.
- No new Lokalise keys. Desktop-only text is hardcoded English.
- One module per PR, branched off `origin/develop`, opened as a draft with the PR body format from the user's global instructions (two blank lines, then a `<details>` block of at most 1000 characters).
- No em-dash in any prose, comment, commit message or PR text.
- Comments describe current code only, per the Comments section of `CLAUDE.md`. No "moved from", no "for desktop".

## Review Focus

Failure modes the spec implies that a green compile does not catch, most likely first. Each is pinned by a step in the conversion recipe.

1. **A ViewModel silently drops out of the Android graph** after its module becomes KMP (generated contribution not visible to `:app`), crashing at runtime with `Unknown model class`. Expected: every ViewModel present in `AppGraph` before is present after. Pinned by recipe step 7.
2. **A nav key loses its polymorphic serializer** and the app crashes on process-death restore. Expected: every key still round-trips. Pinned by recipe step 6.
3. **A Datadog view name changes** because a key's fully qualified name moved, silently killing RUM metrics. Expected: no package or class rename; `AnalyticsNameTest` passes. Pinned by recipe step 6.
4. **Android-only build inputs get left behind in `src/main`**: `AndroidManifest.xml`, `consumer-rules.pro`, `res/raw`, `res/drawable-nodpi`. Expected: they move to `src/androidMain` and the minified staging build still contains them. Pinned by recipe step 2 and the per-wave staging build in "Orchestration".
5. **`commonMain` quietly reaches JVM-only API**, compiling on desktop and Android but not on iOS. Expected: the import guard fails the build. Pinned by Task 2 and recipe step 8.

---

## Orchestration

Claude (this session) orchestrates. Implementation tasks are dispatched to subagents running Opus 5.5 (`Agent` with `model: "opus"`).

**Concurrency: at most two building agents at a time.** The machine has 32 GB and each Gradle daemon is configured with an 8 GB fixed heap plus a Kotlin daemon, and Android Studio is usually open too. A third concurrent build would swap.

**One worktree per task.** The orchestrator creates it before dispatch:

```bash
git -C /Users/stylianosgakis/hedvig/apps/android fetch origin develop
git -C /Users/stylianosgakis/hedvig/apps/android worktree add -b <branch> .claude/worktrees/<name> origin/develop
cp /Users/stylianosgakis/hedvig/apps/android/{local.properties,lokalise.properties} /Users/stylianosgakis/hedvig/apps/android/.claude/worktrees/<name>/
```

The agent then runs `./gradlew downloadOctopusApolloSchemaFromIntrospection downloadStrings` once in its worktree before anything else.

**Each agent's brief contains:** the task text from this plan, the recipe if it is a conversion, the worktree path, the Global Constraints, and the instruction to read `CLAUDE.md` and the spec first. Agents commit on their branch and do not push.

**Each agent's final report contains:** the commands it ran with their last lines of output, any file left in `androidMain` with the reason, any skip, and anything that contradicted the plan.

**After each agent returns, the orchestrator:** reads the full diff, re-runs the task's verification command itself, pushes the branch, opens the draft PR, and updates the ledger below. A report is a claim, not proof.

**Per wave, before telling the user it is done:** build `./gradlew :app:assembleStaging` on a branch merging that wave's PR branches and smoke it on the Pixel (cold start, log in, open each converted feature).

### Waves

| Wave | Tasks | Runs when |
|---|---|---|
| 0 | Task 1 (spike), Task 2 (guard) | now, in parallel |
| 1 | Tasks 3 to 10 (leaf modules and features with no blocked dependencies) | now, two at a time, alongside wave 0 |
| 2 | Tasks 11 to 13 (modules unblocked by wave 1) | as their dependencies merge |
| Gate | Spike result reviewed with Stylianos | after Task 1 |
| 3 | Tasks 14 to 19 (shell, desktop host) | after the gate, mostly sequential |
| 4 | Tasks 20 to 23 (held and hard features), Task 24 (entries sweep) | after wave 3, two at a time |
| 5 | Task 25 (plain JVM libraries), Task 26 (walkthrough) | last |

**Held modules.** `feature-home`, `feature-insurances`, `feature-onboarding` and `feature-login` are being edited by open draft PRs from another developer (3362, 3347, 3305). Moving every file in those modules would wreck those branches, so they are in wave 4 and each needs a go-ahead from Stylianos at that point.

### Ledger

Maintained by the orchestrator on this branch.

| Task | Branch | PR | Status | Left in `androidMain` | Skips |
|---|---|---|---|---|---|

---

## Conversion Recipe

Every conversion task applies these steps to its module `M` (Gradle path `:M`, directory `D`). The task lists only what is specific to the module.

- [ ] **Step 1: Record the baseline.** Run `./gradlew :M:test` and `./gradlew :app:compileReleaseKotlin`, then save the ViewModel keys currently in the graph:

```bash
javap -p -c 'app/app/build/intermediates/built_in_kotlinc/release/compileReleaseKotlin/classes/com/hedvig/android/app/di/AppGraph$Impl$ActivityRetainedGraphImpl.class' \
  | grep -oE 'com/hedvig/[A-Za-z0-9/]+ViewModel' | sort -u > /tmp/vm-before.txt
```

Expected: tests pass; the file is non-empty. Confirm `:app:compileReleaseKotlin` executed and was not `FROM-CACHE`.

- [ ] **Step 2: Move the sources.** `git mv D/src/main/kotlin D/src/commonMain/kotlin`, `git mv D/src/test D/src/jvmTest` (if present), and `git mv` everything else under `D/src/main` (`AndroidManifest.xml`, `res/`, `graphql/` if the Apollo handler expects it elsewhere, `consumer-rules.pro`) to where `feature-claim-chat` keeps the same kind of file. Commit this move alone, so the rename is a pure rename in history.

- [ ] **Step 3: Rewrite `D/build.gradle.kts`** to the shape of `app/feature/feature-claim-history/build.gradle.kts`: plugins `hedvig.multiplatform.library`, `hedvig.multiplatform.library.android`, `hedvig.gradle.plugin`; the same `hedvig { }` block as before; dependencies split into `commonMain.dependencies`, `androidMain.dependencies`, `jvmTest.dependencies`. Replace `androidx.compose.*` catalog entries with their `jetbrains.compose.*` equivalents in `commonMain`.

- [ ] **Step 4: Make `commonMain` compile.** For each file that fails because of platform API, apply the task's seam instructions. A file that fails only because it needs API that lives in `androidMain` of `:navigation-compose` stays in `androidMain` unchanged and is reported. Run `./gradlew :M:compileKotlinJvm :M:compileAndroidMain`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Run the module's tests.** `./gradlew :M:jvmTest`. Expected: the same tests as step 1, all passing.

- [ ] **Step 6: Run the navigation and analytics guards.** `./gradlew :app:test --tests "*SerializationTest*" --tests "*AnalyticsNameTest*"`. Expected: PASS.

- [ ] **Step 7: Compare the ViewModel graph.** Re-run step 1's `:app:compileReleaseKotlin` and `javap` into `/tmp/vm-after.txt`, then `diff /tmp/vm-before.txt /tmp/vm-after.txt`. Expected: no output.

- [ ] **Step 8: Run the import guard and the Android build.** `./gradlew checkCommonMainImports` (once Task 2 is on `develop`; before that, `grep -rnE '^import (java|javax|android)\.' D/src/commonMain` must print nothing), then `./gradlew :M:ktlintFormat :app:assembleDebug`. Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit.** One commit for the conversion on top of the move commit.

---

## Wave 0

### Task 1: Spike (throwaway)

**Files:** anything, on branch `spike/desktop-app`. Never merged, never opened as a PR.

**Interfaces:**
- Produces: a written finding for each question below, in the agent's final report. Wave 3 tasks consume these answers.

- [ ] **Step 1: Artifact check.** Determine, from Gradle module metadata, whether `androidx.navigation3:navigation3-ui:1.2.0` and `androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0` publish JVM (desktop) and iOS variants, or whether a JetBrains-published coordinate does. Report coordinates and variants.
- [ ] **Step 2: Desktop window.** Create a JVM-only module with a `main()` that opens a Compose Desktop window. Hack freely: copy files, hand-write bindings.
- [ ] **Step 3: Navigation.** Render a `NavDisplay` over a `SnapshotStateList<HedvigNavKey>` using the repo's scene strategies (`DialogSceneStrategy`, `BottomSheetSceneStrategy`, `NavSuiteSceneDecorator`). Report every file from `app/navigation/navigation-compose/src/androidMain` that needed changes to compile on JVM and what the change was.
- [ ] **Step 4: DI.** Declare a Metro `@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])` and resolve one existing `commonMain` ViewModel with `metroViewModel()`. Report every binding that had to be hand-provided because no `jvmMain` binding exists.
- [ ] **Step 5: Login.** Wire the Swedish login screen (copy `feature-login` sources as needed) against staging with `X-Platform`, `User-Agent` and `Hedvig-App-Version` set to the Android values. Persist tokens with DataStore to a file. Stop when the QR code renders, and tell the orchestrator. Stylianos scans it.
- [ ] **Step 6: One authenticated screen.** After login, show one screen of an already-KMP feature that loads member data over GraphQL. Report whether the backend returned data.
- [ ] **Step 7: Why are entries in `androidMain`?** For `feature-claim-chat`'s `ClaimChatEntries.kt`, report the exact compile errors when it is moved to `commonMain`.
- [ ] **Step 8: Unleash.** Report whether the 9 flags in `Feature.kt` can be fetched from the Unleash frontend endpoint the Android SDK uses with a plain Ktor GET, and the size of that code.

**Exit:** steps 1 to 8 answered. If step 3, 4, 5 or 6 cannot be made to work, stop and report instead of working around it.

### Task 2: `commonMain` import guard

**Files:**
- Create: `build-logic/convention/src/main/kotlin/com/hedvig/android/CommonMainImportGuard.kt`
- Modify: `build-logic/convention/src/main/kotlin/HedvigGradlePlugin.kt` (register the task)
- Modify: `.github/workflows/pr.yml` (add the task to the `ktlint` job's command)
- Create: `build-logic/convention/src/test/kotlin/com/hedvig/android/CommonMainImportGuardTest.kt` (or the location existing build-logic tests use; if build-logic has no tests, test the pure function with a `jvmTest` in the nearest existing test setup and say so)

**Interfaces:**
- Produces: root Gradle task `checkCommonMainImports`, failing with one line per violation as `<path>:<line>: <import>`. A pure function `findForbiddenImports(source: String): List<Pair<Int, String>>` returning 1-based line numbers. An allow-list file `config/commonMain-import-allowlist.txt` of `<path>` entries, each preceded by a `#` comment giving the reason.

- [ ] **Step 1: Write the failing test.**

```kotlin
@Test fun `flags java javax and android imports only`() {
  val source = """
    package a
    import java.util.Locale
    import javax.inject.Inject
    import android.content.Context
    import androidx.compose.runtime.Composable
    import kotlin.time.Clock
    import javafoo.Bar
  """.trimIndent()
  assertThat(findForbiddenImports(source)).containsExactly(
    2 to "java.util.Locale", 3 to "javax.inject.Inject", 4 to "android.content.Context",
  )
}
```

- [ ] **Step 2: Run it.** Expected: FAIL, `findForbiddenImports` unresolved.
- [ ] **Step 3: Implement `findForbiddenImports` and the task.** The task scans `**/src/commonMain/**/*.kt` under `app/` and `micro-apps/`, skips allow-listed paths, and is configuration-cache compatible.
- [ ] **Step 4: Run the test.** Expected: PASS.
- [ ] **Step 5: Run `./gradlew checkCommonMainImports` on `develop`.** Fix violations that are a one-line change; allow-list the rest with a reason. Expected afterwards: BUILD SUCCESSFUL.
- [ ] **Step 6: Add the task to `pr.yml`, run `./gradlew ktlintFormat`, commit.**

---

## Wave 1

Tasks 3 to 10 each apply the Conversion Recipe. Only module specifics are listed.

### Task 3: `core-icons`

- `M` = `core-icons`, `D` = `app/core/core-icons`. No tests, so recipe step 5 does not apply. No ViewModels, and recipe step 7's diff must still be empty.
- Replace `libs.androidx.compose.materialIconsCore` with the JetBrains material icons core coordinate at the `jetbrains-material` catalog version; add the catalog entry if missing.

### Task 4: `notification-permission`

- `M` = `notification-permission`, `D` = `app/notification-permission`. One file, built on Accompanist and `androidx.activity`.
- **Produces:** the module's existing public composable API, unchanged in name and signature, declared `expect` in `commonMain` if its types are platform-free, with the `actual` in `androidMain` being today's code. If the public signature exposes an Accompanist or Android type, introduce a `commonMain` interface with the same member names and keep call sites source-compatible.
- `jvmMain` actual: permission reads as not granted, requesting does nothing, no dialog is shown.
- `nativeMain` is not added (the module does not compile for iOS today).

### Task 5: `NotAvailableOnDesktop`

`compose-webview` is not converted. Its public API is a copy of Accompanist WebView with Android types throughout (`WebViewClient`, `WebChromeClient`, a `(Context) -> WebView` factory, `Bitmap`/`Bundle` in its state), and Trustly relies on the factory and on `onCreateWindow`. Splitting that for `commonMain` would reshape shared code only for desktop, which the skip rule forbids. The module stays Android-only, and each caller owns its seam: the licenses screen in Task 20 and the Trustly web step in Task 23 each get an `expect` composable whose `androidMain` actual calls `WebView(...)` as today and whose `jvmMain` actual is `NotAvailableOnDesktop()`.

- **Produces:** `@Composable fun NotAvailableOnDesktop(modifier: Modifier = Modifier)` in `app/design-system/design-system-hedvig/src/jvmMain/kotlin/com/hedvig/android/design/system/hedvig/NotAvailableOnDesktop.kt`, public, showing the text `Not available on desktop` centred, with existing design system components. No other parameters. Later tasks use it for every skip.
- Verify: `./gradlew :design-system-hedvig:compileKotlinJvm :design-system-hedvig:jvmTest`, and a `jvmTest` using the Compose UI test already set up for JVM in this repo that asserts a node with text `Not available on desktop` is displayed.

### Task 6: `forever-ui`

- `M` = `forever-ui`, `D` = `app/shared/forever-ui`. Three files use `Intent`/`Context`/`Uri` to share the referral code.
- Seam: before adding one, search `commonMain` across the repo for an existing share or external-navigation abstraction and use it if it exists. Otherwise add `expect fun rememberShareHandler(): (text: String) -> Unit`-shaped seam local to this module; `androidMain` is today's `Intent` code, `jvmMain` copies the text to the system clipboard.

### Task 6a: Email validation callable from `commonMain`

Three feature modules (`feature-insurance-certificate`, `feature-travel-certificate`, `feature-login`) validate emails from presenter code that must move to `commonMain`, but the only validator lives in `androidMain` of `:core-common-public` and uses `androidx.core.util.PatternsCompat`. This task makes the boolean check available in `commonMain` with zero behaviour change on Android.

**Files:**
- Modify: `app/core/core-common-public/src/androidMain/kotlin/com/hedvig/core/common/android/validation/ValidateEmail.kt`
- Create: `app/core/core-common-public/src/commonMain/kotlin/com/hedvig/core/common/android/validation/IsValidEmail.kt`
- Create: the `jvmMain` actual, and a `nativeMain` actual if the module has iOS targets, in the same package
- Test: `app/core/core-common-public/src/commonTest/kotlin/com/hedvig/core/common/android/validation/IsValidEmailTest.kt` (or `jvmTest` if the module has no `commonTest` setup)

**Interfaces:**
- Produces: `expect fun isValidEmail(email: String): Boolean` in `commonMain`, package `com.hedvig.core.common.android.validation` (package unchanged, so existing Android imports keep working).
- `androidMain` actual: today's body, `email.isNotBlank() && PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()`, unchanged. `validateEmail(email: CharSequence): ValidationResult` stays in `androidMain`, unchanged.
- `jvmMain` and `nativeMain` actuals: `email.isNotBlank()` and a full match against a `Regex` built from the exact pattern string of `PatternsCompat.EMAIL_ADDRESS` (copy it from the androidx.core source in the Gradle cache or the artifact's sources jar; do not write a pattern from memory). The pattern string lives once, in a `commonMain` `internal` declaration both actuals use.
- No caller is changed in this task.

- [ ] **Step 1: Write the failing test** with at least these cases, asserting `isValidEmail`:
  - true: `a@b.co`, `first.last@example.com`, `user+tag@sub.example.org`, `USER_1%x-y@example-domain.se`
  - false: empty string, three spaces, `plainaddress`, `@example.com`, `user@`, `user@example`, `user name@example.com`, ` user@example.com` (leading space), `user@example.com ` (trailing space), `user@@example.com`
  Before finalising the expected values, check each case against the copied pattern by reading it; if any expectation above is wrong for that exact pattern, correct the expectation and say so in the report. The pattern is the authority, not this list.
- [ ] **Step 2: Run it.** Expected: FAIL, `isValidEmail` unresolved in `commonMain`.
- [ ] **Step 3: Implement** the `expect`, the actuals and the shared pattern string.
- [ ] **Step 4: Run** `./gradlew :core-common-public:jvmTest` (and the iOS simulator test task if the module has iOS targets and `commonTest`). Expected: PASS.
- [ ] **Step 5: Run** `./gradlew :core-common-public:compileAndroidMain :app:assembleDebug :core-common-public:ktlintFormat`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 6: Commit.**

### Task 7: `feature-insurance-certificate`

- After Task 6a. `M` = `feature-insurance-certificate`, `D` = `app/feature/feature-insurance-certificate`. One file uses `java.io.File`; the email presenter calls `validateEmail(email).isSuccessful`, which becomes `isValidEmail(email)` from Task 6a.
- `InsuranceEvidenceEntries.kt` stays in `androidMain` because it shares the PDF through `LocalContext` and `Context.sharePDF`. It is swept in Task 24 once a share-PDF seam exists.
- Seam: use the file abstraction already in `:core-file-upload` `commonMain` (`JvmFile`/`JvmDownloadPdfUseCaseImpl` exist on the JVM side). Do not add a new one.

### Task 8: `feature-travel-certificate`

- After Task 6a (email validation, as Task 7). Entries that call `Context.sharePDF` stay in `androidMain` as in Task 7. `M` = `feature-travel-certificate`, `D` = `app/feature/feature-travel-certificate`. Platform code: `java.io.File` (2 files), `java.util.Locale` (1), `android.R.attr` (1).
- `File`: as Task 7. `Locale`: use `:core-locale` `commonMain`. `android.R.attr`: replace with the Compose equivalent already used elsewhere in the module, with no visual change; if none exists, keep that one file in `androidMain` and report it.

### Task 9: `feature-payin-account`

- `M` = `feature-payin-account`, `D` = `app/feature/feature-payin-account`. One file uses `Uri`, `Build`, `PackageManager` (opening a bank app).
- Seam: an `expect` composable-remembered launcher local to the module; `jvmMain` opens the URL with `java.awt.Desktop.browse` when it is `http(s)`, otherwise does nothing and the screen shows `NotAvailableOnDesktop()` where the action was (depends on Task 5; if Task 5 is not merged, the agent rebases onto its branch).

### Task 10: `feature-terminate-insurance`

- `M` = `feature-terminate-insurance`, `D` = `app/feature/feature-terminate-insurance`. Platform code: `java.util.Locale` (2 files), `android.R.attr` (1).
- As Task 8. `androidx.compose.material3.windowSizeClass` becomes the JetBrains equivalent already used by a `commonMain` module in this repo (find it; do not add a new library).

## Wave 2

### Task 11: `member-reminders-ui`

- After Task 4. `M` = `member-reminders-ui`, `D` = `app/member-reminders/member-reminders-ui`. No platform imports of its own.

### Task 12: `feature-forever`

- After Task 6. `M` = `feature-forever`, `D` = `app/feature/feature-forever`. Two files, no platform imports.

### Task 13: `feature-payments`

- After Tasks 4 and 11. `M` = `feature-payments`, `D` = `app/feature/feature-payments`. 34 files, no platform imports.

---

## Gate

The orchestrator presents Task 1's findings to Stylianos. Wave 3 starts only on his go-ahead. Task 14's file placement and Task 17's flag provider are fixed from those findings; if a finding contradicts a task below, the task is revised here before dispatch.

## Wave 3

Sequential unless marked parallel: these tasks edit `:app` and `:navigation-compose`.

### Task 14: Navigation UI infrastructure out of `androidMain`

**Files:** `app/navigation/navigation-compose/src/androidMain/kotlin/**` (`Decorators.kt`, `BottomSheetSceneStrategy.kt`, `NavSuiteSceneDecorator.kt`, `NavigationSuiteType.kt`, the two retained decorators), `app/navigation/navigation-compose/build.gradle.kts`.

**Interfaces:**
- Consumes: Task 1 step 1 (artifact variants) and step 3 (required changes).
- Produces: the same public API, same package, reachable from `commonMain` of dependent modules. Destination is `commonMain` if iOS variants exist, else `jvmAndAndroidMain` with an entry in the spec's iOS debt register.

- [ ] Step 1: Move the files and dependencies. Step 2: `./gradlew :navigation-compose:compileKotlinJvm :navigation-compose:compileAndroidMain` and, if `commonMain`, `:navigation-compose:compileKotlinIosSimulatorArm64`. Step 3: `./gradlew :app:assembleDebug :navigation-compose:jvmTest :app:test --tests "*SerializationTest*"`. Step 4: Commit.

### Task 15: Shell keys into `-navigation` modules

**Files:** create KMP modules `app/feature/feature-home-navigation`, `feature-login-navigation`, `feature-insurances-navigation`, `feature-profile-navigation`, `feature-onboarding-navigation` (shape of `app/feature/feature-payments-navigation`); move `HomeKey`, `LoginKey`, `InsurancesKey`, `ProfileKey`, `OnboardingKey`, `OnboardingGate`, `ResetOnboardingSeenUseCase` (interface only; the impl stays in the feature).

**Interfaces:**
- Produces: each moved type in its original package, so no import changes anywhere and no serialized name changes.

- [ ] Step 1: Move one key per commit. Step 2: recipe steps 6 and 8. Step 3: Commit. Needs Stylianos's go-ahead because it touches the held modules, though only one small file in each.

### Task 16 (parallel with 15): Entries and bindings of already-KMP features to `commonMain`

**Files:** for `feature-claim-chat`, `feature-claim-history`, `feature-remove-addons` and every other KMP feature with an entries function, deep link matcher or demo switcher under `src/androidMain`: move those files to `src/commonMain`.

- Consumes: Task 14 and Task 1 step 7.
- [ ] Step 1: List the files (`git ls-files 'app/feature/*/src/androidMain/**' | grep -E 'Entries|DeepLinks|Switchers|MetroProviders'`). Step 2: Move per feature, one commit each. Step 3: recipe steps 4, 6, 7, 8 per feature.

### Task 17 (parallel with 15): JVM bindings for existing platform seams

**Files:** `src/jvmMain` of `core-datastore-public`, `core-file-upload`, `feature-flags`, `language-core`, `network-clients`, `permission-core`, mirroring each module's `androidMain` `*MetroProviders.android.kt`.

**Interfaces:**
- Produces: `@ContributesTo(AppScope::class)` providers in `jvmMain` for every type the `androidMain` providers bind: DataStore (file under `System.getProperty("user.home")/.hedvig-desktop/`), file service, `FeatureManager`, `LanguageStorage`, HTTP engine, permission manager (always not granted).
- `FeatureManager` on JVM: the Unleash fetch if Task 1 step 8 found it small; otherwise a static provider in one file with values supplied by Stylianos. Never `NoopFeatureFlagProvider`.

- [ ] Step 1: For each module, write a `jvmTest` that builds a minimal Metro graph over that module's contributions and asserts each bound type resolves. Step 2: Run, expect FAIL (missing binding is a compile error; confirm that is the failure). Step 3: Add the providers. Step 4: Run, expect PASS. Step 5: `./gradlew jvmTest :app:assembleDebug`. Step 6: Commit per module.

### Task 18: `:app-shell`

**Files:**
- Create: `app/app-shell/build.gradle.kts` (`hedvig.multiplatform.library` + `.android`, `compose()`, `serialization()`)
- Move from `app/app/src/main/kotlin/com/hedvig/android/app/` to `app/app-shell/src/commonMain/kotlin/com/hedvig/android/app/`, keeping packages: `navigation/BackstackController.kt`, `navigation/TopLevelRunLogic.kt`, `navigation/SessionReconciler.kt`, `navigation/HedvigEntryProvider.kt`, `navigation/HedvigContentKeys.kt`, `navigation/ScreenName.kt`, `navigation/ScreenParameterExtractor.kt`, `navigation/CurrentDestinationHolder.kt`, `navigation/NavRetainedViewModel.kt`, `ui/HedvigApp.kt`, `ui/HedvigAppState.kt`, `ui/NavigationSuite.kt`, `ui/TopLevelTabStartDestination.kt`, `urihandler/DeepLinkFirstUriHandler.kt`, `GlobalHedvigSnackBar.kt`, `di/MergedMetroViewModelFactory.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/AndroidAppHost.kt`, `MainActivity.kt`, `app/app/build.gradle.kts`

**Interfaces:**
- Produces, in `:app-shell` `commonMain`:

```kotlin
interface AppHost {
  val uriHandler: UriHandler
  @Composable fun ViewTrackingEffect(backstackEntries: List<HedvigNavKey>)
  @Composable fun SystemBarsEffect(isDarkContent: Boolean)
  fun requestInAppReview()
}

fun EntryProviderScope<HedvigNavKey>.hedvigEntries(
  backstack: Backstack,
  /* existing parameters of hedvigEntryProvider, with AndroidAppHost replaced by AppHost */
  hostEntries: EntryProviderScope<HedvigNavKey>.() -> Unit,
)

@Composable fun HedvigApp(/* existing parameters, AndroidAppHost replaced by AppHost */, hostEntries: EntryProviderScope<HedvigNavKey>.() -> Unit)
```

- `:app`'s `AndroidAppHost` implements `AppHost`: `ViewTrackingEffect` calls Datadog's `Navigation3TrackingEffect` with the same arguments `HedvigApp` passes today; `SystemBarsEffect` holds today's `SystemBarStyle` code.
- `:app` passes as `hostEntries` the `entry<>` registrations of every feature that is still Android-only at that point, cut from `HedvigEntryProvider` verbatim.
- `ExhaustiveBackStackSerializationTest`, `BackStackSerializationTest` and `AnalyticsNameTest` stay in `:app`. `BackstackTest` already lives in `:navigation-compose` and is untouched.

- [ ] Step 1: Baseline: recipe step 1, plus `./gradlew :app:test`.
- [ ] Step 2: Create the module, move the files (move-only commit).
- [ ] Step 3: Introduce `AppHost` and `hostEntries`; make `:app-shell` and `:app` compile.
- [ ] Step 4: `./gradlew :app:test`, recipe steps 7 and 8. Expected: same tests pass, empty ViewModel diff.
- [ ] Step 5: On the Pixel with a debug build, navigate Home, Insurances, a contract detail, Chat, and back. Compare Datadog RUM view names for that session against a session on `develop` (`@view.name` values must be identical). Expected: identical sets.
- [ ] Step 6: Commit.

### Task 19: `:app-desktop`

**Files:**
- Create: `app/app-desktop/build.gradle.kts` (JVM target, Compose Desktop `application { mainClass = "com.hedvig.android.desktop.MainKt" }`)
- Create: `app/app-desktop/src/jvmMain/kotlin/com/hedvig/android/desktop/Main.kt`, `DesktopGraph.kt`, `DesktopAppHost.kt`, `DesktopMetroProviders.kt`, `DesktopHostEntries.kt`
- Modify: `.github/workflows/pr.yml` (add `:app-desktop:compileKotlinJvm` to the `build` job)

**Interfaces:**
- Consumes: `HedvigApp`, `AppHost`, `hedvigEntries` (Task 18); JVM providers (Task 17); `NotAvailableOnDesktop` (Task 5).
- Produces: `./gradlew :app-desktop:run`. `DesktopGraph` is `@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])`. `DesktopMetroProviders` provides `HedvigBuildConstants` with `platformName = "ANDROID"` and the Android `User-Agent` and app version, staging URLs by default, production when Gradle property `hedvig.desktop.production=true`. `DesktopHostEntries` registers `NotAvailableOnDesktop()` for every key whose feature is not yet in the shared provider. Window: 412 x 915 dp, resizable, Escape dispatches system back.

- [ ] Step 1: Write `DesktopGraphTest` in `jvmTest`: the graph is created and `viewModelFactory` resolves `SwedishLoginViewModel`'s shell-side dependencies (or, while `feature-login` is unconverted, one ViewModel of `feature-claim-chat`). Step 2: Run, FAIL. Step 3: Implement. Step 4: Run, PASS. Step 5: `./gradlew :app-desktop:run`, log in by QR (Stylianos scans), open each tab. Expected: tab scaffold renders; converted features work; others show the placeholder. Step 6: Add the CI step, commit.

## Wave 4

Conversion Recipe for each, plus removing that feature's entries from both hosts' `hostEntries` and adding them to the shared provider.

### Task 20: `feature-profile`
- After Tasks 3, 4, 5, 11. The licenses screen's web view becomes a module-local `expect` composable (`androidMain`: today's `rememberWebViewState` + `WebView`; `jvmMain`: `NotAvailableOnDesktop()`, reported as a skip). Platform code: Accompanist permissions (use Task 4's API), `Build`/`Intent`/`Context` for app settings and language (seam; `jvmMain` shows `NotAvailableOnDesktop()` for open-app-settings, language switching works in-app), `androidx.appcompat` per-app locale (stays in `androidMain`).

### Task 21: `feature-home`, `feature-insurances`, `feature-onboarding`
- Held modules, one PR each, each on Stylianos's go-ahead. `feature-home` and `feature-onboarding`: DataStore usage moves to the `commonMain` DataStore API. `feature-onboarding` uses `androidx.navigation3.ui` directly: use Task 14's result.

### Task 22: `feature-login`, `feature-image-viewer`, `feature-claim-details`
- `feature-login`: media3 background video becomes an `expect` composable; `jvmMain` shows `login_still_9x16`. `BankIdState` becomes a seam; `jvmMain` has `canOpenBankId = false`. The video and still move to where each target can load them. Held module: needs go-ahead.
- `feature-image-viewer`: confirm the zoomable library publishes JVM and iOS variants; if not, the zoom gesture goes behind a seam and `jvmMain` shows the image without zoom (report as a skip).
- `feature-claim-details`: file pickers use the seam `feature-claim-chat` already has (`FilePicker`); lift it to a shared module only if both features need the identical API.

### Task 23: `feature-chat`, `feature-connect-payment-trustly`
- `feature-chat`: Room and paging from `commonMain` (paging-compose 3.5.3 publishes desktop and iOS variants); `database-android`'s builder gets a `jvmMain` sibling using the bundled SQLite driver; media3 playback behind a seam with `NotAvailableOnDesktop()` on JVM; attachments through the file picker seam. If the message list cannot be shared without restructuring the presenter, stop and bring it to Stylianos as a skip-rule decision.
- `feature-connect-payment-trustly`: screens and presenters to `commonMain`; the web view step becomes a module-local `expect` composable whose `androidMain` actual is today's `WebView(...)` call with `TrustlyWebView` and its clients, and whose `jvmMain` actual is `NotAvailableOnDesktop()`. Report as a skip.

### Task 23a: Share-PDF seam and the JVM PDF download
- `Context.sharePDF(file, applicationId)` in `:core-common-public` `androidMain` is called from the entries of `feature-insurance-certificate`, `feature-travel-certificate` and `feature-claim-details`. Add a seam those entries can call from `commonMain`; `androidMain` is today's code, `jvmMain` opens the file with `java.awt.Desktop.open`. Implement `JvmDownloadPdfUseCaseImpl` (currently `TODO()`) and bind it in `jvmMain`.

### Task 24: Entries sweep
- Every file the ledger lists under "Left in `androidMain`" because of `:navigation-compose` moves to `commonMain`. One commit per module. Recipe steps 4, 6, 7, 8.

## Wave 5

### Task 25: Plain JVM libraries
- For each non-test `hedvig.jvm.library` module that a `commonMain` source set depends on: swap the plugin to `hedvig.multiplatform.library`, `git mv src/main/kotlin src/commonMain/kotlin`, `git mv src/test src/jvmTest`. One PR per directory group (`data/`, `auth/`, `core/`, the rest). `./gradlew test jvmTest :app:assembleDebug`.

### Task 26: Walkthrough and registers
- [ ] Step 1: `./gradlew :app-desktop:run` against staging. Visit every tab and every flow reachable from it. Step 2: For each flow record works / skipped / broken. Broken ones become fix tasks. Step 3: Update the skip register and iOS debt register in the spec. Step 4: Add a "Desktop app" section to `CLAUDE.md` (how to run it, the commonMain rule, how to add a seam) and a symptom-phrased pointer per the repo's skills convention.
