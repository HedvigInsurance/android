# The Hedvig app on desktop: design

Date: 2026-11-12. Measured against `develop` at `229bd79df0`.

Everything up to "Decisions and who made them" is the design as planned. "As built" at the end describes what
exists on `feat/desktop-app-integration` at `f674f564ce` and wins where the two differ. **"As built" is a draft:**
Android has not been checked on a device and the desktop app has not been walked through end to end.

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

## As built

Draft. State of `feat/desktop-app-integration` at `f674f564ce`. Sections marked "to be updated" change after the
Android device run and the desktop walkthrough.

### What exists

#### Modules

| Module | Plugins | Holds |
|---|---|---|
| `:app-shell` (`app/app-shell`) | `hedvig.multiplatform.library` + `.android` (JVM, Android) | `HedvigApp`, `HedvigAppState`, `NavigationSuite`, `BackstackController`, `TopLevelRunLogic`, `SessionReconciler`, `HedvigEntryProvider` (`hedvigEntryProvider`, registers every key for every host), `AppHost`, `DeepLinkFirstUriHandler`, `AuthorizationCodeUriHandler`, `GlobalHedvigSnackBar`, and the Apollo chain shared by both hosts (`ApolloInterceptorMetroProviders`, `LoggingInterceptor`, `LogoutOnUnauthenticatedInterceptor`). The shell's tests are in its `jvmTest`. |
| `:app` | `hedvig.android.application` | The Android host: `MainActivity`, `HedvigApplication`, `AppGraph`, `ActivityRetainedGraph`, `NavRetainedViewModel`, `MergedMetroViewModelFactory`, `NavigationStateBridge`, `AndroidAppHost`, external deep links, notifications, WorkManager, Firebase and Datadog (`Navigation3TrackingEffect` is composed in `MainActivity` right before `HedvigApp`). `ExhaustiveBackStackSerializationTest` and `BackStackSerializationTest` stay here. |
| `:app-desktop` (`app/app-desktop`) | `hedvig.multiplatform.library` (JVM only), Compose Desktop | `Main.kt` (window, Escape as back), `DesktopGraph`, `DesktopMetroProviders`, `DesktopBindings`, `DesktopAppHost`, `DesktopExternalNavigator`, `DesktopAppBuildConfig`, `DesktopFallbackEntry`, `DesktopLogging`, `StdoutLogcatLogger`. |
| New `-navigation` modules | `.ios` + `.android` | `feature-home-navigation` (`HomeKey`), `feature-login-navigation` (`LoginKey`), `feature-insurances-navigation` (`InsurancesKey`), `feature-profile-navigation` (`ProfileKey`), `feature-onboarding-navigation` (`OnboardingKey`, `OnboardingGate`, `ResetOnboardingSeenUseCase`, `KeepKmp`), `feature-chat-navigation` (`ChatKey`, `InboxKey`), `feature-claim-details-navigation` (`ClaimDetailsKey`), `feature-image-viewer-navigation` (`ImageViewerKey`). |
| `database-jvm` (`app/database/database-jvm`) | `hedvig.jvm.library` | `DatabaseJvmMetroProviders`: Room `AppDatabase` builder with `BundledSQLiteDriver`. |

Every feature module except `feature-impersonation` is KMP with its screens, presenters, ViewModels, entries and deep
links in `commonMain`. No entry is registered by a host: `hedvigEntryProvider` registers all 111 nav keys on both.
Android passes no fallback entry, so an unregistered key still throws there. Desktop passes `desktopFallbackEntry`,
which shows "Not available on desktop" and logs the key once at WARN; `DesktopEntryProviderTest` asserts no key
needs it.

Still Android-only (`hedvig.android.library`): `core-rive`, `database-android`, `datadog-android`,
`accessibility-test`, `experiments-unleash`, `feature-impersonation`, `language-migration`, `logging-device-model`,
`notification-core`, `notification-firebase`, `tracking-datadog`, `tracking-firebase`, `compose-webview`.

#### Running it

```bash
./gradlew :app-desktop:run                                   # staging
./gradlew :app-desktop:run -Phedvig.desktop.production=true  # production
```

Only the `run` task gets the JVM arguments (backend, and `versionName`/`versionCode` read from
`app/app/build.gradle.kts`; the task fails if either cannot be read). Other Compose Desktop tasks such as
`runDistributable` start without them and fail at startup. Only macOS has been used.

