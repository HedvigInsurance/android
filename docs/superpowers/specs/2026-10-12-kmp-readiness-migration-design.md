# KMP-readiness migration and module graph tooling

## Goal

Keep the Android codebase on its path of being multiplatform-ready, where "ready" means
a module compiles for JVM and Android with no Android types in `commonMain`, without
paying the iOS cost for modules iOS never calls. Along the way, make the module
dependency graph readable again so the migration can be seen before, during and after.

There is no product goal to share more code with iOS. The migration converts modules
that already have no Android ties, so the work is mechanical and reversible. Modules
with real Android surface are out of scope.

## Background

Facts established on 2026-10-09 to 2026-10-11, on `develop` at `457a5bab0e`:

- 147 Gradle modules under `app/`: 63 on `hedvig.multiplatform.library` (iOS targets),
  1 on `hedvig.multiplatform.library.jvm` (`feature-claim-chat`), 47 on
  `hedvig.android.library`, 35 on `hedvig.jvm.library`, 1 application.
- `:umbrella` exports 10 modules; their transitive closure, the set compiled into
  `HedvigShared` for iOS, is 48 modules counting `:umbrella` and `:tracking-core`.
  17 iOS-capable modules sit outside that
  closure and compile iOS targets for nothing.
- Production Ugglan calls three things from `HedvigShared`: authlib for login and token
  refresh, the two puppy guide Compose screens, and the DI graph bootstrap.
- Costs of iOS targets on modules iOS never uses: every `expect` needs a `nativeMain`
  actual, Android-only changes in exported modules can break umbrella CI (three such
  fix commits the week of 2026-09-22), and the native dead-code-elimination workaround
  applies to every native binary.
- Android Lint registers no runnable task on KMP-android modules (comment in
  `build-logic/convention/src/main/kotlin/HedvigLintConventionPlugin.kt`), so every
  conversion moves a module out of lint coverage until that is resolved.
- `generateProjectDependencyGraph` works but produces a 32767 by 2870 px, 28 MB PNG:
  151 nodes and 1755 edges, of which 262 survive transitive reduction, and four modules
  attached to every module by the convention plugin account for roughly 590 edges.

## Decisions

- **Single zoomable SVG**, committed to the repo and referenced from the README. Mermaid
  was rejected because GitHub cannot render 150 nodes legibly.
- **Fix the existing Groovy script in place** rather than rewrite it in build-logic.
  It stays configuration-cache incompatible; that is a warning on a manual task.
- **Flip the iOS default** in the convention plugins so new conversions never inherit
  iOS targets. Being KMP-ready and being iOS-exported become two separate choices.
- **Scan is import-based**, so the compile is the real test. A module that needs more
  than a one-line change is dropped from its wave and noted, not forced.
- **Everything stays local** until told otherwise: no pushes, no PRs, no umbrella
  releases triggered.

## Part 1: graph detour

Task name and location unchanged: `./gradlew :generateProjectDependencyGraph`,
`gradle/projectDependencyGraph.gradle`.

**Node set.** Every project except, each with a one-line reason at the top of the script:

- `:app`: depends on every feature; adds ~40 edges that carry no information.
- `:hedvig-lint`, `:hedvig-ktlint`, `:logging-public`, `:tracking-core`: attached to
  every module by the convention plugin.
- Test modules: path ends in `-test` or `-fake`, plus `:test-clock` and
  `:accessibility-test`.

**Edge set.** Configurations whose lowercase name does not contain `test`, `lint` or
`ktlint`. Solid line for `api`, dotted for `implementation`.

**Node kind**, decided by which convention plugin is applied, one fill colour each:

| Kind | Plugin id |
|---|---|
| KMP, iOS targets | `hedvig.multiplatform.library.ios` (after Part 2; before it, `hedvig.multiplatform.library`) |
| KMP, no iOS | `hedvig.multiplatform.library` (after Part 2; before it, the `.jvm` id) |
| Android library | `hedvig.android.library` |
| JVM library | `hedvig.jvm.library` |
| Application | `hedvig.android.application` |

The script resolves kind through a small lookup so the Part 2 rename is a one-line edit.

**iOS closure.** Modules reachable from `:umbrella` through main configurations get a
bold border. This is the "exported to iOS" signal.

**Legend.** Drawn into the graph as a cluster: the five kinds, the border meaning, the
dotted versus solid meaning, and the excluded modules.

