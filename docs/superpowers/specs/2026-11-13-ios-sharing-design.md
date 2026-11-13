# Sharing presenters and data with iOS: design

Date: 2026-11-13. Measured against `feat/desktop-app-integration` at `99815f5a70` (Android) and Ugglan at
`0431fb12d`.

This is the design as planned. The desktop work it builds on is described in `2026-11-12-desktop-app-as-built.md`.
Facts here were read from the code at those commits unless marked "unverified". The reconnaissance behind it is in
`.superpowers/sdd/2026-11-12-desktop-app/` of the `desktop-app-design` worktree (`ios-design-recon-android.md`,
`ios-design-recon-ugglan.md`, `task-40-ios-sharing-recon.md`, `final-review-foundations.md`).

## Intent

The Android app now runs as a desktop JVM application, which showed that the features do not need Android. The next
block is an experiment: share with the iOS app (Ugglan) everything from the presenters down to the data layer, and no
UI. iOS keeps SwiftUI and its own navigation.

The iOS objection to shared Kotlin in the past was binary size: 30 to 40 MB and more once Compose UI was in the
framework. A shared layer without UI should be far below that. The framework Ugglan links today is 3.59 MB shipped; a
Molecule ViewModel on the Compose runtime measured about +1.3 MB; exporting the network clients about +2.9 MB. This
experiment finds out what a real screen costs and how it feels to consume from Swift.

Nothing here ships. No PR is opened for this work until the owner says so. Branches are pushed and merged into
`feat/desktop-app-integration`. The Android app must not get worse: if supporting iOS would complicate `commonMain` a
lot, skipping that piece is acceptable (the skip rule of the desktop design, applied to iOS).

## Goals

The experiment has proved its point when all of these hold:

1. In the iOS shell app, a SwiftUI claim history screen shows loading, error with a working retry, the empty state
   and content, all from the shared Kotlin ViewModel, against staging.
2. The same holds for Ugglan's real claim history screen on an experiment branch built against a local umbrella.
3. Leaving the screen stops the presenter's work: after the screen is gone, its ViewModel is cleared, its coroutines
   are cancelled and no further request for it is made (checked in the log and the network log).
4. Login and logout keep the two GraphQL clients consistent: after a logout and a login as another member, neither
   client shows the previous member's data, and the Kotlin client sends the token iOS holds.
5. Android's compiled DI graph and behaviour are unchanged, verified the way the desktop work did (see
   "Verification").
6. The shipped size of the shared framework is recorded after every step.

## Non-goals

- Shipping anything, publishing an umbrella version, bumping Ugglan's pin, or opening PRs.
- Sharing UI. SwiftUI renders everything on iOS.
- Sharing navigation or deep link handling (see owner decisions 3 and 11).
- Converting features beyond claim history, or fixing UI leaks in modules claim history does not reach.
- Replacing Ugglan's GraphQL client, token refresh, Keychain storage, feature flags or analytics.

## Anti-goals

These make the experiment a failure even if the screen works.

- **Compose UI or Compose resources linked into the iOS framework.** That is the size problem that ended the last
  experiment. The framework links the Compose runtime (Molecule needs it) and nothing above it.
- **Android `commonMain` made noticeably worse** to suit Swift: Swift-only annotations or wrapper types threaded
  through presenters, platform conditionals in shared code, or a second state model per screen in `commonMain`.
- **A second navigation system in shared code.** No Swift-facing navigation port appears in presenters as part of
  this work.
- **Visibility widened across all features.** Only what the claim history proof needs becomes public.
- **Anything shipped or any PR opened.**

## Constraints

- **Repository layout.** `apps/` holds three independent Android clones (`android/`, `android_copy/`,
  `android_copy_2/`) and `ugglan/` as siblings. Ugglan's local umbrella loop (`ugglan/scripts/build-local-umbrella.sh`)
  builds from `$UGGLAN_ROOT/../android`, so it only works against the `android/` clone. If `android/` is busy with
  other work when the Ugglan step starts, surface the conflict instead of working around it.
- **Worktrees.** Work happens in worktrees branched from `feat/desktop-app-integration`; a clone's branch can be
  switched by another session, so verify `HEAD` before each commit. Never mirror a change between clones.
- **Git.** Each task is its own branch, pushed and merged into `feat/desktop-app-integration`. No force pushes. The
  Ugglan experiment branch stays local unless the owner says otherwise.
- **Staging only.** Every run that writes goes to staging with a staging account. Logging in needs a person with a
  staging BankID to scan the QR code.
- **Versions.** Xcode 26.4.1 on the owner's machine. Kotlin 2.4.21, KMP-NativeCoroutines 1.0.6 (Ugglan's Swift
  package 1.0.5), Metro 1.5.2, Apollo 5.2.0, `dd-sdk-kotlin-multiplatform-ktor3` 1.8.0, Ugglan's `dd-sdk-ios` 3.17.0.
