# The Hedvig app on desktop: as built

State of `feat/desktop-app-integration` at `0d94497df0`. The design is in `2026-11-12-desktop-app-design.md`.

- Run it with `./gradlew :app-desktop:run` (staging) on macOS; log in with BankID by QR code or with email.
- Every feature except staff impersonation runs on shared code. Trustly checkout, audio, video, camera, push and OS
  deep links do not work on desktop; the skip register lists every gap.
- On an Android device the converted app passed a smoke run, chat end to end with staff replies, the flows that
  write on entry and process-death restore, on debug and minified builds.
- For iOS, the next step is iOS targets on `auth-core-public` (no blocker left) and on the feature modules, each
  `expect` getting a `nativeMain` actual (see the iOS debt register).

## Running it

```bash
./gradlew :app-desktop:run                                   # staging
./gradlew :app-desktop:run -Phedvig.desktop.production=true  # production
```

Only the `run` task gets the JVM arguments (backend, and `versionName`/`versionCode` read from
`app/app/build.gradle.kts`; the task fails if either cannot be read). Other Compose Desktop tasks such as
`runDistributable` start without them and fail at startup. Only macOS has been used. The log goes to stdout: every priority against staging, INFO and above against production
(`-Phedvig.desktop.production=true`), since DEBUG lines carry GraphQL response data with real members' data.

The app identifies itself as the Android app: `X-Platform: ANDROID`, `Hedvig-App-Version: android;<versionName>`,
the develop application id and debug build type on staging, release values on production, and brand "Hedvig", model
"Desktop" in the `User-Agent` and device fields. The first log lines print the identity and the GraphQL URL.

**Login.** The app starts with `LoginKey` on a fresh in-memory back stack; `SessionReconciler` moves a member with
stored tokens straight to Home. BankID shows only the QR code, to scan with BankID on a phone. Email login with a
one-time code, the BankID sandbox toggle on staging and demo mode (long press on the QR code) work as on Android. One
scan lasts until the refresh token expires. An `UNAUTHENTICATED` GraphQL response logs the member out, through the
same interceptor as Android.