**Layout and output.** `tred` for transitive reduction, then `dot -Tsvg` with
`rankdir=LR`. Output `misc/images/modularization-graph.svg`. The PNG is deleted and the
README image reference updated. Both `tred` and `dot` come from graphviz, already a
documented prerequisite.

**Summary.** The same run writes `misc/images/modularization-summary.md` and prints it:

- Count of modules per kind.
- Count of modules in the iOS closure.
- Non-KMP modules that KMP-capable modules depend on, grouped by blocker, each with the
  list of dependents.

**Bookends.** The graph-detour PR regenerates and commits the SVG and summary on
current `develop` as the baseline. Every later PR in this migration re-runs the task and
commits the result, so the summary diff shows what that PR changed. After the last
wave, the README's module structure section gets the before and after counts.

## Part 2: convention plugin changes

One PR, landed before any conversion.

**Flip the iOS default.**

- `hedvig.multiplatform.library` becomes JVM plus Android targets only.
- New id `hedvig.multiplatform.library.ios` adds `iosArm64` and `iosSimulatorArm64`.
- The 48 closure modules switch to `.ios`. `feature-claim-chat` switches from `.jvm` to
  the plain id. The `.jvm` id and its plugin class are deleted.
- No guard is added: if a closure module forgets `.ios`, `:umbrella` fails to resolve
  its iOS compilation, which is the right failure.

**Delete dead native code.** The 17 modules outside the closure lose their iOS targets.
Their remaining `nativeMain` directories are deleted in the same PR:
`audio-player-ui`, `compose-photo-capture-state`, `compose-result-launcher`,
`core-file-upload`. Any `expect` declaration in those modules that only existed to feed
a native actual is reviewed; where the JVM and Android actuals are identical, the
`expect` is collapsed into plain `commonMain` code.

**Absorb per-module ceremony** into `hedvig.multiplatform.library.android`:

- When the Compose plugin is applied, add `libs.bundles.kmpPreviewBugWorkaround` to
  `androidMain` automatically.
- Apply the `jvmAndAndroid` hierarchy group by default, so a module can add a
  `jvmAndAndroidMain` source set without declaring the hierarchy.
- Modules currently declaring either remove the declaration.

After this, a converted module's build file is: plugin ids, the `hedvig {}` block,
`commonMain.dependencies`, and optionally `androidMain.dependencies` for genuinely
Android-only dependencies.

**Verification for this PR.** `:umbrella:assembleHedvigSharedReleaseXCFramework`,
`:app:assembleRelease`, `jvmTest` across the repo, and the regenerated summary showing
the iOS closure unchanged at 48 and the KMP-with-iOS count down from 63 to 46.

## Part 3: migration waves

**Recipe for every module conversion:**

1. Swap the plugin id to `hedvig.multiplatform.library` (plus
   `hedvig.multiplatform.library.android` when the module uses Compose, Android
   resources, or has consumers that need an Android variant).
2. Move `src/main` to `src/commonMain`, `src/test` to `src/jvmTest`.
3. Move the dependency list into `kotlin { sourceSets { commonMain.dependencies { } } }`
   and test dependencies into `jvmTest.dependencies`.
4. Swap `libs.androidx.compose.*` catalog entries for `libs.jetbrains.compose.*`.
5. Compile, run `jvmTest`, regenerate the graph summary, commit SVG and summary with the
   change.

**Wave 0, cleanup.** Delete `data-product-variant-android` (one file, no consumers).

**Wave 1, Android modules with no Android ties.** One PR.

| Module | Beyond the recipe |
|---|---|
| `feature-chip-id` | none |
| `tier-comparison` | drop the `android { testOptions ... }` block |
| `compose-pager-indicator` | none |
| `file-upload-ui` | none |
| `ui-phone-number` | none |

**Wave 2, JVM modules.** Two PRs, leaves first.

- PR 2a, leaves with no non-KMP dependencies: `auth-event-core`, `theme`, `test-clock`,
  `data-cross-sell-after-flow`, `data-display-items`, `navigation-activity`.