- **Simulator.** Builds that will run must be signed ("Sign to Run Locally"); `CODE_SIGNING_ALLOWED=NO` strips the
  Keychain entitlement and the token is never saved (`-34018`). `xcodebuild test` with parallel testing clones the
  simulator and kills the running app; pass `-parallel-testing-enabled NO`. The framework has only `iosArm64` and
  `iosSimulatorArm64` slices.
- **Android library modules are release-only** under AGP 9: verify KMP modules with `compileAndroidMain` and
  `jvmTest`, the app with `:app:assembleDebug`.
- **No PR CI compiles an iOS target.** Every iOS compile in this work is a local run.

## Current state, in short

- `:umbrella` builds the static XCFramework `HedvigShared` (`iosArm64`, `iosSimulatorArm64`, `smallBinary`) and
  exports `authlib` and `logging-public` only. Ugglan calls Kotlin for BankID and OTP login, token exchange and
  revoke (`AuthenticationClientAuthLib.swift`, through KMP-NativeCoroutines) and console logging. Nothing else.
- `feature-claim-history` is JVM and Android only, 4 Kotlin files in `commonMain`: `ClaimHistoryViewModel.kt`
  (ViewModel, private presenter, `Reload` event, `Loading`, `NoHistory`, `Error`, `Content(NonEmptyList<ClaimHistory>)`),
  `GetClaimsHistoryUseCase.kt` (one query through `safeFlow`, returns `ClaimHistory(id, claimType, submittedAt:
  Instant?, outcome)`), and two UI files: `ClaimHistoryDestination.kt` and `nav/ClaimHistoryDestination.kt` (which also
  declares `ClaimHistoryKey`). Every type is `internal` except the key and the entries function. The presenter does not
  navigate and holds no strings. There is no demo implementation of the use case. `ClaimHistoryKey` is referenced only
  by `:app-shell` (JVM and Android) and `:app`'s serialization test.
- The Ugglan counterpart: `Projects/Claims/Sources/Views/ClaimHistoryScreen.swift` reads
  `ClaimsStore.historyClaims` (an `AppStore`) and calls `fetchHistoryClaims()` in `.task`; the store calls
  `hFetchClaimsClient.getHistoryClaims()`, implemented by
  `Projects/App/Sources/Service/OctopusClientsImplementation/FetchClaimClientOctopus.swift` over the `HistoryClaims`
  operation in `Projects/hGraphQL/GraphQL/Octopus/Claims/Claims.graphql`. The screen has no loading or error state
  (the empty state shows while loading). It is pushed from `Projects/Profile/Sources/Navigation/ProfileNavigation.swift`
  (`.claimHistory`); a tap pushes `.claimsCard(claim: ClaimModel)`, which builds
  `ClaimDetailView(claim:type:)`. That view also accepts `claim: nil` with `.claim(id:)` or `.partnerClaim(id:)`, but
  the Kotlin model does not say whether a row is a partner claim.
- There is no native Metro graph (`shareddi` was deleted). `network-clients` and `auth-core-public` compile for iOS,
  but native has no binding for `AccessTokenFetcher`, `AuthRepository`, `@AuthHttpClientEngine HttpClientEngine` or
  `DeviceIdFetcher`, and `auth-core-public` stores tokens through `AuthTokenStorage`, a concrete
  `@Inject @SingleIn(AppScope::class)` class over `DataStore<Preferences>` in `commonMain`, injected as itself (no
  interface, no `@ContributesBinding`), so it reaches every graph that includes the module.
- `language-core` `commonMain` depends on `core-resources` since `10330a3da7`, for one file, `LanguageLabel.kt`
  (`val Language.label: StringResource`). `core-build-constants`, `network-clients`, `auth-core-public` and more
  depend on `language-core`, so the whole data layer reaches `core-resources`, whose `commonMain` has
  `api(libs.jetbrains.components.resources)`, which on iOS depends on Compose foundation (recon, from the published
  module metadata). The only users of `Language.label` are two UI files: `feature-login`
  `marketing/MarketingDestination.kt` and `feature-profile` `settings/SettingsDestination.kt`. On `develop`, where the
  sizes above were measured, the edge was in `androidMain`.
- A Swift test harness already exists: `micro-apps/umbrella-consumer`, a checked-in Xcode project (format
  `objectVersion = 77`, last upgraded with Xcode 26) whose build phase runs
  `./gradlew :umbrella:embedAndSignAppleFrameworkForXcode` from two directories up. It hardcodes a `JAVA_HOME`, links
  `dd-sdk-ios` through Swift Package Manager and, in `IosInterfaces.swift`, carries a hardcoded access token (its `exp`
  claim is 5 June 2026).

## Decided by the owner

1. **First proof: the claim history screen**, end to end, on a real Ugglan screen, driven by the shared Kotlin
   presenter.
2. **An iOS shell application lives in the Android repo**, like `:app-desktop` for desktop, so changes can be run and
   validated locally and quickly without the whole of Ugglan.