The app identifies itself as the Android app: `X-Platform: ANDROID`, `Hedvig-App-Version: android;<versionName>`,
the develop application id and debug build type on staging, release values on production, and brand "Hedvig", model
"Desktop" in the `User-Agent` and device fields. The first log lines print the identity and the GraphQL URL.

#### Login

The desktop app always starts with `LoginKey` on a fresh in-memory back stack; `SessionReconciler` moves a member
with stored tokens straight to Home. BankID login shows only the QR code (scan it with BankID on a phone). Email
login with an OTP, the BankID sandbox toggle on staging and demo mode (long press on the QR) work as on Android.
Tokens persist on disk, so one scan lasts until the refresh token expires. An `UNAUTHENTICATED` GraphQL response
logs the member out, through the same interceptor as Android.

#### Where its data lives

`~/.hedvig-desktop/staging/` or `~/.hedvig-desktop/production/` (provided to the graph as `@DesktopDataDirectory`):

| Path | Contents |
|---|---|
| `datastore/hedvig_data_store_preferences.preferences_pb` | Preferences DataStore: auth tokens, settings, the picked language |
| `hedvig_chat_database.db` | Room database of the chat |
| `coil_image_cache/` | Coil disk cache |

Downloaded PDFs go to the system temp directory. Feature flags and the back stack are memory only. Two instances
started at once share the token store and the database file without coordinating.

### Seam register

Seams added for this work. "Android" is the code that ran before, moved behind the seam.

#### `expect`/`actual`

| Module | Seam | Android actual | JVM actual |
|---|---|---|---|
| `core-build-constants` | `AppBuildConfig.platformName` (defaults to `internal expect val defaultPlatformName`) | `"ANDROID"` | `"jvm"`; `DesktopAppBuildConfig` overrides it with `"ANDROID"`. A Swift implementer must supply it, it does not see the Kotlin default |
| `core-common-public` | `isValidEmail` | `PatternsCompat.EMAIL_ADDRESS` | `Regex` of the same pattern string (native too) |
| `core-file-upload` | `rememberOpenPdf(applicationId)` | `ACTION_VIEW` chooser through the app's `FileProvider` | `Desktop.open`; on failure a WARN and a dialog with the saved path |
| `molecule-public` | `mainDispatcher` (now public) | `AndroidUiDispatcher.Main` | `Dispatchers.Main` (Swing) plus a frame clock that completes each frame 16 ms after it is requested |
| `notification-permission` | `rememberNotificationPermissionState`, `rememberNotificationPermissionStatus` | Accompanist permission state | Always granted, never prompts |
| `forever-ui` | `rememberShareHandler` | Share sheet | Copies to the clipboard |
| `forever-ui` | `rememberCopyToClipboard` | Clipboard | AWT clipboard |
| `compose-result-launcher` | `rememberPickMultipleVisualMediaResultLauncher`, `rememberGetMultipleContentsResultLauncher` (existing; JVM was `TODO()`) | Activity result pickers | AWT `FileDialog`, results as `file:` URIs |
| `compose-photo-capture-state` | `rememberPhotoCaptureState` (existing) and new `PhotoCaptureState.isAvailable` | Camera, available | Unavailable; launching logs a WARN |
| `audio-player-ui` | `CommonMediaPlayer` (existing; JVM was `TODO()`) | `MediaPlayer` | `UnavailableMediaPlayer`: reports an error on prepare |
| `feature-chat` | `chatViewModelDispatcher` | `AndroidUiDispatcher.Main` | `mainDispatcher` |
| `feature-chat` | `ChatContext`, `isAccessibilityEnabled` | `Context`, reads enabled accessibility services | Always `false` |
| `feature-chat` | `webUrlLinkMatcher` | `Patterns.WEB_URL` | `https?://\S+`, case-insensitive |
| `feature-chat` | `ChatContentResolver`, `takePersistableReadPermission`, `releasePersistableReadPermission` | `ContentResolver` persistable URI grants | No-op (a picked file stays readable) |
| `feature-chat` | `RoomDatabase.chatTransaction` | `withTransaction` | `useWriterConnection { immediateTransaction }` |
| `feature-chat` | `rememberChatVisualMediaPicker`, `rememberChatFilePicker` | Activity result pickers (remembered) | The `compose-result-launcher` JVM pickers |
| `feature-chat` | `ChatVideoMessage`, `ChatVideoCache`, `ChatSimpleVideoCache`, `ChatVideoPlayers`, `rememberChatVideoPlayers`, `rememberPreviewChatVideoCache` | ExoPlayer inline video with a media3 cache | The attached-file card, opening the URL |
| `feature-chat` | `WindowInsets.chatImeAnimationTarget` | `imeAnimationTarget` | `WindowInsets.ime` (zero) |
| `feature-chat` | `talkBackDateTime` | `formatInstantForTalkBack` (`DateUtils`) | `HedvigDateTimeFormatterDefaults.yearMonthDateAndTime` |
| `feature-claim-details` | `rememberCopyToClipboard(text, confirmation)` | Clipboard and toast | Clipboard, no confirmation |
| `feature-connect-payment-trustly` | `TrustlyCheckoutWebView` | Trustly web view with its JS callbacks | `NotAvailableOnDesktop` |
| `feature-image-viewer` | `Bitmap.onWhiteBackground` | Android `Bitmap` composite | Skia composite (opaque images returned as is) |
| `feature-login` | `rememberBankIdState(bankIdUri: com.eygraber.uri.Uri)` | `BankIdStateImpl`, opens the BankID app | QR only, `canOpenBankId = false` |
| `feature-login` | `LoginBackgroundVideo` | Video over the still | The still (a second copy in `jvmMain/composeResources`) |
| `feature-login` | `CommonLocale.languageTag` | `toLanguageTag()` (one `jvmAndAndroidMain` actual) | same |
| `feature-onboarding-navigation` | `KeepKmp` | Typealias to `androidx.annotation.Keep` (`jvmAndAndroidMain`) | same; native actual exists |
| `feature-onboarding` | `KeypadGlyphPlatformTextStyle` | `PlatformTextStyle(includeFontPadding = false)` | `null` |
| `feature-payin-account` | `rememberSwishAppHandover` | Detects the Swish app and hands over | Not installed: QR only; an http approval URL opens in the browser |
| `feature-profile` | `rememberOpenBugReportEmail` | Email intent | `Desktop.mail` with a `mailto:`; logs when no mail client |
| `feature-profile` | `OpenSourceLicenses` | Web view of the bundled HTML | `NotAvailableOnDesktop` |
| `feature-profile` | `NotificationSettingsRow` | Row opening system settings | Not shown |
| `feature-profile` | `Theme.apply` | `AppCompatDelegate` night mode | No-op; the theme is read through `HedvigAppState.darkTheme` |