**Back.** Escape, Cmd+[ or Cmd+Left on macOS, Alt+Left on Windows and Linux, or the mouse back button
(`DesktopBack.kt`). On macOS a focused text field takes Cmd+Left (line start); on Windows and Linux Alt+Left goes back
even while typing. Holding a key goes back once per key repeat. Back at the root does nothing; the app quits when its
window closes or an in-content close button pops the root.

**Links.** A link no deep link matcher handles does not open the browser when it is on a Hedvig deep link host
(`DesktopUnmatchedLinkRouter`, used by `DesktopAppHost.uriHandler`). On the running flavour's hosts it lands on the
Home tab (nothing while logged out); a production host link under staging is moved to the staging host of the same
kind and matched again. Other links open in the browser.

**Data.** `~/.hedvig-desktop/staging/` or `~/.hedvig-desktop/production/`, provided to the graph as
`@DesktopDataDirectory`:

| Path | Contents |
|---|---|
| `datastore/hedvig_data_store_preferences.preferences_pb` | Preferences DataStore: auth tokens, settings, the picked language |
| `hedvig_chat_database.db` | Room database of the chat |
| `coil_image_cache/` | Coil disk cache |

Downloaded PDFs go to the system temp directory. Feature flags and the back stack are memory only. Two instances
started at once share the token store and the database file without coordinating.

**Walkthrough tool.** A headless harness that renders every registered key in the real desktop graph and writes a PNG
and the visible text per screen lives on the branch `tool/desktop-walkthrough` (tooling only, not merged; the local
branch contains `884ee23e96`, the pushed copy is older). On that branch: `./gradlew :app-desktop:desktopWalkthrough`
(demo mode), or, with the desktop app closed and a logged-in staging session, add `-Phedvig.walkthrough.mode=session
-Phedvig.walkthrough.sessionInPlace=true -Phedvig.walkthrough.sessionDataDir=$HOME/.hedvig-desktop/staging`.
Session mode skips the keys that write on entry unless `-Phedvig.walkthrough.allowWrites=true`, which also runs the
flow and chat phases that write to staging. Output: `app/app-desktop/build/walkthrough/`.

## Modules

| Module | Plugins | Holds |
|---|---|---|
| `:app-shell` (`app/app-shell`) | `hedvig.multiplatform.library` + `.android` (JVM, Android) | `HedvigApp`, `HedvigAppState`, `NavigationSuite`, `BackstackController`, `TopLevelRunLogic`, `SessionReconciler`, `HedvigEntryProvider` (`hedvigEntryProvider`, registers every key for every host), `AppHost`, `DeepLinkFirstUriHandler`, `AuthorizationCodeUriHandler`, `GlobalHedvigSnackBar`, and the Apollo chain shared by both hosts (`ApolloInterceptorMetroProviders`, `LoggingInterceptor`, `LogoutOnUnauthenticatedInterceptor`). The shell's tests are in its `jvmTest`. |
| `:app` | `hedvig.android.application` | The Android host: `MainActivity`, `HedvigApplication`, `AppGraph`, `ActivityRetainedGraph`, `NavRetainedViewModel`, `MergedMetroViewModelFactory`, `NavigationStateBridge`, `AndroidAppHost`, external deep links, notifications, WorkManager, Firebase and Datadog (`Navigation3TrackingEffect` is composed in `MainActivity` right before `HedvigApp`). `ExhaustiveBackStackSerializationTest` and `BackStackSerializationTest` stay here. |
| `:app-desktop` (`app/app-desktop`) | `hedvig.multiplatform.library` (JVM only), Compose Desktop | `Main.kt` (window), `DesktopBack` (back shortcuts and the mouse back button), `DesktopUnmatchedLinkRouter`, `DesktopGraph`, `DesktopMetroProviders`, `DesktopBindings`, `DesktopAppHost`, `DesktopExternalNavigator`, `DesktopAppBuildConfig`, `DesktopFallbackEntry`, `DesktopIntegration` (`java.awt.Desktop` actions, notice dialogs), `DesktopWindowSize`, `DesktopLogging`, `StdoutLogcatLogger`. |
| `database-jvm` (`app/database/database-jvm`) | `hedvig.jvm.library` | `DatabaseJvmMetroProviders`: Room `AppDatabase` builder with `BundledSQLiteDriver`. |

Every feature module except `feature-impersonation` is KMP with its screens, presenters, ViewModels, entries and deep
links in `commonMain`, and its keys in the feature; `:app-shell` depends on the features. No entry is registered by a host: `hedvigEntryProvider` registers all 111 nav keys on both.
Android passes no fallback entry, so an unregistered key still throws there. Desktop passes `desktopFallbackEntry`,
which shows "Not available on desktop" and logs the key once at WARN; `DesktopEntryProviderTest` asserts no key
needs it.

Android-only (`hedvig.android.library`): `core-rive`, `database-android`, `datadog-android`,
`accessibility-test`, `experiments-unleash`, `feature-impersonation`, `language-migration`, `logging-device-model`,
`notification-core`, `notification-firebase`, `tracking-datadog`, `tracking-firebase`, `compose-webview`.
## Seam register

Seams between the Android and desktop implementations. The Android column is the Android code behind the seam.
Under `app/` there are 71 `expect` declarations, 39 of them added by this work (34 before).

### `expect`/`actual`

| Module | Seam | Android actual | JVM actual |
|---|---|---|---|
| `core-build-constants` | `AppBuildConfig.platformName` (defaults to `internal expect val defaultPlatformName`) | `"ANDROID"` | `"jvm"`; `DesktopAppBuildConfig` overrides it with `"ANDROID"`. A Swift conformer must supply it, it does not see the Kotlin default |
| `core-common-public` | `isValidEmail` | `PatternsCompat.EMAIL_ADDRESS` | `Regex` of the same pattern string (native too) |
| `core-file-upload` | `rememberOpenPdf(applicationId)` | `ACTION_VIEW` chooser through the app's `FileProvider` | `Desktop.open`; on failure a WARN and a dialog with the saved path |
| `molecule-public` | `mainDispatcher` (public) | `AndroidUiDispatcher.Main` | `Dispatchers.Main` (Swing) plus a frame clock that completes each frame 16 ms after it is requested |
| `notification-permission` | `rememberNotificationPermissionState`, `rememberNotificationPermissionStatus` | Accompanist permission state | Always granted, never prompts. So Home and Profile drop the notification reminder, and Settings hides its "Allow notifications" reminder |
| `forever-ui` | `rememberShareHandler` | Share sheet | Copies to the clipboard |
| `forever-ui` | `rememberCopyToClipboard` | Clipboard | AWT clipboard |
| `compose-result-launcher` | `rememberPickMultipleVisualMediaResultLauncher`, `rememberGetMultipleContentsResultLauncher` | Activity result pickers | AWT `FileDialog`, results as `file:` URIs |
| `compose-photo-capture-state` | `rememberPhotoCaptureState`, `PhotoCaptureState.isAvailable` | Camera, available | Unavailable. Chat, claim details, add files and claim chat read `isAvailable` and do not offer the camera |
| `audio-player-ui` | `CommonMediaPlayer` | `MediaPlayer` | `UnavailableMediaPlayer`: reports an error on prepare |
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
| `feature-onboarding` | `KeepKmp` | Typealias to `androidx.annotation.Keep` (`jvmAndAndroidMain`) | same |
| `feature-onboarding` | `KeypadGlyphPlatformTextStyle` | `PlatformTextStyle(includeFontPadding = false)` | `null` |
| `feature-payin-account` | `rememberSwishAppHandover` | Detects the Swish app and hands over | Not installed: QR only; an http approval URL opens in the browser |
| `feature-profile` | `rememberOpenBugReportEmail` | Email intent | `Desktop.mail` with a `mailto:`; logs when no mail client |
| `feature-profile` | `OpenSourceLicenses` | Web view of the bundled HTML | `NotAvailableOnDesktop` |
| `feature-profile` | `NotificationSettingsRow` | Row opening system settings | Not shown |
| `feature-profile` | `Theme.apply` | `AppCompatDelegate` night mode | No-op; the theme is read through `HedvigAppState.darkTheme` |

### Per-platform Metro bindings

The bindings below sit in `jvmMain` (or `database-jvm`, or `:app-desktop`), next to an Android binding in
`androidMain` or `:app`. No binding is a platform-overridable `commonMain` default; the one `commonMain` binding,
`ExtraApolloClientConfiguration` in `:app-shell`, is a single provider for both hosts. Nothing in the desktop graph
reads `PermissionManager` (its readers are the notification senders in `:app`, `notification-core` and
`logging-device-model`, all Android-only); the notification UI reads `notification-permission`, which reports granted
on the JVM.

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
| `network-clients` | `TlsDiagnostics` (interface in `commonMain`) | Android binding | `JvmTlsDiagnostics`, returns `null` |
| `database-jvm` | `RoomDatabase.Builder<AppDatabase>` | `database-android` | Bundled SQLite at `hedvig_chat_database.db` |
| `feature-chat` | `ChatContext`, `ChatContentResolver` | Typealiases to `Context`, `ContentResolver` | `DesktopChatContext`, `DesktopChatContentResolver` |
| `permission-core` | `PermissionManager` | `ActivityCompatPermissionManager` | `JvmPermissionManager`, every permission not granted (iOS: `NativePermissionManager` in `nativeMain`) |
| `feature-claim-chat` | `AudioRecordingManager` | `MediaRecorder` | `JvmAudioRecordingManager`, reports the recording error state |
| `:app-shell` (`commonMain`) | `ExtraApolloClientConfiguration` | One provider for both hosts | same |
| `:app-desktop` | `MetroViewModelFactory` | `MergedMetroViewModelFactory` | `DesktopViewModelFactory` (one graph holds both scopes) |
| `:app-desktop` | `EventTrackingClient` | Firebase | `LoggingEventTrackingClient`, DEBUG log only |
| `:app-desktop` | `ExperimentProvider` | `UnleashExperimentProvider` | `NoActiveExperimentsProvider`, empty |
| `:app-desktop` | `kotlin.time.Clock`, `TimeZone`, `HedvigDeepLinkMatcher`, `ImageLoader` | `ApplicationMetroProviders` | `DesktopMetroProviders` (Coil with Ktor and SVG only) |
| `:app-desktop` | `Backstack`, `AppBuildConfig` | `ActivityRetainedGraph.Factory`, `AppGraph` | `DesktopGraph.Factory` inputs |
| `:app-desktop` | `RumLogger` (installed, not bound) | Datadog | `LogcatRumLogger` |
| `:app-desktop` | `AppHost`, `ExternalNavigator` (passed to `HedvigApp`) | `AndroidAppHost`, `ExternalNavigatorImpl` | `DesktopAppHost` (its `uriHandler` tries `DesktopUnmatchedLinkRouter` before the browser), `DesktopExternalNavigator` |

`DesktopGraph` is `@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])`. Its
`serializersModules` set is empty: nav key serializers are generated for the Android target only and the desktop
back stack is never persisted.
## Skip register

### Skipped (rule)

Supporting these on desktop would complicate shared code, or desktop has no equivalent.

| What a desktop user sees | Why |
|---|---|
| No push notifications, no notification prompts or reminders, no Settings notifications row, no "turn on notifications" after filing a claim | No push transport; the permission reads as granted |
| Links from email or the browser never open the app; in-app links work | No OS URL handler without packaging (`Desktop.setOpenURIHandler`) |
| The app store review prompt never shows | No store |
| App settings and Play Store actions show a notice dialog. The force-upgrade screen blocks, and its button says to update the checkout | Desktop reports the checkout's `versionName` |
| Calling a number goes to the OS `tel:` handler (FaceTime on macOS), else a notice with the number | No dialer |
| OTP "Open email app" opens a new message, not the inbox | `java.awt.Desktop` can only compose mail |
| No analytics, Datadog RUM or Crashlytics | No desktop SDKs; the calls are logged |
| No staff impersonation | `feature-impersonation` is an Android Activity |
| The back stack is not restored after quitting | No process death on desktop; the stack is in memory |

### Not done yet

Unfinished work and bugs. Most need only a JVM actual or a host change.

| What a desktop user sees | What it would take |
|---|---|
| Trustly checkout (payin, payout, onboarding "connect payment") shows "Not available on desktop" under a working Up arrow, but entering the step still starts a Trustly session on the backend (`SetupTrustlyPayoutMutation`). Swish works by QR | A desktop flow (browser plus status polling, or a JCEF web view) behind `TrustlyCheckoutWebView`; until then, not starting the session |
| Claim chat voice answer: the recorder shows its error state after the countdown, and Send stays enabled and does nothing (a silent no-op). Typing the answer works | Disable Send in that state, or a `TargetDataLine` recorder once the backend's accepted formats are known |
| Audio recordings (claim details, claim chat, chat) show the player's error card, whose retry cannot succeed | A JVM player (JavaFX Media, vlcj) behind `CommonMediaPlayer`, or no retry when unavailable |
| Chat: small file reads block the UI thread; Enter inserts a newline (send with the button); links without a scheme are not detected as GIFs; the cross-sell card and banner never hide for a keyboard | Per item, small |
| Chat video messages are a file card that opens the URL in the browser | A JVM video player behind `ChatVideoMessage` |
| Profile, license attributions: "Not available on desktop" | Render the bundled list as text, or open a hosted page, in the `OpenSourceLicenses` JVM actual |
| A PDF message that failed to send shows an empty tile under its retry overlay (a received PDF whose thumbnail fails shows the file card) | A file card fallback for local PDFs |
| No camera option in chat, claim details, add files or claim chat uploads; gallery and file pickers (AWT file dialog) work | Webcam capture behind `rememberPhotoCaptureState` |
| Claim chat "helipad" loader is blank | A Compose fallback in `HelipadRiveAnimation.jvm.kt` |
| Login background is a still image | A JVM video player behind `LoginBackgroundVideo` |
| BankID: QR only, no "Open BankID" button | `Desktop.browse(bankIdUri)` when a `bankid:` handler exists |
| Sharing a referral link copies it, with no confirmation; copying a claim id or handler email gives no confirmation | A snackbar through `GlobalSnackBarState` |
| "Submit a bug" only logs when no mail client is configured | A notice with the address |
| No experiment variants: the member is in no experiment, so the backend serves defaults | Keep variants in `JvmUnleashClient`, convert `experiments-unleash` |
| Feature flags read never-fetched defaults on every start until the first fetch | Persist the toggle state under the data directory |
| No log file and no crash report: logs go to stdout only | A rotating log file under the data directory |
| The backend gets no device row for a desktop login | A JVM `AuthEventListener` like `DeviceNameLoggingAuthListener` |
| Home and Insurances: pull to refresh cannot be triggered with a mouse | A refresh action or wheel trigger |
| Image viewer zooms by double click and drag only, no pinch or wheel | Wheel zoom |
| Animated GIFs do not animate | An animated image decoder in the desktop `ImageLoader` |
| `AnimatedBorder`'s gradient starts at 0 degrees | A rotated Skia sweep shader in the `AngledSweepGradient` JVM actual |

## iOS debt register

### Library modules without iOS

`commonMain` reaches no plain JVM library except `data-chat`. These KMP modules compile for `iosSimulatorArm64` and
`iosArm64`: `auth-event-core`, `core-demo-mode`, `core-app-review`, `theme`, `experiments-public`,
`navigation-activity`, `notification-badge-data-public`, `data-cross-sell-after-claim-closed`,
`data-cross-sell-after-flow`, `data-display-items`, `data-paying-member`, `data-settings-datastore-public`,
`member-reminders-public`, `initializable`, `apollo-network-cache-manager`. Those iOS compiles are local runs on one
machine; PR CI compiles no iOS target. What is left:

| Module | Reached from `commonMain` of | Blocker | Action |
|---|---|---|---|
| `auth-core-public` (KMP, JVM only) | app-shell, delete-account, login, onboarding, payments, profile | None in its build file: every dependency has iOS targets and `test-clock` is test-only | Add `.ios` and compile it for iOS |
| `language-data` (KMP, JVM only) | payments | `auth-core-public` | `.ios` after `auth-core-public` |
| `data-addons` (KMP, JVM only) | app-shell, cross-sells, 5 features | `androidx.annotation.Keep` on `AddonBannerSource` (no iOS klib) | `KeepKmp`, or drop the annotation |
| `data-changetier` (KMP, JVM only) | app-shell, choose-tier, terminate-insurance | `CostBreakdownEntry` comes from `ui-tiers-and-addons` (Compose UI, no iOS); `ProductVariant` and `AddonVariant` from `data-product-variant-public` (JVM only) | Move the model to a data module; `.ios` on `data-product-variant-public` |
| `data-chat` (plain JVM) | feature-chat | Room: the `room()` convention hard-codes `ksp`/`kspTest`; depends on `data-product-variant-public` (JVM only) | Room KSP for KMP in the convention, `.ios` on `data-product-variant-public`, then convert |

The multiplatform conventions set `jvmDefault` to `NO_COMPATIBILITY` on the JVM target and the Android library
compilation, as plain JVM and Android modules have it, so a conversion adds no `$DefaultImpls` classes. Plain JVM
libraries: `data-chat`, `database-core`, `database-jvm`, `apollo-auth-listeners`, `datadog-demo-tracking`,
`auth-event-fake`, the two KSP processors, `test-clock` and the `-test` modules.

### KMP modules without an iOS target

`app-shell`, `audio-player-data`, `audio-player-ui`, `claim-status`, `compose-pager-indicator`,
`compose-photo-capture-state`, `compose-result-launcher`, `core-file-upload`, `core-icons`, `core-markdown`,
`cross-sells`, `data-claim-intent`, `data-product-variant-public`, `file-upload-ui`, `forever-ui`,
`member-reminders-ui`, `notification-permission`, `pullrefresh`, `tier-comparison`, `ui-analytics-consent`,
`ui-claim-flow`, `ui-force-upgrade`, `ui-phone-number`, `ui-tiers-and-addons`, the four library modules above, and
every `feature-*` module except the `-navigation` ones. Each `expect` of the seam register in these modules needs a
`nativeMain` actual (`molecule-public`, `core-common-public`, `core-build-constants` and `KeepKmp` in `data-coinsured` have one). For
`core-icons`, check that `material-icons-core` 1.7.3 iOS klibs link against Compose Multiplatform 1.12.2.

| Also | Action |
|---|---|
| `HedvigQrCode` is in `design-system-hedvig` `jvmAndAndroidMain` and used from `commonMain` of `feature-payin-account` and `feature-login` | Move to `commonMain` with a native QR encoder |
| `CommonLocale.languageTag` has one `jvmAndAndroidMain` actual | A native actual over `NSLocale` |
| `login_still_9x16.webp` exists in Android `res` and in `jvmMain/composeResources` | One shared Compose resource |
| Room's `AppDatabase` (`database-core`, plain JVM) has no `@ConstructedBy`; chat needs a native driver and file location | Room KMP setup for iOS, plus a `chatTransaction` native actual |
| `DeviceIdFetcher`: Android and JVM bodies are identical and iOS gets it from Swift | Optional: one `commonMain` implementation |
| `AppBuildConfig.platformName`: Kotlin/Native exports interface members as required, so a Swift conformer must implement it | Note for any Swift `AppBuildConfig` (none exists on ugglan master) |

### JDK and JVM-only calls the import guard cannot see

| Where | Call | Action |
|---|---|---|
| `feature-chat` `CbmChatViewModel.kt:103,117,136`, `model/CbmChatMessage.kt:64,157,173,189,210,226,242`, `data/CbmChatRepository.kt:239,273,294`; `app-shell` `HedvigEntryProvider.kt:116` | `Uuid.fromString`, `Uuid.randomUUID()`: `java.util.UUID` statics through benasher's `Uuid` typealias | `uuidFrom()` / `uuid4()` |
| `feature-home` `GetHomeDataUseCase.kt:286` | `Dispatchers.IO` without the `kotlinx.coroutines.IO` import needed on native | Add the import |
| Any `commonMain` | Members of `CommonLocale` (`java.util.Locale` on the JVM) and other typealiased JDK types | Found only by an iOS compile; review by hand |

### Annotations and libraries

| Item | Action |
|---|---|
| `@androidx.annotation.Keep` in `commonMain` of `feature-onboarding` (`OnboardingKeys.kt`), `feature-chip-id`, `feature-choose-tier`, `feature-addon-purchase` and `data-addons`, each allowed by name in `config/commonMain-import-allowlist.txt` | `KeepKmp` (as in `feature-onboarding` and `data-coinsured`) |
| `:navigation-compose` (iOS) pulls Compose UI and the design system through the JetBrains `navigation3-ui` artifacts | Keep it out of `:umbrella` unless that is intended |

### Sharing with iOS without UI

- `:umbrella` exports `authlib` and `logging-public` only, as a static XCFramework with `smallBinary`, with no
  Compose or Skiko linked. The Ugglan app calls into it for auth and logging.
- Measured shipped size (`size -m`, `ios-arm64` slice): 75.8 MB with Compose UI; 7.33 MB compose-free with seven
  exports; 4.40 MB with the two exports; 3.59 MB with `smallBinary`. A Molecule presenter on the Compose runtime, without
  Compose UI, adds 1.3 MB; exporting `network-clients` adds about 2.9 MB (Apollo, Arrow, Okio). No data layer or
  feature has been measured.
- Presenters (`MoleculePresenter`) need the Compose runtime, not Compose UI. Features keep presenters and Compose UI
  in the same source set, so sharing presenters without UI needs them separated, most cheaply by source set.
- The two apps have drifted in three places: the conversation deep link (`/conversation/{id}` on Android,
  `conversation?conversationId=` on iOS); analytics event definitions written twice (`cross_sell_*`,
  `addon_purchased`); and same-named GraphQL operations with different bodies (`query Home` is 17 lines on iOS and
  131 on Android).

## Converting a module

1. A commit of pure `git mv`: `src/main/kotlin` to `src/commonMain/kotlin`, `src/main/graphql` to
   `src/commonMain/graphql`, `src/test/kotlin` to `src/jvmTest/kotlin`, and files that need Android API to
   `src/androidMain/kotlin` as `X.android.kt`. `git show -M --summary HEAD` lists only 100% renames.
2. A commit with the build file and the seams: `hedvig.multiplatform.library`, plus `.android` for a module with
   Android code, resources or the `navKeys()`/`viewModels()` processors, plus `.ios` when every dependency compiles
   for iOS; dependencies in `kotlin { sourceSets { commonMain.dependencies { } } }`; the `libs.jetbrains.*` Compose
   artifacts. Robolectric tests go to `androidHostTest`, as in `feature-flags`.
3. Verify: `:<module>:test` runs as many tests as before (a test left in `src/test` runs nothing),
   `checkCommonMainImports`, `:app:assembleDebug`, `:app-desktop:jvmTest`, and the compiled `:app` graph keeps the
   same ViewModels and deep link matchers (the `javap` recipe in `CLAUDE.md`). Library modules are release-only, so
   compile Android with `compileAndroidMain`.

## Working on the Android app after this change

| Topic | What to do |
|---|---|
| Where code lives | Features and most libraries keep their code in `src/commonMain/kotlin`; Android-only files are `X.android.kt` in `src/androidMain/kotlin`; tests are in `src/jvmTest/kotlin` (Robolectric tests in `src/androidHostTest/kotlin`) |
| Tests | `./gradlew test` runs every unit test; `./gradlew :feature-home:test --tests "*HomePresenterTest*"` works in every module |
| Android-only code written in `commonMain` | `android.*` or `LocalContext` compiles for Android and the app runs; the JVM compile fails, and `checkCommonMainImports` (in `./gradlew check` and CI) reports it. `java.*` compiles on both targets and only the guard reports it |
| An Android-specific need in shared code | Three files: an `expect` (or an interface) in `commonMain`, the Android `actual` in `androidMain`, a JVM `actual` in `jvmMain` |
| A new feature module | `hedvig.multiplatform.library` plus `.android`; its entries called from `hedvigEntryProvider` in `:app-shell`; a dependency in `:app`, `:app-shell` and `:app-desktop` |

Build times, `229bd79df0` against `884ee23e96`, single runs on a shared machine, so indicative: clean
`:app:assembleDebug` 6m05s to 6m44s; a one-line edit in a feature, median 11 s to 12 s. Unchanged: `@Preview`s and
their tooling, string and drawable resources (Compose resources before and after), ktlint, every library version
apart from three JetBrains Android wrappers and `savedstate` 1.3.6 to 1.4.0, and the APK size (debug 118 KB smaller,
minified within 300 bytes). Lint analyses `commonMain` of every converted module; modules with both an Android and a
JVM target are linted twice, once per target.

## Module graph

157 Gradle modules at `0d94497df0`, by convention plugin:

| Kind | Modules |
|---|---|
| KMP with iOS targets | 62 |
| KMP, Android and JVM | 46 |
| KMP, JVM only | 10 |
| Android library | 13 |
| Plain JVM (including the lint and ktlint rule modules, the KSP processors and the `-test` modules) | 24 |
| Android application | 2 |

Measured before the eight key modules were folded back (when there were 165 modules), from Gradle's project model:
`commonMain` holds 89% of production Kotlin (53% before the change); 125 of the 139 modules `:app` reaches, 98.4% of
their Kotlin lines, are also compiled into the desktop host. The modules only `:app` reaches are `compose-webview`,
`core-rive`, `database-android`, `datadog-android`, `datadog-demo-tracking`, `experiments-unleash`,
`feature-impersonation`, `language-migration`, `logging-device-model`, `notification-core`, `notification-firebase`,
`shareddi`, `tracking-datadog`, `tracking-firebase`.

A feature's keys live in the feature. A separate `feature-{name}-navigation` module exists only where another module
navigates to that feature without depending on it.

## Deviations from the plan

| Plan said | Built instead |
|---|---|
| Navigation UI may need `jvmAndAndroidMain` and an iOS debt entry | JetBrains `navigation3-ui` publishes desktop and iOS; the six files moved to `commonMain` of `:navigation-compose` and compile for iOS |
| Converted ViewModels run on desktop | They did not: Molecule needs a frame clock on the JVM; `molecule-public` got one |
| Hosts pass nav keys to the entry decorators | They pass Nav3 content keys |
| `compose-webview` becomes KMP | Stays Android; each caller (licenses, Trustly) has its own seam |
| JVM bindings for existing seams: a short list | 7 missing in the libraries, then 12 more found by compiling a desktop graph |
| `platformName` overridden by the host's build constants | `AppBuildConfig.platformName`, defaulting to `defaultPlatformName` per target |
| `AppHost` interface sketched in the plan | Real shape: `uriHandler`, `shouldShowPermissionRationale`, `tryShowAppStoreReviewDialog`, `SystemBarsEffect`; Datadog's effect stays in `MainActivity` |
| Shell keys (tab roots, `LoginKey`, onboarding) move to `-navigation` modules | They stay in their features, as do `ChatKey`, `InboxKey`, `ClaimDetailsKey` and `ImageViewerKey`; `:app-shell` depends on the features. Eight key modules were created for them and later folded back |
| A host-entries block that ends holding Android-only entries | Built, then deleted once every feature was shared; replaced by the optional `fallbackEntry` |
| Desktop notification permission reads "not granted" | Reads "granted", so no prompt or reminder appears |
| Share sheet "Not available" | Copies to the clipboard |
| Email validation and PDF sharing as plain moves | New seams `isValidEmail` and `rememberOpenPdf` |
| Tests move to `jvmTest` | Robolectric tests go to `androidHostTest` (feature-flags precedent) |
| A final "entries sweep" | Nothing was left; replaced by an audit |
| Phase 5: `hedvig.multiplatform.library` on 20 modules | JVM-only alone does nothing for iOS, so `.ios` per module; 17 modules; `graphql` dirs move too; `java.util.UUID` must change; no `.android` plugin |
| (not planned) | `LogoutOnUnauthenticatedInterceptor` and `LoggingInterceptor` moved to `:app-shell` so desktop logs out on an expired session; a build rule forbids `feature-*` depending on `:app-shell` |
| Guard appended to the ktlint step | Its own `pr.yml` step, since the ktlint step is `continue-on-error` |
| Escape mapped to the system back | Escape, Cmd+[ and Cmd+Left on macOS, Alt+Left elsewhere, the mouse back button; back at the root does nothing instead of quitting |
| (not planned) | Hedvig links that no matcher handles stay in the desktop app; KMP JVM and Android compilations set `jvmDefault` to `NO_COMPATIBILITY` |
| Each phase is a series of small PRs, one module per PR | Each change is its own branch, merged into one integration branch, `feat/desktop-app-integration`; no PR is open |
| Per conversion, and at the end of the project, a person exercises the desktop app by hand against staging | Replaced by headless walkthroughs of every registered key (demo mode and a staging session at `f674f564ce`, which skipped the 28 screens that write on entry and 15 without suitable data; a second staging session with `884ee23e96` merged and nothing skipped) plus a person's use of login, Home, the tabs and the help center |
| No iOS targets are added in this project | Fifteen library modules have iOS targets |

## Verification status

"Author" is whoever made the change, reporting on their own run. "Independent" is a second party: a reviewer reading
the code, or the integrator re-running a build, driving a device or viewing screenshots.

| What | How it was verified, and by whom | Result |
|---|---|---|
| Tests per change | On every branch: the module's tests before and after (same count), `checkCommonMainImports`, `:app:assembleDebug`. Author, and re-run by the integrator on most branches; every branch's diff read by a reviewer, except small changes (the chat camera change, the `jvmDefault` change, the test task and guard changes, the chat thumbnail and follow fixes, and some fix rounds) whose diffs the integrator read | Pass |
| Repo-wide tests | `test jvmTest checkCommonMainImports :app:assembleDebug` at `150f117a53`, independent. `./gradlew test` at `d9e1ec3c8a` (after the key modules were folded), author: 1,154 tests | Pass, 0 failures |
| Android dependency graph | `javap` of `AppGraph$Impl` and `ActivityRetainedGraphImpl` before and after each conversion: 87 ViewModel entries, 21 deep link matcher providers, 111 nav keys; jar member signatures and `releaseRuntimeClasspath` for the library conversions; serial names of the nine folded keys before and after the fold. Author; independent re-checks on a few branches | Identical on every comparison |
| Desktop graph and entries | `DesktopGraphTest`, `DesktopEntryProviderTest`, the per-module JVM graph tests, headless tests of the back keys, link router, Home hero, navigation bar highlight, chat thumbnail fallback (`ChatFileThumbnailTest`) and chat follow (`ChatFollowsNewestMessageTest`), at `0d94497df0` (author, within the module tests) | Pass |
| iOS compiles | `compileKotlinIosSimulatorArm64` and `compileKotlinIosArm64` of the library modules and `:navigation-compose`, local runs on one machine (author, and the integrator for `:navigation-compose`). Not in PR CI | Pass |
| R8 build | `:app:assembleStaging` (minified) at `4a8672217e`, `ed80d6b267`, `884ee23e96` and `d9e1ec3c8a`, independent | Builds and runs on the device |
| APK size | `apkanalyzer` on debug and minified staging APKs, `229bd79df0` against `884ee23e96`, author | Debug 118 KB smaller; minified 287 bytes smaller (download size 157 bytes larger) |
| Build time | Clean `:app:assembleDebug` and a one-line incremental edit, `229bd79df0` against `884ee23e96`, single runs on a shared machine, author | Clean 6m05s to 6m44s; incremental median 11 s to 12 s. Indicative only |
| Device: launch and navigation | Pixel 6 Pro, Android 17, debug and minified staging 14.6.3 (43) at `ed80d6b267`, logged out and logged in, author. Cold start and system bars in dark and light; all five tabs, with per-tab back stacks kept across tab switches; back one level deep, at tab roots (to Home) and at Home (leaves the app) | Pass on both builds |
| Device: process death | Same run, author. Debug: Profile/Contact with Insurances holding details, `ClaimDetailsKey`, `ChatKey`, `HelpCenterQuestionKey`. Minified: insurance details with Profile holding Contact, `ChatKey`, `HelpCenterQuestionKey`. Two minified restores viewed by the integrator (independent) | Each restored to the same screen and stacks; no R8 or serialization error |
| Device: deep links and screens | Same run, author. Deep links logged out (held, landed after login) and logged in (`/payments`, `/inbox`, a help center question); help center topic and question; claim details with audio playback and the add files sheet; the connect payment entry and the method list; chat pickers opened and closed, and a rotation with a picker open. No crash or ANR; the only mutations were the three login side effects | Pass |
| Device: chat end to end, debug | Debug build at `884ee23e96`, driven by hand by the integrator (independent), support replies sent from the staff tool on staging. Text send and delivery state; 45 messages sent and history paged back to the top in order; an image uploaded, rendered and opened in the image viewer; a PDF uploaded, rendered as a thumbnail and opened externally; a new conversation started; inbox previews and ordering; a support reply arriving live | Pass |
| Device: chat, minified | Minified staging build at `884ee23e96`, same way. History with attachments, text send, an image attachment, paging to the top, a support reply arriving live with the conversation open. Messages and attachments sent from the desktop app render on Android | Pass |
| Device: upload retry after process death | Debug build at `884ee23e96` with a temporary, uncommitted failure hook, integrator. A failed image upload, force stop, relaunch | The failed message keeps its thumbnail; retry succeeds |
| Device: flows that write on entry | Debug build at `884ee23e96`, integrator: claim start (first question answered), moving, change tier, travel add-on offer, Trustly web view, Swish setup, each opened and left | No crash, no GraphQL error |
| Device: process death after the key modules were folded | Debug and minified builds at `d9e1ec3c8a`, integrator: a conversation with content on screen, process killed, relaunched | Restored to the same conversation with its content; no serialization error |
| Device: chat follows a new message after a background | `develop` debug builds, integrator: without the fix the list stays on the previous message after a support reply arrives in the background; with the fix it follows. On the integration branch the scenario is covered by `ChatFollowsNewestMessageTest` (author) | Fixed; not re-run on an integration build |
| Desktop walkthrough, demo mode | Headless harness at `f674f564ce`, every registered key pushed in the real desktop graph. Author; screenshots viewed by the integrator | 120 visits: 90 rendered (41 show the generic error because demo mode has no data for them), 0 threw, 0 timed out, 29 need an id, 1 not available |
| Desktop walkthrough, staging session | Harness with `884ee23e96` merged, on a logged-in staging session with no screen excluded. Author; screenshots viewed by the integrator | 156 visits: 125 rendered, 0 threw, 0 timed out, 29 need the output of an earlier flow step, 2 not available (Trustly checkout, licenses). The four desktop fixes confirmed in a rendered scene. Chat history renders; two texts, an image, a PDF and a new conversation were sent from desktop |
| Seen by a person on desktop | The owner, on staging, macOS. At `961f975c97`: BankID QR login; the app's log also records navigation to Home, `OnboardingKey`, its steps and `SetupSwishPayinKey`, which shows those screens were navigated to, not that a person judged them. At `f674f564ce`: Home, the tabs, the help center (where the owner found the four problems fixed at `ed80d6b267`), with the log recording insurance details, chat, claim details, payments and profile screens | Login works; four problems found and fixed. The fixes are covered by headless tests, not yet by a person |
| Follow-ups at `884ee23e96` | Per-platform `PermissionManager`, production stdout filter, `java.net.URI` file paths, clearing an unsupported stored language. Tests by the author; diff read by an independent reviewer; Android graph compared for the permission binding by the author | Pass |
| PR CI | None on these branches; only the separate chat follow fix for `develop` has run CI | Not run |

### Not verified

- PR CI on these branches.
- The six flows that write on entry, on the minified build.
- On desktop: a support reply arriving while the app runs, and an image or file sent from the staff tool.
- The cold start where many messages wait: a separate timing issue in the same scroll effect (`ScrollToBottomEffect`),
  present on `develop`.
- Android Studio behaviour in `commonMain`: highlighting, previews, navigation between `expect` and `actual`.
- Datadog RUM view names on Android against the previous release. Look up version 14.6.3 (43) on 2026-11-13 UTC:
  staging build (`com.hedvig.app`) 09:25:35 to 09:26:56, 09:45:01 to 09:54:49 and 11:32 to 11:35; debug build
  (`com.hedvig.dev.app`) 09:22:18 to 09:25:10, 09:28:58 to 09:44:30 and 11:28 to 11:32.
- Production flavour builds: no Android release build, no desktop run with `-Phedvig.desktop.production=true`.
- Startup time; CI duration.
- On Android: the notification permission prompt (the device had it denied and it was left so), a cross-sell link
  with `requiresAuthorization = true`, details of a past payment.
- In a real desktop window: the back keys with nothing focused, Cmd+[ on a non-US keyboard layout, the mouse back
  button over a sheet or dialog, reopening the Home hero with a real trackpad.
- Windows and Linux: never run, by decision; the code keeps their back shortcuts and file paths
  (`file:/C:/...` parsing is covered by `UriToPathTest`).

## For other developers

### Moved files

| What | From | To |
|---|---|---|
| Feature sources | `src/main/kotlin`, `src/main/graphql`, `src/test/kotlin` | `src/commonMain/kotlin`, `src/commonMain/graphql`, `src/jvmTest/kotlin` (platform files to `androidMain` with `.android.kt` names). Modules: core-icons, forever-ui, member-reminders-ui, notification-permission and the features chat, claim-details, connect-payment-trustly, forever, home, image-viewer, insurance-certificate, insurances, login, onboarding, payin-account, payments, profile, terminate-insurance, travel-certificate |
| Robolectric tests | `src/test` of feature-login, feature-payin-account | `src/androidHostTest` |
| Already-KMP entries | `androidMain` of claim-chat, claim-history, remove-addons | `commonMain` |
| Shell | `app/app/src/main/.../app/` (`BackstackController`, `TopLevelRunLogic`, `SessionReconciler`, `HedvigEntryProvider`, `HedvigContentKeys`, `ScreenName`, `ScreenParameterExtractor`, `CurrentDestinationHolder`, `HedvigApp`, `HedvigAppState`, `NavigationSuite`, `TopLevelTabStartDestination`, `GlobalHedvigSnackBar`, `DeepLinkFirstUriHandler`, `AuthorizationCodeUriHandler`, `GetMemberAuthorizationCodeUseCase`, `LoggingInterceptor`) and their tests | `app/app-shell/src/commonMain`, `src/jvmTest`, same packages |
| Nav UI | `:navigation-compose` `androidMain` (`Decorators`, `BottomSheetSceneStrategy`, `NavSuiteSceneDecorator`, `NavigationSuiteType`, the two retained decorators) | `commonMain`, same package |
| Keys | `HomeKey`, `LoginKey`, `InsurancesKey`, `ProfileKey`, `OnboardingKey`, `ClaimDetailsKey` and the `OnboardingGate` and `ResetOnboardingSeenUseCase` interfaces shared files with other declarations | Their own files in the same feature and package (`HomeKey.kt`, `LoginKey.kt` and so on; the implementations in `OnboardingGateImpl.kt`, `ResetOnboardingSeenUseCaseImpl.kt`) |
| Unleash keys | `Feature.unleashKey`, `neverFetchedDefaults` in `feature-flags` `androidMain` | `commonMain` (`FeatureUnleashKey.kt`, `UnleashSettings.kt`) |
| `Language.label` | `language-core` `androidMain/Language.kt` | `commonMain/LanguageLabel.kt` |
| `TlsDiagnostics` | `network-clients` `androidMain` | `commonMain` |
| Library sources | `src/main/kotlin`, `src/main/graphql`, `src/test/kotlin` of 17 libraries that were plain JVM modules (`auth-event-core`, `auth-core-public`, `core-demo-mode`, `core-app-review`, `theme`, `experiments-public`, `navigation-activity`, `notification-badge-data-public`, `language-data`, `data-addons`, `data-changetier`, `data-cross-sell-after-claim-closed`, `data-cross-sell-after-flow`, `data-display-items`, `data-paying-member`, `data-settings-datastore-public`, `member-reminders-public`) | `src/commonMain/kotlin`, `src/commonMain/graphql`, `src/jvmTest/kotlin` |

### Changed symbols

- `AndroidAppHost` implements the shell's `AppHost`. `HedvigApp` takes an `appHost` and an optional `fallbackEntry`.
- `validateEmail(x).isSuccessful` callers use `isValidEmail(x)`.
- `platformName` is `AppBuildConfig.platformName`; the old `expect val platformName` is `defaultPlatformName`.
- `NoopFeatureFlagProvider` is deleted. `mainDispatcher` in `molecule-public` is public.
- `HostEntryContext`, `AndroidHostEntries.kt` and the host entries block no longer exist.
- `deepLinkHostsOf(flavor)` in `core-build-constants` is public (same host values as before).
- `isSupportedLocaleTag(localeTag)` in `language-core` `commonMain` is public.
- `PermissionManager` has a binding per platform; `NoopPermissionManager` is deleted and `ActivityCompatPermissionManager` no longer uses `replaces`.
- `java.util.UUID.randomUUID()` in `auth-core-public` and `member-reminders-public` is `uuid4()` (the same call on the JVM).

### Shared behaviour changes that reach Android

| Change | File | Test |
|---|---|---|
| On Home with the hero collapsed, an upward, mostly vertical mouse wheel scroll that the list leaves unused reopens the hero in one animation. Touch input produces no wheel event and is unchanged | `feature-home` `HomeDestination.kt` | `HomeHeroScrollTest` |
| Navigation bar items drop their hover or focus highlight when the bar moves between scenes: a new collector first receives the end of every hover and focus the previous one saw start. Press ripples and touch are unchanged | `design-system-hedvig` `internal/MappedInteractionSource.kt` | `MappedInteractionSourceTest`, `NavigationBarHighlightTest` |
| A chat image or PDF attachment whose thumbnail fails to load renders as the file card, which opens the URL, instead of an empty tile | `feature-chat` `ChatLoadedScreen.kt` (`ChatFileThumbnail`) | `ChatFileThumbnailTest` |
| The chat list follows a newly arrived message after the screen returns from the background: `ScrollToBottomEffect` reads the paging list through `rememberUpdatedState`, so it observes the list the new presenter produces. The same bug exists on `develop` and is fixed there separately | `feature-chat` `ChatLoadedScreen.kt` | `ChatFollowsNewestMessageTest` |

### Observed, not fixed

Seen on builds of this branch; not compared against a build of `develop` unless stated.

- Each return to Home fires `ActiveInsuranceContractTypes`, `MemberActions` and `AvailableSelfServiceOnContracts`
  twice. The cause is in shared presenter code; seen on Android and desktop.
- Tapping a co-insured row in the edit list opens the remove confirmation directly.
- A deep link from outside the app (`am start` VIEW intent) stacks a second `MainActivity` on the existing task, so
  after landing, one Back on Home appears to do nothing while the second activity unwinds.
- The login screen in dark mode shows white status bar icons over a light background (the same logic as `develop`).
- `MissedPayment` is queried on the login screen while logged out, which forces a redundant logout (the same as
  `develop`).
- On a cold start with a stored session, `LoginKey` is navigated to about 30 ms before `HomeKey`, behind the splash;
  it may record a Login view in Datadog RUM. It does not happen on every cold start.
- Production `link.hedvig.com` links in help center answers are not matched by the develop and staging flavours and
  go to the OS on Android (Google Play on the device); only release builds navigate them in the app. The desktop app
  rewrites them to the staging host.

### Open branches that collide

| Branch | Collision |
|---|---|
| PR 3305 (`feat/native-purchase-flow`) | Edits `feature-login` `build.gradle.kts`, `SwedishLoginDestination.kt`, `SwedishLoginPresenter.kt` and moves `BankIdState.kt` into an Android-only `app/ui/ui-bankid` with `rememberBankIdState(autoStartToken: String)`. Here `BankIdState` is an expect/actual seam: the interface and `internal expect fun rememberBankIdState(bankIdUri: com.eygraber.uri.Uri)` in `commonMain`, `BankIdStateImpl` in `androidMain/.../BankIdState.android.kt`, a JVM actual. `ui-bankid` would have to become KMP with the same seam, or `feature-login` keeps its own. The branch also changes `insuranceEntries` (`openCrossSell: (CrossSell) -> Unit`) and makes `InsuranceContractDetailKey` public; a `commonMain` user outside the module would need it in `feature-insurances-navigation` |
| `feat/product-pages` | Four files in `feature-insurances` and seven in `feature-onboarding`, all moved by pure renames; changes the onboarding entries signature |

### Dead code (listed, not deleted)

- `validateEmail` and `ValidationResult` (`core-common-public` `androidMain/.../validation/`).
- `SubmittedAndClosedColumns.kt` (`feature-claim-details` `androidMain`, called only from its preview).
- `rememberFilePicker` with both actuals and `FilePicker` (`feature-claim-chat`).
- Empty `<manifest />` files in `androidMain` of feature-forever, feature-home, feature-insurance-certificate,
  feature-insurances, feature-payments, feature-terminate-insurance, feature-travel-certificate.

### Landing the work

The proposed split into pull requests is in `docs/superpowers/plans/2026-11-13-desktop-app-pr-split.md`.
