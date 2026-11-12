# The Hedvig app on desktop: design

Date: 2026-11-12. Measured against `develop` at `229bd79df0`.

## Intent

The long-term goal is to share as much code as possible with the iOS app (Ugglan). The last
attempt shared Compose screens into SwiftUI, felt clunky to the iOS developers, and was replaced
with a native implementation (ugglan PRs "native puppy guide stack", 1/9 to 9/9).

This project takes a different route to the same goal. It makes the whole Android app run as a
real desktop (JVM) application. Desktop is the cheapest second platform available, since it is a
JVM target like Android, and a working desktop app is proof that the app does not need Android to
exist. Once that is true, bringing the app to iOS is a matter of adding iOS pieces to a shell that
already runs on two platforms, instead of rethinking the app from scratch.

Nobody will ship the desktop app. It is a proof and a forcing function. Whether iOS later takes
UI, or only the non-UI layers, is decided after this project and is not part of it.

## Goals

1. A desktop application that is the real Hedvig app: a member logs in with BankID through the QR
   code and can use the app's functionality against the real backend.
2. Every feature's code (UI, presenters, ViewModels, use cases, navigation keys) lives in
   `commonMain`. Android-specific and desktop-specific code is confined to small platform seams.
3. The app shell (single back stack, tab runs, session reconciliation, entry registration, the
   root composable) is shared between Android and desktop, so iOS later adds a third host for the
   same shell.
4. The desktop app cannot silently rot: CI compiles it on every PR.

## Non-goals

- Shipping, packaging, signing or distributing the desktop app. `./gradlew` run on a developer
  machine is the only way it is started. Only macOS is verified.
- Desktop-specific UX. The app renders the same adaptive layouts it already has (the navigation
  suite already switches on window size class). No menus, shortcuts, multi-window or tray work.
- Adding iOS targets to the modules converted here, changing what `:umbrella` exports, or any
  change in the ugglan repository.
- Push notifications, deep links from outside the app, Firebase, Datadog RUM and Crashlytics on
  desktop.
- Converting Android platform infrastructure that has no meaning off Android (listed under
  "Stays Android").

## Anti-goals

These make the project a failure even if the desktop app runs.

- **Android gets worse.** No behaviour change, no added indirection in feature code whose only
  purpose is desktop, no regression in startup, APK size or build time beyond noise. Every
  conversion PR is behaviour-neutral on Android.
- **Desktop goes green while the iOS distance stays the same.** A module on JVM plus Android
  targets lets `commonMain` reach `java.*` and JVM-only libraries, because no metadata compilation
  runs without a native target. Code that takes that shortcut works on desktop and has to be
  rewritten for iOS. See "The commonMain rule".
- **A fake desktop app.** A demo-mode walkthrough, or stubs returning canned data, does not meet
  goal 1.

## The skip rule

Decided by Stylianos: if a piece of functionality depends on very Android-specific implementation,
and supporting it on desktop would mean complicating `commonMain` in a way that makes the Android
code worse, skip it on desktop.

How to apply it:

- A platform seam is not a complication. An `expect`/`actual`, or an interface bound per platform
  in Metro, with a trivial desktop side, is the normal tool and always acceptable.
- What triggers a skip is restructuring shared code, adding an abstraction layer, or threading
  platform conditionals through presenters and screens, only to accommodate desktop.
- A skipped piece of functionality shows a plain "Not available on desktop" state where the member
  would reach it. It never crashes and is never hidden behind a silent no-op that looks like a bug.
- Every skip is recorded in the skip register at the bottom of this document, with one line of
  reason. The register is the honest measure of how real the desktop app is.

## Current state

152 Gradle modules.

| Kind | Modules | Notes |
|---|---|---|
| KMP with a JVM target | 80 | 46 of them also compile for iOS |
| Plain JVM library (`hedvig.jvm.library`) | 38 | about 16 are `-test` utilities |
| Android-only library (`hedvig.android.library`) | 32 | the work |
| Android application | 2 | `:app`, `micro-apps/design-showcase` |