#### Per-platform Metro bindings

Every binding below sits in `jvmMain` (or `database-jvm`, or `:app-desktop`), next to an Android binding in
`androidMain` or `:app`. None is a `commonMain` default.

| Module | Type bound | Android | JVM |
|---|---|---|---|
| `core-common-public` | `@DesktopDataDirectory File` (qualifier, `jvmMain`) | n/a | Provided by `DesktopGraph.Factory` |
| `core-datastore-public` | `DataStore<Preferences>` | `filesDir` | Same file name under the data directory (`JvmDataStoreMetroProviders`) |
| `core-datastore-public` | `DeviceIdFetcher` | Android binding | `JvmDeviceIdFetcher`, same body |
| `core-file-upload` | `FileService` | `AndroidFileService` (`ContentResolver`) | `JvmFileService` (`file:` URIs) |
| `core-file-upload` | `DownloadPdfUseCase` | `AndroidDownloadPdfUseCaseImpl` (cache dir) | `JvmDownloadPdfUseCaseImpl` (temp file, non-2xx is an error) |
| `feature-flags` | `FeatureManager` via the Unleash client | `UnleashFeatureFlagProvider` over the Unleash Android SDK | `JvmUnleashFeatureFlagProvider` over `JvmUnleashClient`: Ktor GET of the frontend API every 30 s and on member change, memory only, no variants |
| `language-core` | `LanguageService` | `AndroidLanguageService` (AppCompat locales) | `JvmLanguageService`: OS language until a pick, the pick kept in the DataStore |
| `network-clients` | `AccessTokenFetcher`, `ExtraKtorClientConfiguration`, `@AuthHttpClientEngine HttpClientEngine` | Datadog-instrumented OkHttp engine, Chucker | `JvmNetworkMetroProviders`: plain engine, no-op configuration |
| `network-clients` | `TlsDiagnostics` (interface now in `commonMain`) | Android binding | `JvmTlsDiagnostics`, returns `null` |
| `database-jvm` | `RoomDatabase.Builder<AppDatabase>` | `database-android` | Bundled SQLite at `hedvig_chat_database.db` |
| `feature-chat` | `ChatContext`, `ChatContentResolver` | Typealiases to `Context`, `ContentResolver` | `DesktopChatContext`, `DesktopChatContentResolver` |
| `feature-claim-chat` | `AudioRecordingManager` | `MediaRecorder` | `JvmAudioRecordingManager`, reports the recording error state |
| `:app-shell` (`commonMain`) | `ExtraApolloClientConfiguration` | One provider for both hosts | same |
| `:app-desktop` | `MetroViewModelFactory` | `MergedMetroViewModelFactory` | `DesktopViewModelFactory` (one graph holds both scopes) |
| `:app-desktop` | `EventTrackingClient` | Firebase | `LoggingEventTrackingClient`, DEBUG log only |
| `:app-desktop` | `ExperimentProvider` | `UnleashExperimentProvider` | `NoActiveExperimentsProvider`, empty |
| `:app-desktop` | `kotlin.time.Clock`, `TimeZone`, `HedvigDeepLinkMatcher`, `ImageLoader` | `ApplicationMetroProviders` | `DesktopMetroProviders` (Coil with Ktor and SVG only) |
| `:app-desktop` | `Backstack`, `AppBuildConfig` | `ActivityRetainedGraph.Factory`, `AppGraph` | `DesktopGraph.Factory` inputs |
| `:app-desktop` | `RumLogger` (installed, not bound) | Datadog | `LogcatRumLogger` |
| `:app-desktop` | `AppHost`, `ExternalNavigator` (passed to `HedvigApp`) | `AndroidAppHost`, `ExternalNavigatorImpl` | `DesktopAppHost`, `DesktopExternalNavigator` |