3. **Navigation stays separate per platform** and is not mixed at all if possible. How to keep it out of shared code
   is not decided; it is assessed when that work starts.
4. **Presenters holding `TextFieldState` go last**, after the rest are shown to be shareable. A sustainable solution
   is owed eventually.
5. **Swift may consume ViewModels or presenters.** No conceptual preference; choose what is practically best.
6. **iOS tokens stay where iOS keeps them today (Keychain)**, bridged with interfaces as auth is today.
7. **Strings: typed values in state** that the UI maps to text. String resources in data or ViewModel layers are a
   mistake generally.
8. **Small seams** such as opening links and email: an injected interface where it applies, but UI work stays in
   SwiftUI, so not everything has to go through shared code.
9. **Inside a feature module the split is by source set**: shared code in `commonMain` with iOS targets, Compose UI in
   a JVM and Android source set (`jvmAndAndroidMain`). No extra `-ui` modules.
10. **KMP-NativeCoroutines is not assumed** to be the right bridge.
11. **Deep link and navigation semantics are tied to Navigation 3** and are not shared; at most link definitions.

## Design

Each call below is the designer's, open to override. Each gives the reason and what it costs if it is wrong.

### A. Order of work (designer's call)

| Step | What | Exit |
|---|---|---|
| 0 | Foundation fixes before any native graph: the `language-core` edge, per-platform auth and demo bindings, a guard against Compose UI in shared `commonMain` | Android graph unchanged; the data layer's iOS compile classpath has no Compose resources |
| 1 | Split `feature-claim-history` by source set, add iOS targets, give it a deliberate public surface | The module compiles for both iOS targets; Android unchanged |
| 2 | A native Metro graph in `:umbrella` with Swift-supplied inputs | The framework links with the graph and the claim history ViewModel reachable |
| 3 | The iOS shell app | Goal 1 and goal 3 in the shell |
| 4 | Bridge comparison inside the shell | A written recommendation to the owner |
| 5 | The Ugglan screen on an experiment branch | Goals 2 to 4 in Ugglan |

Size is measured after every step (call I). Reason: each step is independently checkable, and the step 0 fixes are
cheaper before any feature depends on the shapes they change. Cost if wrong: if step 0 turns out larger than it looks
(for example the auth binding change ripples through Android tests), steps 1 and 2 could go first on a branch and step
0 land behind them; nothing in the order is load-bearing except that step 2 needs step 0's auth change.

#### Step 0 in detail

1. **`Language.label` leaves `language-core`.** Move the mapping from `Language` to a `StringResource` next to its two
   UI callers (or into one UI module both already depend on), and drop `projects.coreResources` from `language-core`'s
   `commonMain`. `Language` is already the typed value decision 7 asks for; only its label is UI. `core-build-constants`
   and `core-common-public` keep `core-resources` in `androidMain` only, so after this nothing under `network-clients`
   reaches Compose resources. Check with the iOS compile classpath of `network-clients` (no `components-resources`).
2. **Auth bindings per platform, Android graph unchanged.** `auth-core-public` has JVM and iOS targets and no Android
   target, so Android consumes its JVM variant: a binding moved from `commonMain` to `jvmMain` keeps Android's graph as
   it is. `AuthTokenStorage` is a concrete `@Inject @SingleIn(AppScope::class)` class today, bound as itself; it
   becomes an interface, and today's DataStore class becomes its JVM implementation in `jvmMain` with a new
   `@ContributesBinding(AppScope::class)` (keeping `@SingleIn`). `AuthTokenServiceImpl`, `AndroidAccessTokenProvider`
   and `LogoutUseCaseImpl` keep their classes but move to `jvmMain` with their `@ContributesBinding` (and, for
   `AuthTokenServiceImpl`, are also bound by the native graph, see F). `core-demo-mode`'s `DataStoreDemoManager` gets
   the same treatment: that module also has JVM and iOS targets and no Android target, so it moves to `jvmMain`.
   `MemberIdService` and the tests that construct `AuthTokenStorage` follow the interface. Expected Android graph
   difference, precisely: the `AuthTokenStorage` key is now satisfied by a binding to the new implementation class
   (a new class name if it is renamed) where it was the class's own constructor; the other moved bindings keep the
   same keys and classes; 87 ViewModels, 21 matcher providers and 111 keys unchanged.