- About 3.0 MB of Kotlin is in `commonMain`, about 2.4 MB in Android-only modules.
- No Android-only module uses `R.string`/`R.drawable`. Strings and drawables already come from
  Compose Multiplatform resources (`hedvig.resources.Res`).
- The Android-only feature modules are nearly pure Compose. Files with platform imports, out of
  total files: payments 0/34, insurances 0/26, forever 0/2, member-reminders-ui 0/4, onboarding
  1/32, payin-account 1/23, insurance-certificate 1/9, home 2/14, profile 3/30,
  terminate-insurance 3/24, travel-certificate 3/20, forever-ui 3/13, claim-details 5/9,
  connect-payment-trustly 5/16, login 6/16, chat 11/29.
- In `:app`, these files have no `android.*` imports: `BackstackController`, `TopLevelRunLogic`,
  `SessionReconciler`, `HedvigEntryProvider`, `NavigationSuite`, `HedvigAppState`,
  `NavRetainedViewModel`, `ActivityRetainedGraph`, `MergedMetroViewModelFactory`,
  `HedvigContentKeys`, `ScreenName`, `ScreenParameterExtractor`, `CurrentDestinationHolder`,
  `DeepLinkFirstUriHandler`, `GlobalHedvigSnackBar`. They are still tied to Android in three ways
  that the shell move has to undo:
  - They import nav keys and types from Android-only feature modules. `BackstackController` uses
    `HomeKey` and `LoginKey`; `TopLevelTabStartDestination` uses the five tab root keys;
    `HedvigApp`, `ActivityRetainedGraph` and `NavRetainedViewModel` use `OnboardingGate`,
    `OnboardingKey` and `ResetOnboardingSeenUseCase`; `HedvigEntryProvider` imports every
    feature's entries function.
  - `HedvigApp` and `HedvigEntryProvider` take `AndroidAppHost`, whose interface exposes
    `SystemBarStyle` and the Play review dialog. `HedvigApp` also uses `SafeAndroidUriHandler`,
    `android.graphics.Color` and `LocalContext`.
  - `HedvigApp` calls Datadog's `Navigation3TrackingEffect`, which keeps 18 RUM metrics alive
    (see "Working with Datadog RUM metrics" in `CLAUDE.md`).
- The navigation UI infrastructure the shell renders through (`Decorators`,
  `BottomSheetSceneStrategy`, `NavSuiteSceneDecorator`, `NavigationSuiteType`, the retained
  decorators) sits in `androidMain` of `:navigation-compose`, a module that also compiles for iOS.
  `navigation3-ui` and `lifecycle-viewmodel-navigation3` are `androidMain` dependencies there.
- Precedent to copy, with a caveat: `feature-claim-chat` is a KMP module with `commonMain` screens
  and ViewModels, `navKeys()`, `viewModels()`, and six `jvmMain` actuals. Its entries function,
  deep links and demo switchers (the only bindings for its use cases) are still in `androidMain`,
  as are the entries of `feature-claim-history` and `feature-remove-addons`. So no feature is
  reachable from a non-Android host today. `IosGraph` in `:shareddi` shows a non-Android Metro
  graph that collapses `ActivityRetainedScope` into `AppScope` with `additionalScopes`.
- No `jvmMain` source set contributes a Metro binding. Platform bindings exist only in
  `androidMain` (`core-datastore-public`, `core-file-upload`, `feature-flags`, `language-core`,
  `network-clients`, `permission-core`). The JVM code exists; the wiring does not.
- `navKeys()` wires its processor to `kspAndroid` only, so the JVM target receives no
  `SerializersModule` contributions.
- `platformName`, which feeds the `X-Platform` and `Hedvig-App-Version` headers, is `"ANDROID"`
  on Android, `"jvm"` on JVM and `"ios"` on native.
- BankID login already has a QR path: `SwedishLoginDestination` renders `HedvigQrCode` from
  `bankIdLiveQrCodeData` when the BankID app cannot be opened. Desktop always takes that path.