`DesktopGraph` is `@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])`. Its
`serializersModules` set is empty: nav key serializers are generated for the Android target only and the desktop
back stack is never persisted.

### Skip register

Skip rule: skip a piece of functionality on desktop when supporting it would complicate `commonMain` and make the
Android code worse. Every row is checked against the code at `f674f564ce`.

| What a desktop user sees | Why | What it would take |
|---|---|---|
| Trustly checkout (payin, payout, onboarding "connect payment") shows "Not available on desktop" under a working Up arrow. Entering the step still starts a Trustly session on the backend. Swish works by QR | The checkout relies on Android `WebView` JS callbacks; `compose-webview` is Android-typed | A desktop web view (JCEF/KCEF) behind `TrustlyCheckoutWebView`, or open the URL in the browser and poll the connection status |
| Profile, license attributions: "Not available on desktop" | The list is an HTML asset shown in a web view | Render the bundled list as text, or open a hosted page |
| Chat video messages are a file card that opens the URL in the browser | ExoPlayer is Android-only | A JVM video player (vlcj, JavaFX) behind `ChatVideoMessage` |
| Chat camera button is visible and does nothing (WARN in the log). Claim details and claim chat hide the camera option | No camera capture on JVM | Read `PhotoCaptureState.isAvailable` in `ChatInput`, or webcam capture |
| Audio recordings (claim details, claim chat, chat) show the player's error card; its retry cannot succeed | `javax.sound` cannot decode AAC/m4a | JavaFX Media or vlcj behind `CommonMediaPlayer` |
| Claim chat voice answer: the recorder shows its error state after the countdown; Send stays enabled and does nothing. Typing the answer works | No recorder on JVM | A `TargetDataLine` recorder, after checking which formats the backend accepts |
| Claim chat "helipad" loader is blank | Rive has no JVM runtime | A Compose fallback or the still frame in `HelipadRiveAnimation.jvm.kt` |
| Login background is a still image | No ExoPlayer | Cosmetic, none planned |
| BankID: QR only, no "Open BankID" button | No BankID app hand-over on desktop | `Desktop.browse(bankIdUri)` when a `bankid:` handler exists |
| No push notifications, no notification prompts or reminder cards, no Settings notifications row, no "turn on notifications" after filing a claim | No push transport; the permission reads as granted | A push transport; out of scope |
| Links from email or the browser never open the app | No OS URL handler; in-app links work | Packaging plus `Desktop.setOpenURIHandler` |
| App store review prompt never shows | No store | None |
| App settings and Play Store actions show a notice dialog. The force-upgrade screen stays blocking; its button says to update the checkout | Desktop reports the checkout's `versionName` | None; update the checkout |
| Calling a phone number goes to the OS `tel:` handler (FaceTime on macOS), else a notice with the number | No dialer | None |
| OTP "Open email app" opens a new message, not the inbox. "Submit a bug" only logs when no mail client is configured | `java.awt.Desktop` can only compose mail | None |
| Sharing a referral link copies it, with no confirmation. Copying a claim id or handler email gives no confirmation | No share sheet or toast on desktop | A snackbar through `GlobalSnackBarState` |
| No experiment variants: the member is in no experiment, so the backend serves defaults | `ExperimentProvider` lives in Android-only `experiments-unleash`; `JvmUnleashClient` keeps no variants | Keep variants in `JvmUnleashClient`, convert `experiments-unleash` |
| Feature flags read never-fetched defaults on every start until the first fetch, so gated UI can change a moment after launch | `JvmUnleashClient` keeps state in memory only | Persist the toggle state under the data directory |
| No analytics, Datadog RUM, Crashlytics or log file. Everything goes to stdout | No desktop SDKs | A rotating log file under the data directory |
| The backend gets no device row for a desktop login | `DeviceNameLoggingAuthListener` is in Android-only `logging-device-model` | A JVM auth listener |
| No staff impersonation | `feature-impersonation` is an Android Activity | Not planned |
| The back stack is not restored after quitting | Desktop has no process death; the stack is in memory | Not planned |
| Chat: Enter inserts a newline (send with the button); links without a scheme are not detected as GIFs; the cross-sell card and banner never hide for a keyboard; small file reads block the UI thread | Seams with trivial JVM actuals | Per item, small |
| Home and Insurances: pull to refresh cannot be triggered with a mouse | Touch gesture | A refresh action or scroll-wheel trigger |
| Image viewer zooms by double click and drag only, no pinch or wheel | Zoomable's desktop input | Wheel zoom support |
| Animated GIFs do not animate | The desktop `ImageLoader` has no GIF decoder | A JVM animated image decoder |
| The onboarding Forever screen has no Up arrow; leave it with Escape | The screen has none on Android either, the system back is used | None |
| Escape inside a focused text field acts as back; Escape at the root closes the app | Escape is the desktop system back | Decide after the walkthrough |
| `AnimatedBorder`'s gradient starts at 0 degrees | `AngledSweepGradient` JVM actual ignores `startAngle` | A rotated Skia sweep shader |