3. **A guard against Compose UI in shared `commonMain`.** Opt-in per module through the `hedvig { }` DSL (the name is
   the builder's), because many modules with iOS targets are UI modules themselves (`design-system-*`, `compose-ui`,
   `navigation-compose`, `feature-help-center`). For an opted-in module it fails the build when `commonMain` imports
   Compose UI, foundation, material, the design system, `hedvig.resources`, `org.jetbrains.compose.resources`, Coil,
   `paging-compose` or `navigation3-ui`, reusing the `CommonMainImportGuard` machinery with a second list. The same
   flag changes the convention itself: `viewModels()` in `HedvigGradlePluginExtension` adds `metro.viewmodel.compose`
   to `commonMainImplementation` for every module that calls it, not per module, so for an opted-in module the
   convention must add it to `jvmAndAndroidMain` instead (`metro.viewmodel` stays in `commonMain`). Non-opted modules
   keep today's placement, so Android's classpaths do not change. A dependency-list assertion on
   `commonMain` (Compose UI artifacts, `design-system-*`, `ui/*`, `core-resources`, `navigation-compose` forbidden) is
   the stronger form; add it if it is cheap. Reason: the compiler will not catch UI in `commonMain`, since every one of
   those artifacts publishes iOS variants. Cost if wrong: a guard that is too strict blocks a legitimate import; the
   allowlist mechanism the guard already has covers that.

Step 0 does not touch the 44 leak files in other modules (J).

#### Step 1 in detail

`feature-claim-history` applies `hedvig.multiplatform.library`, which the convention documents as never part of the
iOS umbrella (`KotlinMultiplatformLibraryConventionPlugin.kt`, the class KDoc), and its `commonMain` depends on
`design-system-hedvig`, `core-resources`, `jetbrains.compose.ui.tooling.preview` and `navigation-compose`. Designer's
call: switch it to `hedvig.multiplatform.library.ios` (keeping `.android`), opt it into the step 0 guard, and move
`ClaimHistoryDestination.kt` and `nav/ClaimHistoryDestination.kt` (with `ClaimHistoryKey` and the entries function)
to `jvmAndAndroidMain` with `git mv`, together with the dependencies only they use: `design-system-hedvig`,
`core-resources`, the tooling preview, `navigation-compose`, `navigation-common` and `kotlinx.serialization.core`.
`commonMain` keeps Apollo, Arrow, the Compose runtime, `apollo-core`, `apollo-octopus-public`, `core-common-public` and
`molecule-public`, all of which have iOS targets. `ClaimHistoryKey`'s only consumers are `:app-shell` and `:app`
(JVM and Android), so the move is invisible to them. The facade (E) and the partner flag land in the same step.

### B. The iOS shell (designer's call)

A small SwiftUI app at `app/app-ios/`, next to `app/app-desktop/`, with a checked-in, hand-made Xcode project in the
format `micro-apps/umbrella-consumer` already uses (which proves Xcode 26 opens and builds it). Not generated: Tuist or
XcodeGen would add a toolchain to a repo that has none, for a project with a handful of files. `app/app-ios/` has no
`build.gradle.kts`, so `settings.gradle.kts` does not pick it up as a module.

- It consumes `:umbrella` straight from Gradle through a Run Script build phase that runs
  `./gradlew :umbrella:embedAndSignAppleFrameworkForXcode` from a path relative to `$SRCROOT`, so it works from any
  clone or worktree, unlike Ugglan's loop. `JAVA_HOME` is resolved at build time (for example
  `/usr/libexec/java_home -v 21`), never hardcoded. It consumes the same framework Ugglan would, so the shell's size
  numbers are the real ones.
- Login: BankID by QR code through `authlib`, as the desktop app does: `startLoginAttempt`, then
  `observeLoginStatus`, rendering each `LoginStatusResult.Pending.liveQrCodeData` as a QR code with Core Image, then
  `exchange` with the authorization code. No OTP, no BankID app handover.
- Tokens: stored in the Keychain by a Swift implementation of the token storage interface, refreshed through
  `authlib`'s `exchange(RefreshTokenGrant)` inside the shell's `AccessTokenFetcher`.
- A logout button, a demo mode switch if cheap, and one screen: claim history in SwiftUI, plain system controls.
- Simulator only, signed for local running. It is a test host, never shipped. No design system, no navigation beyond
  a `NavigationStack`, no localisation: the shell's strings are English literals in Swift.
- `micro-apps/umbrella-consumer` is superseded by it. Its Swift conformers are a starting point; its hardcoded token is
  not copied. Whether to delete it is an open question.

Reason: one place to run the shared layer from Swift in seconds, from any checkout. Cost if wrong: a hand-made project
may need re-creating when Xcode's project format moves on; at a handful of files that is under an hour.

### C. Swift consumes the ViewModel (designer's call)

For the first proof Swift consumes `ClaimHistoryViewModel`, not the bare presenter. Presenters are constructed by hand
inside ViewModels and are not in the DI graph, so the ViewModel route reuses the generated ViewModel map unchanged and
needs no DI change; the measured +1.3 MB already included a ViewModel.

Swift owns the lifetime: one ViewModel store per screen and an explicit clear when the screen's Swift owner is
deinitialised. `MoleculeViewModel` shares state with `WhileSubscribed(5.seconds)`, so without a clear the presenter
keeps running 5 s after the last collector, and without a store `viewModelScope` is never cancelled. State arrives on
the main thread: the native `mainDispatcher` is `Dispatchers.Main` plus a `DisplayLinkClock`. Whether recomposition
stalls while the app is in the background (display link paused) is unverified; the shell checks it.