- 29 `jvmMain` actuals already exist; some are stubs (photo capture, audio recording, Rive, a
  no-op feature flag provider).

## Design

### The commonMain rule

A module counts as converted only when:

1. Its feature code is in `commonMain`. `jvmAndAndroidMain` is not an acceptable home for screens,
   presenters, ViewModels or use cases. It stays available for genuine JVM-shared helpers that
   already live there.
2. `commonMain` imports nothing from `java.*`, `javax.*`, `android.*`, or `androidx.*` artifacts
   that are Android-only. A CI check enforces the `java.*`/`javax.*`/`android.*` part by scanning
   `src/commonMain` of every module (see "CI").
3. Third-party libraries used from `commonMain` publish iOS artifacts. If a library does not, the
   usage goes behind a seam.

Depending on an in-repo `hedvig.jvm.library` module from `commonMain` is allowed during this
project, because those modules are pure Kotlin and converting them is mechanical. They are
converted in the last phase so that the later iOS step is a plugin swap.

### Module shape

A converted module uses `hedvig.multiplatform.library` plus `hedvig.multiplatform.library.android`
(JVM and Android targets), exactly like `feature-claim-chat`. Module names do not change. Files
move with `git mv` so history follows. `src/main/kotlin` becomes `src/commonMain/kotlin`;
the handful of platform files go to `src/androidMain/kotlin` with a `src/jvmMain/kotlin`
counterpart.

Existing unit tests move to `src/jvmTest` unchanged (JUnit, AssertK, Turbine keep working there;
53 test files already live in `jvmTest`). Rewriting tests for `commonTest` is not part of this
project.

### Platform seams

Follow the existing Metro KMP rule in `CLAUDE.md`: bind per platform, never put a
platform-overridable `@ContributesBinding` default in `commonMain`. A missing desktop binding must
be a compile error in the desktop graph, not a silent fallback.

Composable seams (things a screen calls) are `expect`/`actual` functions or small interfaces
remembered through an `expect fun rememberX()`, as `feature-claim-chat` does for the file picker
and permission state.

Desktop side of each seam:

| Concern | Desktop behaviour |
|---|---|
| Preferences, auth tokens (DataStore) | Real. File under a per-user app directory. |
| Chat database (Room) | Real. Bundled SQLite driver, file in the same directory. |
| Apollo normalized cache | Real, same as Android (in memory). |
| HTTP, GraphQL, auth | Real. The code is already `commonMain` + `jvmMain`; the JVM bindings are new. |
| BankID app launch | Never available, so login shows the QR code. |
| Open URL, email, phone | `java.awt.Desktop` where it applies, otherwise "Not available". |
| Share sheet | Copies the shared text to the system clipboard. |
| App settings, Play Store, in-app review | Not available. |
| File picking (claims, chat attachments) | Real, native file dialog. |
| Downloaded PDFs (certificates, claim documents) | Open in the system's PDF viewer. If that fails, a dialog shows where the file was saved. |
| Camera capture, audio recording | Not available (stubs exist today). |
| Push notifications and the notification permission prompt | Not applicable. The permission reads as granted, meaning there is nothing to ask for, so no prompt or reminder appears. The Settings notifications row is not shown. |
| Feature flags | Real values from Unleash if cheap, otherwise static. See "Open risks". |
| Tracking, Datadog, Firebase, Crashlytics | No-op. Logging goes to stdout through `logcat`. |
| System bars, edge-to-edge, screen-on flag | No-op. |
| Language selection | Real, in-app only. |
| Back | Top app bar arrows as today, plus Escape mapped to system back. |
| Deep links | In-app links resolve through the shared matcher. Nothing arrives from outside. |
| Back stack persistence | In memory. There is no process death to survive. |
| Rive animations, login background video | Static fallback (still image). |

### The shared app shell