### iOS debt register

What stands between this state and an iOS target. Plain JVM library work is in progress on unmerged, stacked
branches `refactor/kmp-libs-auth`, `-core`, `-leaves`, `-data`; their rows say so.

#### Plain JVM modules reached from `commonMain`

| Module | Reached from `commonMain` of | Action | Status |
|---|---|---|---|
| `auth-event-core` | (via `auth-core-public`, `core-demo-mode`, `core-app-review`, `language-data`) | `.ios` | In progress (`kmp-libs-auth`), iOS compiles |
| `auth-core-public` | app-shell, delete-account, login, onboarding, payments, profile | Move `java.util.UUID` to `uuid4()`; move the unused `test-clock` main dependency to tests; then `.ios` | In progress: KMP JVM-only until `test-clock` leaves main |
| `core-demo-mode` | app-shell and 13 features | `.ios` | In progress (`kmp-libs-core`), iOS compiles |
| `core-app-review` | app-shell, edit-coinsured | `.ios` | In progress (`kmp-libs-core`), iOS compiles |
| `theme`, `experiments-public`, `navigation-activity`, `notification-badge-data-public` | app-shell, features | `.ios` | In progress (`kmp-libs-leaves`), iOS compiles |
| `language-data` | payments | `.ios` after `auth-core-public` | In progress: KMP JVM-only |
| `data-cross-sell-after-claim-closed`, `data-cross-sell-after-flow`, `data-display-items`, `data-paying-member`, `data-settings-datastore-public`, `member-reminders-public` (9 `java.util.UUID` calls) | features, app-shell | `.ios` | In progress (`kmp-libs-data`), not finished |
| `data-addons` | app-shell, cross-sells, 5 features | Replace `androidx.annotation.Keep` on `AddonBannerSource` | Planned JVM-only in `kmp-libs-data`; iOS is a follow-up |
| `data-changetier` | app-shell, choose-tier, terminate-insurance | Move `CostBreakdownEntry` out of `ui-tiers-and-addons`; drop unused `data-chat` dependency | Planned JVM-only; iOS is a follow-up |
| `data-chat` | feature-chat | Convert with Room KSP for KMP (`RoomHandler` hard-codes `ksp`/`kspTest`) | Not started |

`initializable` and `apollo-network-cache-manager` (KMP, JVM-only) get `.ios` in the same branches.

#### KMP modules without an iOS target