Revisit after the bridge comparison. Cost if wrong: a later switch to presenters needs presenters in the graph (a
second processor or hand-written factories), which is the work this call avoids now.

### D. Bridge comparison (designer's call)

In the shell, on the claim history ViewModel, three candidates on top of the same Kotlin facade (E):

1. Plain Objective-C export with a small hand-written Kotlin facade: callback-based state observation that returns a
   cancel handle, no library.
2. KMP-NativeCoroutines, annotations on the facade only (never in `commonMain` presenters).
3. SKIE, if a release supports Kotlin 2.4.21. That is unverified: the builder checks first and drops SKIE if not.

Kotlin's Swift export is noted and not pursued unless the builder finds it usable in 2.4.21.

Criteria, each written up with the Swift code that shows it:

- How sealed state and events read in Swift: an exhaustive `switch` or `as?` casts with a `default`.
- Cancellation and lifetime: leaving the screen cancels collection and clears the ViewModel; no retain cycle across
  the boundary (an `assertDeallocates`-style check like Ugglan's ViewModel tests).
- Main-thread delivery.
- Behaviour on a Kotlin exception (an uncaught one crossing into Swift terminates the app; unverified per candidate).
- Build time and shipped size each adds.
- Kotlin-side code per screen: annotations, facade lines, generated surface.

The outcome is a recommendation to the owner, not a silent choice. Ugglan's step uses the recommended bridge only after
the owner has seen it; until then it uses plain export.

### E. Public surface (designer's call)

ViewModels, state and event types are `internal` today, and Kotlin/Native exports nothing internal. For the first
proof, add a narrow public facade in `feature-claim-history`'s `nativeMain`: a class that takes the graph's ViewModel
factory, owns a `ViewModelStore`, creates the internal `ClaimHistoryViewModel`, maps its state to public iOS-only
types, forwards `reload()`, and has `close()`. The public types are plain and Swift-friendly: a sealed state of
`Loading`, `Empty`, `Failed`, `Content(items)`; items with `id`, `claimType: String?`, `submittedAt` as something Swift
turns into a `Date` without kotlinx-datetime in its signature, an outcome enum, and whether the row is a partner claim.
No Arrow types, no Apollo types.

The partner flag does not exist in `ClaimHistory` today; Ugglan's detail route needs it. Adding it to the internal
model is a small `commonMain` change that Android ignores.

Reason: Android consumers (`:app-shell`) see no widened visibility, `commonMain` gains nothing Swift-specific, and the
facade is where all three bridge candidates attach. Cost if wrong: one mapping of about 30 lines per screen, which
becomes noise across 26 features. If the comparison shows that a bridge reads well on public `commonMain` types,
making state types public per feature becomes the cheaper option, and is a decision for the owner then.

### F. The native graph (designer's call)

`IosGraph` lives in `:umbrella`'s `nativeMain`, shaped like `DesktopGraph`:
`@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])`, extending `ViewModelGraph`,
with a `MetroViewModelFactory` binding like `DesktopViewModelFactory`. Swift creates it through a plain Kotlin function
(Metro's own factory accessor is not Swift-callable), as the removed `initDiGraph` did. `:umbrella` is the one module
both the shell and Ugglan link, so both run the same graph. The graph's own bindings, which need no Swift input:
`AuthRepository` and `@AuthHttpClientEngine` over Darwin, `kotlin.time.Clock` as `Clock.System`, `TimeZone` as the
system default (desktop provides the last two the same way).

`:umbrella` keeps exporting `authlib` and `logging-public`, and adds `feature-claim-history`, whose iOS public API is
only the facade once its key and entries move to `jvmAndAndroidMain`. Swift also has to see the interfaces it
implements; whether that needs their modules exported or they appear through the graph function's signature is
checked against the generated header, and the minimum is exported (an exported module's whole public API lands in the
header and the link).

Swift-supplied inputs. One rule for all of them: Swift never has to produce a Kotlin `Flow`. Where the Kotlin
interface returns one, the native binding is a small Kotlin adapter that Swift pushes values into or that reads
through a suspend call.

| Input | Interface today | iOS shell passes | Ugglan passes |
|---|---|---|---|
| `AppBuildConfig` (`core-build-constants`) | Yes. A Swift conformer must implement `platformName` (`"ios"`) | Constants: staging flavour, bundle id, version | Values from `Bundle.main` and `UIDevice` (prior art `IosAppBuildConfig`, removed in `73b577c70`) |
| `AccessTokenFetcher` (`network-clients`) | Yes; no native binding | Keychain read, refresh through `authlib` when the access token is near expiry | `TokenRefresher.shared.refreshIfNeeded()`, then the Keychain token (prior art `KeychainAccessTokenFetcher.swift`, removed in `73b577c70`) |
| Token storage (`auth-core-public`) | Needs one: `AuthTokenStorage` becomes an interface in step 0 | Keychain | `KeychainHelper` (service `oAuthorizationToken`) |
| `DeviceIdFetcher` (`core-datastore-public`) | Yes; no native binding | A UUID kept in `UserDefaults` | `UserDefaults` key `hedvig-device-identifier`, so both clients send the same id |
| `LanguageStorage` (`language-core` `nativeMain`) | Yes; `NativeLanguageService` is bound over it | Fixed `en-SE` or `sv-SE` | `Localization.Locale` |
| Demo mode (`DemoManager`, `core-demo-mode`) | Yes, but returns a `Flow`; native binding is an adapter after step 0 | The shell's switch | `UgglanStore.isDemoMode` |
| `FeatureManager` (`feature-flags`) | Yes; `IosFeatureManager` exists, not bound | Static values | A lookup in its Unleash client |
| `EventTrackingClient` (`tracking-core`) | Yes; no native binding | Logs to the console | Its Firebase-backed client |
| Clock, time zone | n/a | Nothing: the graph provides them | Nothing |

`FeatureManager` and `EventTrackingClient` are needed only if something the slice reaches injects them; Metro reports
a missing binding at compile time, which is the intended way to find out. No `Backstack`, deep link matcher,
`ImageLoader` or serializers module goes into the native graph.

On native, `AuthTokenServiceImpl` is bound over the Swift token storage, so `MemberIdService` and the logout
interceptor work, while the access token on requests comes from Swift's `AccessTokenFetcher`; Kotlin's
`AndroidAccessTokenProvider` and its refresh are not bound on native.

Session events and initializables (designer's call). `AuthEventBroadcaster` is an `Initializable`, so the Kotlin
function that creates the graph also runs the graph's initializables before returning it; Swift never calls them. The
object that function returns (the graph holder) has one Kotlin function Swift calls when a session starts or ends,
which fires `AuthEventStorage.loggedIn` or `loggedOut` and so clears the Kotlin cache; it does not write tokens. In the
shell it is called from its login and logout code. In Ugglan the call has to sit in the App target, since only the App
target links `HedvigShared`; `hGraphQL/Sources/ApolloClient.swift` (`deleteToken()` at line 107, `saveToken` at line
136) cannot import it. And `saveToken`, through `handleAuthTokenSuccessResult`, also runs on every token refresh
(`AuthenticationClientAuthLib.exchange(refreshToken:)`), which would clear the Kotlin cache on each refresh. So Ugglan
calls it next to `handleAuthTokenSuccessResult` in `AuthenticationClientAuthLib.exchange(code:)` (line 226, the login
exchange) and next to `ApolloClient.deleteToken()` in `AppDelegate.logout()` (line 62).

`ApolloNormalizedCacheAuthEventListener` is in `apollo-auth-listeners`, a `hedvig.jvm.library` module (`src/main`).
Before a second member logs in on iOS it is converted in step 2: the plugin becomes `hedvig.multiplatform.library.ios`
(plus `.android` only if needed), `src/main/kotlin` moves to `src/commonMain/kotlin`, dependencies move into
`commonMain`. The listener is 24 lines with no platform code.

Reason: one graph for both Swift hosts and missing bindings fail at compile time. Cost if wrong: moving the graph to
its own module later is a file move.

### G. Two GraphQL clients side by side (designer's call, two rows to the owner)

On iOS the Kotlin client and Apollo iOS run next to each other. Designer's default: the Kotlin client sends the
headers iOS sends today plus Kotlin's own, and where both send the same header the iOS value wins; token refresh stays
owned by iOS through the injected fetcher; Swift tells the Kotlin side about login and logout through an explicit call
that fires `AuthEventStorage`, so the Kotlin cache is cleared.

| Concern | iOS today | Kotlin today | Position for the experiment |
|---|---|---|---|
| `Accept-Language`, `hedvig-language` | `sv-SE`/`en-SE` from `Localization.Locale`, re-read per operation | From `LanguageService`, re-read per request | Match iOS, through Swift's `LanguageStorage` |
| `User-Agent` | `<bundleId> <version> (iOS <system version>)` | Built in `CommonHedvigBuildConstants` with a literal `(Android ...` | Match iOS. Needs a per-platform user agent in `core-build-constants` (a small seam) |
| `Hedvig-App-Version` | `ios;<short version>` | `<platformName lowercased>;<versionName>` | Already matches when Swift supplies `platformName = "ios"` |
| `hedvig-device-id` | `UserDefaults` `hedvig-device-identifier` | `DeviceIdFetcher` | Match iOS, through Swift's `DeviceIdFetcher` |
| `Hedvig-TimeZone` | `TimeZone.current.identifier` | Not sent | Match iOS: add on native only |
| `X-Build-Version`, `X-App-Version`, `X-System-Version`, `X-Platform`, `X-Model` | Not sent | Sent | Keep Kotlin's |
| `apollographql-client-name`/`-version` | `iOS:<bundleId>` and version through client awareness (header names unverified) | Package id and version name | Match iOS once the builder confirms what iOS sends |
| `Authorization` | Keychain token after a refresh check | `AccessTokenFetcher` | Match iOS: the fetcher is Swift's |
| Timeouts | 30 s per query, 10 s per mutation, whole request | Connect 15 s, socket 30 s per stall | Keep Kotlin's |
| Cache | Normalized in memory, but `networkOnly` and no cache writes, so nothing is cached in practice | 10 MB memory normalized cache, fetch policy per use case | Keep Kotlin's, cleared on Swift's login and logout call |
| Token refresh | `TokenRefresher` | `AndroidAccessTokenProvider` with a mutex | Match iOS: refresh stays in Swift |
| Logout on `UNAUTHENTICATED` | None; iOS logs out only when a refresh fails | `LogoutOnUnauthenticatedInterceptor` clears tokens unless in demo mode | **Open, owner** |
| Datadog tracing | `URLSession` instrumentation and first-party host tracing | `datadogKtorPlugin` in `mobileMain`, on Android and native | **Open, owner** |

Two notes for the open rows. If Kotlin's logout applies on iOS as it stands, it clears the Keychain through the token
storage interface without telling Ugglan's UI, so whichever way the owner decides, the native path has to either route
to Ugglan's force logout hook or be switched off. Kotlin requests go through Ktor's Darwin engine, not Ugglan's
`URLSession` delegate, so Ugglan's Datadog instrumentation does not see them; the Kotlin Datadog plugin on iOS needs the
Datadog iOS frameworks linked with the shared framework, and whether KMP 1.8.0 works with `dd-sdk-ios` 3.17.0 is
unverified.

Interim defaults until the owner answers (designer's call): on native, logout on `UNAUTHENTICATED` is off, and there
is no Kotlin-side Datadog tracing. Today both run on native: `addHedvigInterceptors` adds the logout interceptor in
`commonMain`, and `installDatadogKtorPlugin` has one actual in `mobileMain` (Android and native), so the defaults need
a native-only switch for the first and a native no-op actual for the second (splitting the `mobileMain` actual so
Android keeps its plugin). The shell (steps 2 to 4) runs on these defaults. Both questions must be answered before
step 5, since both change Ugglan's behaviour.

### H. The Ugglan step (designer's call)

An experiment branch in Ugglan, not for merge, built against a local umbrella from the `android/` clone with the
experiment's Android branch checked out. It follows the existing auth pattern: Swift feature modules cannot import
`HedvigShared` (only the App target links it), so a protocol in the `Claims` module with Swift-native state, an
implementation over the Kotlin facade in the App target, registered in `AppDelegate+DI.swift` in the non-demo branch.
The demo branch keeps the Swift demo client, since claim history has no Kotlin demo implementation.
`ClaimHistoryScreen` gains the loading and error states it lacks today and drops `ClaimsStore.historyClaims` (no other
reader exists outside tests). A tap routes with the claim id and the partner flag to `ClaimDetailView(claim: nil,
type:)`.

Recorded constraints: the local loop is hardwired to `../android`; Ugglan master needs Xcode 27.2 or later while the
owner's machine has 26.4.1, so the owner's local workaround (a local patch of `ToolbarButtonView`, never committed, or
a checkout from before #2788) applies; simulator builds must be signed; Xcode Cloud and the `verify-generated` job
cannot build local mode, so the branch never builds in CI; never commit a project generation made in local mode;
`use-local-umbrella.sh` refuses to run with Xcode open and wipes DerivedData.

Reason: the shell proves the mechanics, Ugglan proves the fit with real house patterns. Cost if wrong: if Ugglan cannot
build locally at all on the owner's machine, the experiment ends at the shell and says so.

### I. Size budget (designer's call)

After each step, report the shipped size of the device slice: `./gradlew :umbrella:linkReleaseFrameworkIosArm64`, then
`size -m` on the `ios-arm64` binary, summing `__TEXT` and non-zerofill `__DATA` sections, plus `nm | grep -c kfun:` for
the symbol count. Quote shipped bytes, never zip size. First re-measure the integration branch's baseline, since it is
not `develop`'s 3.59 MB.

Stop and report to the owner if the shared framework exceeds 15 MB shipped. The owner's pain threshold is 30 to 40 MB;
15 MB is the designer's early warning, high enough for Apollo, the runtime and a feature, low enough to catch Compose
UI or a stray large dependency long before the threshold.

### J. Later work, out of scope here (designer's call)

- The navigation change, piloted on `feature-payments` afterwards (`ManualChargeViewModel` is its only `Backstack`
  user). The recon's options: B, a per-feature navigator interface injected into presenters, or C, navigation as a
  presenter output.
- The 44 leak files in 24 modules, beyond what claim history needs.
- `TextFieldState` presenters (decision 4).
- Moving policy (`TryShowOnboardingEffect` and the other effects) out of `HedvigApp`.
- Converting the other 25 features.
- Shipping, publishing an umbrella, or changing what Ugglan pins.

## Verification

| Step | Android | iOS |
|---|---|---|
| 0 | `checkCommonMainImports`, the touched modules' tests (same count before and after), `:app:assembleDebug`, `:app-desktop:jvmTest`. `javap` of `AppGraph$Impl` and `ActivityRetainedGraphImpl` against the parent commit (recipe in `CLAUDE.md`): 87 ViewModels, 21 deep link matcher providers, 111 keys, and only the `AuthTokenStorage` binding changed (step 0, item 2). A debug build on a device: log in, kill, relaunch still logged in, log out | `compileKotlinIosSimulatorArm64` and `compileKotlinIosArm64` of `language-core`, `auth-core-public`, `core-demo-mode`, `network-clients`; the iOS compile classpath of `network-clients` has no `components-resources` |
| 1 | As step 0, plus the key count and `ExhaustiveBackStackSerializationTest` (the key moves to `jvmAndAndroidMain`) and the desktop app opening claim history | Both iOS compiles of `feature-claim-history`; the guard passes |
| 2 | Unchanged from step 1 | `:umbrella` debug framework link for the simulator; the generated header shows the facade and the input interfaces and nothing from Compose UI; size |
| 3 | n/a | The shell on the simulator: QR login on staging, the four states (error by switching the network off, retry by switching it on, empty and content with suitable staging members), leaving the screen clears the ViewModel (log) and no request follows (network log), logout then login as another member shows that member's claims; size |
| 4 | n/a | The comparison write-up with code, timings and sizes |
| 5 | n/a | The same checks as step 3 in Ugglan, plus the partner claim route and demo mode still showing the Swift demo data |

Verification logistics. The owner scans the staging BankID QR code for the shell, as for the desktop app; one scan
lasts until the refresh token expires. The staging test member is 666227461. Whether staging has members with no
claims, with regular history claims and with partner history claims is established by the builder at step 3 by
querying staging, not assumed; the empty and partner cases may need the owner to point at another member.

## Risks and what stops the experiment

- **Size.** Above 15 MB shipped at any step: stop and report (I).
- **Compose UI or Skiko in the link.** Any Skiko or Compose UI symbol in the framework: stop and find the edge before
  continuing.
- **Android gets worse.** If a step needs a change to Android behaviour or a non-trivial restructuring of shared code
  to suit Swift, apply the skip rule: skip that piece and record it, do not change Android.
- **The KSP metadata pass with UI in `jvmAndAndroidMain`.** Not verified by any compile yet; `feature-help-center`
  proves the metadata pass with iOS targets but still has UI in `commonMain`. If the ViewModel processor cannot resolve
  the module on the metadata compilation, that is a convention fix, and if it is not a small one, report it.
- **Swift cannot hold the lifetime correctly** with any bridge (leaks, presenter running after the screen is gone):
  report it as the result of step 4; it is a finding, not a failure.
- **Ugglan cannot build on the owner's machine** (Xcode version): the experiment ends at the shell and says so.
- **The display-link clock in the background** stalls state delivery: report; it affects every shared screen.
- **Two Kotlin frameworks in one app** cannot share objects (unverified); keep everything in the one umbrella.

## Left to the builder

- The facade's exact names and shape, and how the input adapters are written, within the constraints in E and F.
- Where `Language.label` goes among its two UI callers, and the name of the opt-in guard flag.
- The shell's Swift structure, its Keychain wrapper and QR rendering, and whether its demo switch is cheap enough.
- How `Hedvig-TimeZone` and the iOS user agent are added on native.
- Which modules `:umbrella` must export for the input interfaces, judged from the header.
- Whether a `commonTest` smoke test on `iosSimulatorArm64Test` is worth adding for the facade.
- Branch names and the order of commits within a step.

## Open questions for the owner

1. Should Kotlin's logout on `UNAUTHENTICATED` apply on iOS (routed to Ugglan's force logout), or be off on native for
   the experiment, as iOS today logs out only when a refresh fails?
2. Datadog on iOS: link the Kotlin Datadog Ktor plugin (needs the Datadog iOS frameworks with the shared framework;
   compatibility with Ugglan's 3.17.0 unverified), or no Kotlin-side tracing on iOS for the experiment?
3. Once the shell works, delete `micro-apps/umbrella-consumer` (and the access token committed in it)?

Questions 1 and 2 do not block the shell: until they are answered, native runs with logout on `UNAUTHENTICATED` off
and no Kotlin-side Datadog tracing (G). Both must be answered before step 5.

## Decisions and who made them

Stylianos: decisions 1 to 11 above, the intent, and that nothing ships.

Designer's calls, open to override: A (order), B (shell at `app/app-ios/`, checked in, embed and sign from Gradle), C
(ViewModel, Swift-owned lifetime), D (three-way comparison ending in a recommendation), E (public facade in
`nativeMain`), F (graph in `:umbrella`, Swift inputs, no Swift-produced flows), G (parity positions), H (Ugglan
pattern), I (15 MB early warning), J (scope cut).