A new KMP module, `:app-shell` (JVM and Android targets), receives the Android-free parts of
`:app`: the back stack controller and its run logic, session reconciliation, `HedvigEntryProvider`
with all cross-feature lambda wiring, `HedvigApp` and its state, the navigation suite, the content
key and screen name helpers, the snackbar, and the in-app URI handling. The module is not named
`feature-*`, so the feature isolation rule lets it depend on every feature, as `:app` does today.

Three things make that move possible:

- **Keys the shell references move to `-navigation` modules.** The tab root keys, `LoginKey`,
  and the onboarding key and gate move into each feature's `feature-{name}-navigation` module
  (KMP), created where it does not exist. The shell is a cross-feature consumer of those keys,
  which is what `-navigation` modules are for. This lets the shell core move in phase 1, before
  the features themselves are converted.
- **The entry provider takes host entries.** The shared `hedvigEntryProvider` registers the
  entries of every converted feature and accepts one host-supplied block for the rest. On Android
  that block holds the entries of features not yet converted, plus the Android-only ones that
  stay (`feature-impersonation`). On desktop it holds "Not available on desktop" placeholders for
  the same keys. Each feature conversion moves one registration from the host blocks into the
  shared provider. When the project ends the Android block holds only the permanently
  Android-only entries, so the hook is not scaffolding that needs removing.
- **`AndroidAppHost` becomes a platform-neutral host interface.** Anything that is Android by
  type (`SystemBarStyle`, the Play review dialog) moves behind it or stays in `:app`. URI
  handling and view tracking are supplied by the host: Android passes exactly what it uses today,
  so `Navigation3TrackingEffect` keeps receiving the same back stack entries and produces the
  same view names. That seam must be verified against Datadog on a device, not assumed.

Entries functions, deep link matchers and demo switchers that sit in `androidMain` of
already-KMP features (`feature-claim-chat`, `feature-claim-history`, `feature-remove-addons` and
any other) move to `commonMain` as part of making those features reachable.

The navigation serialization tests (`ExhaustiveBackStackSerializationTest`,
`BackStackSerializationTest`) stay in `:app` as Android host tests, because only the Android
target gets nav key serializer contributions and only Android persists the back stack.

`:app` keeps everything Android: `MainActivity`, `HedvigApplication`, `AppGraph`, startup
initializers, `NavigationStateBridge` and saved-state persistence, `ExternalNavigatorImpl`, the
notification senders, WorkManager, Firebase and Datadog wiring. After the move `:app` is a thin
Android host of `:app-shell`.

Whether the graph extension `ActivityRetainedGraph` moves to `:app-shell` or each host declares
its own is left to the builder. The constraint is that Android keeps its per-Activity scope and
desktop collapses it, as `IosGraph` does.

### Modules that already compile for iOS

46 modules use `hedvig.multiplatform.library.ios`. When desktop needs code from one of them that
lives in `androidMain` today, the code moves to `commonMain` and keeps compiling for iOS. Where
that needs a `nativeMain` actual, adding one is in scope, and a minimal one is fine. No module
loses its iOS targets in this project, and `:umbrella` keeps exporting what it exports today
(`authlib` and `logging-public`), so none of this changes the framework iOS consumes.

The one expected exception is navigation UI infrastructure in `:navigation-compose`. Phase 0
checks whether `navigation3-ui` and `lifecycle-viewmodel-navigation3` publish both JVM and iOS
variants. If both exist, that code goes to `commonMain`. If only JVM exists, it may go to
`jvmAndAndroidMain`, and that is recorded in the iOS debt register at the bottom of this document.
The register is for exactly this case: shared-in-practice code that iOS cannot compile yet for a
reason outside this repo.

### The desktop host

A new module, `:app-desktop`, a Compose Desktop application with a `main()` that opens one
resizable window and renders `HedvigApp` from `:app-shell`. It declares `DesktopGraph`, a Metro
`@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])`, and holds
the desktop-only bindings that do not belong to a library module's `jvmMain`.