`app-shell`, `audio-player-data`, `audio-player-ui`, `claim-status`, `compose-pager-indicator`,
`compose-photo-capture-state`, `compose-result-launcher`, `core-file-upload`, `core-icons`, `core-markdown`,
`cross-sells`, `data-claim-intent`, `data-product-variant-public`, `file-upload-ui`, `forever-ui`,
`member-reminders-ui`, `notification-permission`, `pullrefresh`, `tier-comparison`, `ui-analytics-consent`,
`ui-claim-flow`, `ui-force-upgrade`, `ui-phone-number`, `ui-tiers-and-addons`, and every `feature-*` module except
the `-navigation` ones. Adding `.ios` to each needs every `expect` below to get a `nativeMain` actual. For
`core-icons`, check that `material-icons-core` 1.7.3 iOS klibs link against Compose Multiplatform 1.12.2.

#### Seams with no native actual

Every `expect` in the seam register in a module without iOS targets: all of `feature-chat`, `feature-login`
(`rememberBankIdState`, `LoginBackgroundVideo`, `CommonLocale.languageTag`), `feature-profile` (4),
`feature-payin-account`, `feature-onboarding`, `feature-claim-details`, `feature-image-viewer`,
`feature-connect-payment-trustly`, `forever-ui` (2), `notification-permission` (2), `core-file-upload`
(`rememberOpenPdf`), `compose-result-launcher`, `compose-photo-capture-state`, `audio-player-ui`, and the existing
`feature-claim-chat` seams. `molecule-public` (`mainDispatcher`), `core-common-public` (`isValidEmail`),
`core-build-constants` and `KeepKmp` already have native actuals.

| Also | Action |
|---|---|
| `HedvigQrCode` is in `design-system-hedvig` `jvmAndAndroidMain` and used from `commonMain` of `feature-payin-account` and `feature-login` | Move to `commonMain` with a native QR encoder |
| `CommonLocale.languageTag` has one `jvmAndAndroidMain` actual | Add a native actual over `NSLocale` |
| `login_still_9x16.webp` exists in Android `res` and in `jvmMain/composeResources` | One shared Compose resource |
| Room's `AppDatabase` (`database-core`, plain JVM) has no `@ConstructedBy`; chat needs a native driver and file location | Room KMP setup for iOS, plus a `chatTransaction` native actual |
| DataStore: the JVM provider takes `@DesktopDataDirectory`, declared in `core-common-public` `jvmMain` | iOS already has its own provider in `core-datastore-public` `nativeMain`; nothing to share |
| `DeviceIdFetcher`: Android and JVM bodies are identical and iOS gets it from Swift | Optional: one `commonMain` implementation |
| `AppBuildConfig.platformName`: Kotlin/Native exports interface members as required, so a Swift conformer must implement it | Note for any Swift `AppBuildConfig` (none exists on ugglan master) |

#### JDK and JVM-only calls the import guard cannot see

| Where | Call | Action |
|---|---|---|
| `feature-chat` `CbmChatViewModel.kt`, `model/CbmChatMessage.kt` | `Uuid.fromString`, `Uuid.randomUUID()`: `java.util.UUID` statics through benasher's `Uuid` typealias | `uuidFrom()` / `uuid4()` |
| `feature-home` `GetHomeDataUseCase.kt:286` | `Dispatchers.IO` without the `kotlinx.coroutines.IO` import needed on native | Add the import |
| Any `commonMain` | Members of `CommonLocale` (`java.util.Locale` on JVM) and other typealiased JDK types | Found only by an iOS compile; review by hand |

#### Annotations and libraries

| Item | Action |
|---|---|
| `@androidx.annotation.Keep` in `commonMain` of `feature-onboarding` (`OnboardingKeys.kt`), `feature-chip-id`, `feature-choose-tier`, `feature-addon-purchase`, and in `data-addons` | `KeepKmp` (as in `feature-onboarding-navigation` and `data-coinsured`) |
| `:navigation-compose` (iOS) now pulls Compose UI and the design system through the JetBrains `navigation3-ui` artifacts | Keep it out of `:umbrella` unless that is intended |
| `$DefaultImpls` classes: KMP JVM targets compile with the default `-jvm-default` mode, unlike `hedvig.jvm.library` | Convention change if byte-identical jars matter (affects every KMP module) |

### Deviations from the plan