- PR 2b, the rest: `core-demo-mode`, `data-settings-datastore-public`, `data-addons`,
  `data-paying-member`, `data-cross-sell-after-claim-closed`, `core-app-review`,
  `apollo-auth-listeners`, `datadog-demo-tracking`, `notification-badge-data-public`,
  `language-data`, `data-chat`, `database-core`, `data-changetier`, and
  `auth-core-public` and `member-reminders-public` with their single `java.util.UUID`
  replaced by the catalog's multiplatform uuid.

Test modules (`*-test`, `*-fake`) that depend on converted modules are converted in the
same PR when the compile requires it, otherwise left as JVM libraries.

**Wave 3, Android features unblocked by waves 1 and 2.** Grouped by shared blockers,
two or three modules per PR:

| PR | Modules | Beyond the recipe |
|---|---|---|
| 3a | `claim-status`, `cross-sells`, `feature-delete-account` | none |
| 3b | `ui-analytics-consent` | none |
| 3e | `feature-edit-coinsured` | replace one `java.util.UUID` |

**Stayed Android libraries.** Only `feature-insurances` stays on `hedvig.android.library`:
it uses `NavSuiteSceneDecoratorStrategy` from `navigation-compose`'s `androidMain`. The
ViewModel KSP processor's commonMain metadata pass now runs as a JVM analysis on modules
without native targets, so JVM-library types resolve.

**Wave 4, finish `feature-claim-history`.** Move `ClaimHistoryDestination.kt` and
`ClaimHistoryViewModel.kt` from `androidMain` to `commonMain`; the nav entry file stays
in `androidMain`.

**Out of scope.** Modules with real Android surface: `notification-*`,
`datadog-android`, `tracking-*`, `compose-webview`, `core-rive`, `language-migration`,
`feature-login`, `feature-chat`, `feature-connect-payment-trustly`, `feature-profile`,
`feature-payin-account`, `feature-claim-details`, `forever-ui`, `feature-forever`,
`feature-home`, `feature-onboarding`, `feature-payments`, `feature-image-viewer`,
`feature-impersonation`, `feature-insurance-certificate`, `feature-travel-certificate`,
`feature-terminate-insurance`, `logging-device-model`, `member-reminders-ui`,
`notification-permission`. Also `navigation-compose` and anything in the iOS closure.

Deferred, found during planning on 2026-10-12: `core-icons`. It builds its icons with
`materialIcon` and `materialPath` from `material-icons-core` in 53 files, and the JetBrains
multiplatform artifact for that library stops at Compose Multiplatform 1.7.3 while the project
is on 1.12.1. Converting it means rewriting every icon onto `ImageVector.Builder`. Its only
consumers are `:app` and `feature-profile`, so it blocks nothing.

Wave 2 (Tasks 16 to 18) was cancelled on 2026-10-12: plain JVM libraries are already
consumable from `commonMain` of JVM-plus-Android KMP modules, verified by probe, while
single-target KMP modules expose no `commonMain` metadata.

## Part 4: verification, lint, logistics

**Per-PR gate.** For every module touched: its main compilation, `jvmTest`, and
`:app:compileReleaseKotlin`. For the Part 2 PR, additionally the umbrella XCFramework.
Then `generateProjectDependencyGraph`; the committed summary diff must match what the PR
claims to have changed.

**Lint.** Measured outcome of the spike: AGP 9.4.1 registers no Android Lint on KMP-android
modules by itself. The convention plugin now applies `com.android.lint` on top of
`com.android.kotlin.multiplatform.library` for those modules, which registers `lintAndroidMain`,
`updateLintBaselineAndroidMain` and friends. That pass lints `commonMain`, `jvmAndAndroidMain` and
`androidMain` with the shared `lint.xml` and the per-module baseline, and root `./gradlew lint`
runs it. The dependency cycle between `core-common-public` and `apollo-octopus-public` was dead on
both sides and has been removed, so every KMP-android module gets Android Lint. KMP modules without
an Android target get JVM lint over `jvmMain` only, so their `commonMain` is not linted. CI annotates lint findings
from every module's report on the PR.

**Logistics.** Work happens in `android_copy`, which was moved to `develop` on
2026-10-12. One branch per PR, each off `develop`. All branches stay local: no pushes,
no PRs, no umbrella releases, until explicitly told otherwise.

## Order of work

1. Graph detour PR (Part 1), including the baseline SVG and summary.
2. Convention plugin PR (Part 2).
3. Lint spike, with the spec amended by its outcome.
4. Waves 0 through 4 in order.
5. Final graph regeneration and README update.