- Started with `./gradlew :app-desktop:run`.
- Talks to staging by default. A Gradle property switches it to production.
- Identifies itself to the backend as the Android app. `platformName` is `"jvm"` on the JVM
  target today, so the desktop host overrides the build constants it provides to send the Android
  values for `X-Platform`, `User-Agent` and `Hedvig-App-Version`. The `jvmMain` actual is not
  changed, since JVM unit tests use it (see "Open risks").
- The default window is phone-sized so the default experience matches the app; resizing exercises
  the adaptive layouts.
- The existing empty `micro-apps/design-showcase-desktop` skeleton is unrelated and left alone.

### Stays Android

These modules keep `hedvig.android.library` and are not converted. Desktop gets its behaviour from
the seam table above.

`datadog-android`, `tracking-datadog`, `tracking-firebase`, `notification-firebase`,
`notification-core`, `experiments-unleash`, `language-migration`, `logging-device-model`,
`database-android`, `core-rive`, `feature-impersonation`, `accessibility-test`,
`micro-apps/design-showcase`.

`compose-webview` also stays Android. Its API is Android-typed throughout and Trustly depends on
that, so each caller wraps its own web view step in a seam whose desktop side is "Not available
on desktop". `notification-permission` became a KMP module with actuals.

## Phases

Each phase is a series of small PRs, normally one module per PR, each mergeable on its own because
Android behaviour does not change. A module is converted when no large open branch is rewriting it,
since moving every file in a module conflicts with in-flight work on that module.

**Phase 0: spike, throwaway.** Prove the things that could kill the idea before any production
change: Navigation 3's `NavDisplay` and the scene strategies running on the JVM target, and
whether their artifacts also exist for iOS; Metro ViewModel resolution (`metroViewModel()`) in a
desktop graph; a real BankID QR login against staging; the backend accepting the desktop client
with Android identity headers. The spike may hack freely (copy files, move entries out of
`androidMain`, hand-write bindings) since nothing in it is kept. Exit: a desktop window where a
staging member logs in by QR and reaches one authenticated screen of an already-KMP feature. If
any of these fails, stop and bring the finding back before continuing. The spike branch is not
merged.

**Phase 1: shell.** Move the keys the shell references into `-navigation` modules. Move the
navigation UI infrastructure out of `androidMain`. Move the entries and bindings of already-KMP
features to `commonMain`. Create `:app-shell` and move the shell out of `:app`. Add the JVM
bindings for the existing platform seams. Create `:app-desktop` with `DesktopGraph`. This is the
largest phase and is several PRs, in that order. Exit: the desktop app starts, logs in by QR, shows
the tab scaffold with every already-KMP feature working, and placeholder entries for features not
yet converted. Android unchanged. CI compiles `:app-desktop`.

**Phase 2: leaf UI modules.** `core-icons`, `forever-ui`, `member-reminders-ui`, and the seams
`notification-permission` and `compose-webview`.

**Phase 3: features with no or trivial platform code.** `feature-payments`, `feature-insurances`,
`feature-forever`, `feature-onboarding`, `feature-payin-account`, `feature-insurance-certificate`,
`feature-home`, `feature-profile`, `feature-terminate-insurance`, `feature-travel-certificate`,
`feature-image-viewer`. Each PR removes that feature's desktop placeholder.

**Phase 4: features with real platform code.** `feature-login` (finish what phase 0 proved, with
the video fallback), `feature-claim-details` (file pickers), `feature-chat` (Room, paging, media,
attachments), `feature-connect-payment-trustly` (web view). The skip rule is most likely to apply
here. Trustly is the leading candidate.

**Phase 5: plain JVM libraries.** Convert the non-test `hedvig.jvm.library` modules that
`commonMain` code depends on to `hedvig.multiplatform.library`. No behaviour change. This phase can
be cut without affecting goals 1 to 4; it exists to make the iOS step a plugin swap.

**Done** means: goals 1 to 4 hold, the skip register is current, and a walkthrough of every tab
and every flow reachable from them has been done on the desktop app against staging, with each
flow either working or listed in the register.

## Verification

Per conversion PR:

- Android: `./gradlew :app:assembleDebug` and the module's tests. Library modules are release-only
  under AGP 9, so there is no `compileDebugKotlin` outside `:app`; KMP modules verify with
  `compileAndroidMain` and `jvmTest`.
- Desktop: `:app-desktop` compiles, the app is started, and the converted feature is exercised by
  hand against staging.
- For any PR that touches `androidMain` behaviour rather than only moving files, a check on a
  device or emulator of the affected screen.

## CI

- `pr.yml` gains a step that compiles `:app-desktop`.
- A check fails the build when a file under any `src/commonMain` imports `java.*`, `javax.*` or
  `android.*`. Existing violations, if any, are fixed or allow-listed with a reason when the check
  is introduced. The mechanism (Gradle task, lint rule or script) is the builder's choice.

## Open risks

- **Navigation 3 UI on JVM.** `navigation3-ui` is only depended on from `androidMain` today.
  Phase 0 exists largely to settle this.
- **Feature flags.** There are 9 flags in `Feature.kt`, and their live values exist only in
  Unleash. Preferred: a small `jvmMain` provider that fetches them from the same Unleash frontend
  endpoint the Android SDK uses, with no change to `commonMain`. If that turns out to be more
  than a small file, fall back to a static provider whose values Stylianos supplies, kept in one
  file. The existing unbound `NoopFeatureFlagProvider` (everything `false`) is not acceptable as
  the desktop binding, since it would hide released features.
- **Backend identity.** The backend may vary responses by `X-Platform`, `User-Agent` or
  `Hedvig-App-Version`. Desktop sends the Android values so it gets the Android experience, which
  also means desktop traffic is indistinguishable from Android in backend logs. Staging is the
  default for that reason. If phase 0 shows the backend needs to tell them apart, raise it instead
  of inventing a new platform identifier.
- **Logging in to verify.** Every desktop check needs an authenticated staging session, and
  getting one needs a person with a staging BankID to scan the QR code. Tokens persist on disk,
  so one scan lasts until the refresh token expires (about 23 hours).
- **Paging and Room in chat.** `paging-compose` and Room's paging integration need to work from
  `commonMain` on JVM. If not, chat's message list is the place the skip rule gets tested.
- **Build time.** Android-only modules that become KMP gain a JVM compilation. CI already runs
  `test jvmTest`; watch the PR workflow duration across phase 3 and raise it if it grows by more
  than a couple of minutes.

## Left to the builder

- Module-internal structure of `:app-shell` and `:app-desktop`, and the exact file split with
  `:app`.
- The shape of each seam (expect/actual versus injected interface), following existing precedent
  in the module being touched.
- Order of modules within a phase.
- Where the desktop app stores its files, and the Gradle property name for the backend switch.
- Wording and look of the "Not available on desktop" state, built from existing design system
  components.

## Decisions and who made them

Stylianos:

- The desktop app is the real app with QR login and full functionality, not a demo.
- The skip rule.
- iOS work after this must be additions to the existing shell.

Claude's calls, open to override:

- The commonMain rule and its CI check.
- A shared `:app-shell` module rather than duplicating the shell in the desktop host.
- A throwaway phase 0 spike with a stop condition.
- Shell keys move to `-navigation` modules, and the entry provider takes a host-supplied block.
- Code leaving `androidMain` of an iOS-compiled module goes to `commonMain` and keeps compiling
  for iOS, with the one registered exception for navigation UI if its iOS artifacts do not exist.
- Feature flags fetched from Unleash on desktop, static as the fallback.
- Staging by default, Android identity headers through a host override.
- Navigation serialization tests stay in `:app`.
- Existing tests move to `jvmTest` unchanged.
- Phase 5 is in scope but cuttable.
- No iOS targets are added in this project.

## Skip register

Nothing skipped yet. Expected candidates: Trustly web view, camera capture, audio recording, share
sheet, in-app review, push notifications.

## iOS debt register

Code that is shared between Android and desktop but cannot compile for iOS yet. Empty. Expected
candidate: navigation UI infrastructure, pending the phase 0 artifact check.