| Plan said | Built instead |
|---|---|
| Navigation UI may need `jvmAndAndroidMain` and an iOS debt entry | JetBrains `navigation3-ui` publishes desktop and iOS; the six files moved to `commonMain` of `:navigation-compose` and compile for iOS |
| Converted ViewModels run on desktop | They did not: Molecule needs a frame clock on the JVM; `molecule-public` got one |
| Hosts pass nav keys to the entry decorators | They pass Nav3 content keys |
| `compose-webview` becomes KMP | Stays Android; each caller (licenses, Trustly) has its own seam |
| JVM bindings for existing seams: a short list | 7 missing in the libraries, then 12 more found by compiling a desktop graph |
| `platformName` overridden by the host's build constants | `AppBuildConfig.platformName`, defaulting to `defaultPlatformName` per target |
| `AppHost` interface sketched in the plan | Real shape: `uriHandler`, `shouldShowPermissionRationale`, `tryShowAppStoreReviewDialog`, `SystemBarsEffect`; Datadog's effect stays in `MainActivity` |
| Shell keys: tab roots, `LoginKey`, onboarding | Also `ChatKey`, `InboxKey`, `ClaimDetailsKey`, `ImageViewerKey` |
| A host-entries block that ends holding Android-only entries | Built, then deleted once every feature was shared; replaced by the optional `fallbackEntry` |
| Desktop notification permission reads "not granted" | Reads "granted", so no prompt or reminder appears |
| Share sheet "Not available" | Copies to the clipboard |
| Email validation and PDF sharing as plain moves | New seams `isValidEmail` and `rememberOpenPdf` |
| Tests move to `jvmTest` | Robolectric tests go to `androidHostTest` (feature-flags precedent) |
| A final "entries sweep" | Nothing was left; replaced by an audit |
| Phase 5: `hedvig.multiplatform.library` on 20 modules | JVM-only alone does nothing for iOS, so `.ios` per module; 17 modules; `graphql` dirs move too; `java.util.UUID` must change; no `.android` plugin |
| (not planned) | `LogoutOnUnauthenticatedInterceptor` and `LoggingInterceptor` moved to `:app-shell` so desktop logs out on an expired session; a build rule forbids `feature-*` depending on `:app-shell` |
| Guard appended to the ktlint step | Its own `pr.yml` step, since the ktlint step is `continue-on-error` |

### Verification status

To be updated after the device run.

| Check | Status |
|---|---|
| Per conversion: module tests before and after (same count), `checkCommonMainImports`, `:app:assembleDebug` | Done on every branch |
| Per conversion: `:app` compiled graph compared before and after (87 ViewModel entries, 21 deep link matcher providers, 111 nav key serializers) | Done on every branch, identical |
| JVM graph tests: `DesktopGraphTest` builds every ViewModel and factory; `DesktopEntryProviderTest` registers every serializable key once, without a fallback; per-module `Jvm*BindingsTest` | Pass |
| Integration tip `f674f564ce`: repo-wide `test jvmTest`, `:app:test`, `checkCommonMainImports`, `:app:assembleDebug` | Pass |
| PR CI on the integration branch | Not run: no PR is open |
| Seen by a person on desktop (staging) | BankID QR login, Home, onboarding. Nothing else |
| Walkthrough of every tab and flow on desktop | Not done |
| Release or staging (R8) build | Not done |
| Any run on an Android device | Not done |

Android device checks owed:

1. Process-death restore on a minified staging build with `ChatKey`, `ClaimDetailsKey` and `ImageViewerKey` on the
   stack and a parked side tab.
2. A deep link while logged out, then login.
3. Datadog RUM `@view.name` versus develop: claim chat to claim details from Home, the termination flow, delete
   account and claim history from Profile.
4. Light and dark system bars after the splash.
5. The notification permission prompt (the Android actual now wraps Accompanist).
6. A cross-sell link with `requiresAuthorization = true` (authorization code).
7. Chat: inbox timestamps, paging back through history, sending text, a camera photo, a gallery image and a
   document, a failed upload in airplane mode then kill and retry, inline video and backgrounding, TalkBack
   descriptions, a GIF URL, the keyboard hiding the banner and cross-sell card, rotating with a picker open.
8. Navigation chrome transitions, bottom sheets and parked tabs (now from the JetBrains artifacts).
9. After the plain JVM library conversion: login, Home (reminders, badges, addon banner), Insurances, Payments,
   theme switch, change tier, logout and login.

Desktop checks owed: Escape as back, the notice dialogs, the file dialog, image viewer zoom, Coil thumbnails of
`file:` paths with spaces, whether one Enter press on the email screen submits twice, and the full walkthrough.

### For other developers

#### Moved files

| What | From | To |
|---|---|---|
| Feature sources | `src/main/kotlin`, `src/main/graphql`, `src/test/kotlin` | `src/commonMain/kotlin`, `src/commonMain/graphql`, `src/jvmTest/kotlin` (platform files to `androidMain` with `.android.kt` names). Modules: core-icons, forever-ui, member-reminders-ui, notification-permission and the features chat, claim-details, connect-payment-trustly, forever, home, image-viewer, insurance-certificate, insurances, login, onboarding, payin-account, payments, profile, terminate-insurance, travel-certificate |
| Robolectric tests | `src/test` of feature-login, feature-payin-account | `src/androidHostTest` |
| Already-KMP entries | `androidMain` of claim-chat, claim-history, remove-addons | `commonMain` |
| Shell | `app/app/src/main/.../app/` (`BackstackController`, `TopLevelRunLogic`, `SessionReconciler`, `HedvigEntryProvider`, `HedvigContentKeys`, `ScreenName`, `ScreenParameterExtractor`, `CurrentDestinationHolder`, `HedvigApp`, `HedvigAppState`, `NavigationSuite`, `TopLevelTabStartDestination`, `GlobalHedvigSnackBar`, `DeepLinkFirstUriHandler`, `AuthorizationCodeUriHandler`, `GetMemberAuthorizationCodeUseCase`, `LoggingInterceptor`) and their tests | `app/app-shell/src/commonMain`, `src/jvmTest`, same packages |
| Nav UI | `:navigation-compose` `androidMain` (`Decorators`, `BottomSheetSceneStrategy`, `NavSuiteSceneDecorator`, `NavigationSuiteType`, the two retained decorators) | `commonMain`, same package |
| Keys | `HomeKey`, `LoginKey`, `InsurancesKey`, `ProfileKey`, `OnboardingKey` (+ `OnboardingGate`, `ResetOnboardingSeenUseCase` interfaces), `ChatKey`, `InboxKey`, `ClaimDetailsKey`, `ImageViewerKey` | The new `-navigation` modules, same packages |
| Unleash keys | `Feature.unleashKey`, `neverFetchedDefaults` in `feature-flags` `androidMain` | `commonMain` (`FeatureUnleashKey.kt`, `UnleashSettings.kt`) |
| `Language.label` | `language-core` `androidMain/Language.kt` | `commonMain/LanguageLabel.kt` |
| `TlsDiagnostics` | `network-clients` `androidMain` | `commonMain` |

#### Changed symbols

- `AndroidAppHost` implements the shell's `AppHost`. `HedvigApp` takes an `appHost` and an optional `fallbackEntry`.
- `validateEmail(x).isSuccessful` callers use `isValidEmail(x)`.
- `platformName` is `AppBuildConfig.platformName`; the old `expect val platformName` is `defaultPlatformName`.
- `NoopFeatureFlagProvider` is deleted. `mainDispatcher` in `molecule-public` is public.
- `HostEntryContext`, `AndroidHostEntries.kt` and the host entries block no longer exist.

#### Open branches that collide

| Branch | Collision |
|---|---|
| PR 3305 (`feat/native-purchase-flow`) | Edits `feature-login` `build.gradle.kts`, `SwedishLoginDestination.kt`, `SwedishLoginPresenter.kt` and moves `BankIdState.kt` into an Android-only `app/ui/ui-bankid` with `rememberBankIdState(autoStartToken: String)`. Here `BankIdState` is an expect/actual seam: the interface and `internal expect fun rememberBankIdState(bankIdUri: com.eygraber.uri.Uri)` in `commonMain`, `BankIdStateImpl` in `androidMain/.../BankIdState.android.kt`, a JVM actual. `ui-bankid` would have to become KMP with the same seam, or `feature-login` keeps its own. The branch also changes `insuranceEntries` (`openCrossSell: (CrossSell) -> Unit`) and makes `InsuranceContractDetailKey` public; a `commonMain` user outside the module would need it in `feature-insurances-navigation` |
| `feat/product-pages` | Four files in `feature-insurances` and seven in `feature-onboarding`, all moved by pure renames; changes the onboarding entries signature |

#### Dead code (listed, not deleted)

- `validateEmail` and `ValidationResult` (`core-common-public` `androidMain/.../validation/`).
- `SubmittedAndClosedColumns.kt` (`feature-claim-details` `androidMain`, called only from its preview).
- `rememberFilePicker` with both actuals and `FilePicker` (`feature-claim-chat`).
- Empty `<manifest />` files in `androidMain` of feature-forever, feature-home, feature-insurance-certificate,
  feature-insurances, feature-payments, feature-terminate-insurance, feature-travel-certificate.
