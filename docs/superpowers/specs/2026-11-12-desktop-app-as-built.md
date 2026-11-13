# The Hedvig app on desktop: as built

State of `feat/desktop-app-integration` at `884ee23e96`. The design is in `2026-11-12-desktop-app-design.md`.

- Run it with `./gradlew :app-desktop:run` (staging) on macOS; log in with BankID by QR code or with email.
- Every feature except staff impersonation runs on shared code. Trustly checkout, audio, video, camera, push and OS
  deep links do not work on desktop; the skip register lists every gap.
- The Android app passed a device smoke run after the change, including process-death restore on a minified build.
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
and the visible text per screen lives on the branch `tool/desktop-walkthrough` (tooling only, not merged, based on
`f674f564ce`). On that branch: `./gradlew :app-desktop:desktopWalkthrough` (demo mode), or, with the desktop app
closed and a logged-in staging session, add `-Phedvig.walkthrough.mode=session -Phedvig.walkthrough.sessionInPlace=true
-Phedvig.walkthrough.sessionDataDir=$HOME/.hedvig-desktop/staging`. Session mode never pushes the keys that write on
entry. Output: `app/app-desktop/build/walkthrough/`.

## Modules

| Module | Plugins | Holds |
|---|---|---|
| `:app-shell` (`app/app-shell`) | `hedvig.multiplatform.library` + `.android` (JVM, Android) | `HedvigApp`, `HedvigAppState`, `NavigationSuite`, `BackstackController`, `TopLevelRunLogic`, `SessionReconciler`, `HedvigEntryProvider` (`hedvigEntryProvider`, registers every key for every host), `AppHost`, `DeepLinkFirstUriHandler`, `AuthorizationCodeUriHandler`, `GlobalHedvigSnackBar`, and the Apollo chain shared by both hosts (`ApolloInterceptorMetroProviders`, `LoggingInterceptor`, `LogoutOnUnauthenticatedInterceptor`). The shell's tests are in its `jvmTest`. |
| `:app` | `hedvig.android.application` | The Android host: `MainActivity`, `HedvigApplication`, `AppGraph`, `ActivityRetainedGraph`, `NavRetainedViewModel`, `MergedMetroViewModelFactory`, `NavigationStateBridge`, `AndroidAppHost`, external deep links, notifications, WorkManager, Firebase and Datadog (`Navigation3TrackingEffect` is composed in `MainActivity` right before `HedvigApp`). `ExhaustiveBackStackSerializationTest` and `BackStackSerializationTest` stay here. |
| `:app-desktop` (`app/app-desktop`) | `hedvig.multiplatform.library` (JVM only), Compose Desktop | `Main.kt` (window), `DesktopBack` (back shortcuts and the mouse back button), `DesktopUnmatchedLinkRouter`, `DesktopGraph`, `DesktopMetroProviders`, `DesktopBindings`, `DesktopAppHost`, `DesktopExternalNavigator`, `DesktopAppBuildConfig`, `DesktopFallbackEntry`, `DesktopIntegration` (`java.awt.Desktop` actions, notice dialogs), `DesktopWindowSize`, `DesktopLogging`, `StdoutLogcatLogger`. |
| `-navigation` modules added for the shell | `.ios` + `.android` | `feature-home-navigation` (`HomeKey`), `feature-login-navigation` (`LoginKey`), `feature-insurances-navigation` (`InsurancesKey`), `feature-profile-navigation` (`ProfileKey`), `feature-onboarding-navigation` (`OnboardingKey`, `OnboardingGate`, `ResetOnboardingSeenUseCase`, `KeepKmp`), `feature-chat-navigation` (`ChatKey`, `InboxKey`), `feature-claim-details-navigation` (`ClaimDetailsKey`), `feature-image-viewer-navigation` (`ImageViewerKey`). |
| `database-jvm` (`app/database/database-jvm`) | `hedvig.jvm.library` | `DatabaseJvmMetroProviders`: Room `AppDatabase` builder with `BundledSQLiteDriver`. |

Every feature module except `feature-impersonation` is KMP with its screens, presenters, ViewModels, entries and deep
links in `commonMain`. No entry is registered by a host: `hedvigEntryProvider` registers all 111 nav keys on both.
Android passes no fallback entry, so an unregistered key still throws there. Desktop passes `desktopFallbackEntry`,
which shows "Not available on desktop" and logs the key once at WARN; `DesktopEntryProviderTest` asserts no key
needs it.

Android-only (`hedvig.android.library`): `core-rive`, `database-android`, `datadog-android`,
`accessibility-test`, `experiments-unleash`, `feature-impersonation`, `language-migration`, `logging-device-model`,
`notification-core`, `notification-firebase`, `tracking-datadog`, `tracking-firebase`, `compose-webview`.
## Seam register

Seams between the Android and desktop implementations. The Android column is the Android code behind the seam.

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
| `feature-onboarding-navigation` | `KeepKmp` | Typealias to `androidx.annotation.Keep` (`jvmAndAndroidMain`) | same; native actual exists |
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
`nativeMain` actual (`molecule-public`, `core-common-public`, `core-build-constants` and `KeepKmp` have one). For
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
| `@androidx.annotation.Keep` in `commonMain` of `feature-onboarding` (`OnboardingKeys.kt`), `feature-chip-id`, `feature-choose-tier`, `feature-addon-purchase` and `data-addons`. `checkCommonMainImports` does not check `androidx` | `KeepKmp` (as in `feature-onboarding-navigation` and `data-coinsured`) |
| `:navigation-compose` (iOS) pulls Compose UI and the design system through the JetBrains `navigation3-ui` artifacts | Keep it out of `:umbrella` unless that is intended |

## Converting a module

1. A commit of pure `git mv`: `src/main/kotlin` to `src/commonMain/kotlin`, `src/main/graphql` to
   `src/commonMain/graphql`, `src/test/kotlin` to `src/jvmTest/kotlin`, and files that need Android API to
   `src/androidMain/kotlin` as `X.android.kt`. `git show -M --summary HEAD` lists only 100% renames.
2. A commit with the build file and the seams: `hedvig.multiplatform.library`, plus `.android` for a module with
   Android code, resources or the `navKeys()`/`viewModels()` processors, plus `.ios` when every dependency compiles
   for iOS; dependencies in `kotlin { sourceSets { commonMain.dependencies { } } }`; the `libs.jetbrains.*` Compose
   artifacts. Robolectric tests go to `androidHostTest`, as in `feature-flags`.
3. Verify: the module's `jvmTest` runs as many tests as `test` did (a test left in `src/test` runs nothing),
   `checkCommonMainImports`, `:app:assembleDebug`, `:app-desktop:jvmTest`, and the compiled `:app` graph keeps the
   same ViewModels and deep link matchers (the `javap` recipe in `CLAUDE.md`). Library modules are release-only, so
   compile Android with `compileAndroidMain`.

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
| Escape mapped to the system back | Escape, Cmd+[ and Cmd+Left on macOS, Alt+Left elsewhere, the mouse back button; back at the root does nothing instead of quitting |
| (not planned) | Hedvig links that no matcher handles stay in the desktop app; KMP JVM and Android compilations set `jvmDefault` to `NO_COMPATIBILITY` |
| Each phase is a series of small PRs, one module per PR | Each change is its own branch, merged into one integration branch, `feat/desktop-app-integration`; no PR is open |
| Per conversion, and at the end of the project, a person exercises the desktop app by hand against staging | Replaced by headless walkthroughs of every registered key (demo mode and a real staging session, both at `f674f564ce`) plus a person's use of login, Home, the tabs and the help center. The 28 screens that write on entry and the 15 without suitable data were not visited |
| No iOS targets are added in this project | Fifteen library modules and the eight new `-navigation` modules have iOS targets |

## Verification status

"Author" is whoever made the change, reporting on their own run. "Independent" is a second party: a reviewer reading
the code, or the integrator re-running a build or viewing screenshots.

| What | How it was verified, and by whom | Result |
|---|---|---|
| Tests per change | On every branch: the module's tests before and after (same count), `checkCommonMainImports`, `:app:assembleDebug`. Author, and re-run by the integrator on most branches; every branch's diff read by a reviewer, except a few small changes (the chat camera change, the `jvmDefault` change and some fix rounds) whose diffs the integrator read | Pass |
| Repo-wide tests | `test jvmTest checkCommonMainImports :app:assembleDebug`, independent, at `150f117a53` (all library conversions merged). `test jvmTest` by the author at the `jvmDefault` change (`4dee5ff43b`): 1112 tests. The last merge, `ed80d6b267`, ran only the tests of the modules it touched (author) | Pass, 0 failures |
| Android dependency graph | `javap` of `AppGraph$Impl` and `ActivityRetainedGraphImpl` before and after: 87 ViewModel entries, 21 deep link matcher providers, 111 nav keys; jar member signatures and `releaseRuntimeClasspath` for the library conversions. Author on each conversion; independent re-checks on a few branches. `ed80d6b267` changes no Metro annotation and was not compared | Identical on every comparison |
| Desktop graph and entries | `DesktopGraphTest`, `DesktopEntryProviderTest`, the per-module JVM graph tests, headless tests of the back keys, link router, Home hero and navigation bar highlight, at `ed80d6b267` (author, within the module tests above) | Pass |
| iOS compiles | `compileKotlinIosSimulatorArm64` and `compileKotlinIosArm64` of the library modules and `:navigation-compose`, local runs on one machine (author, and the integrator for `:navigation-compose`). Not in PR CI | Pass |
| R8 build | `:app:assembleStaging` (minified) at `4a8672217e` and again at `ed80d6b267`, independent | Builds. APK size not compared with `develop` |
| Device run: launch and navigation | Pixel 6 Pro, Android 17, debug and minified staging 14.6.3 (43) from `ed80d6b267`, logged out and logged in (author). Cold start and system bars in dark and light; all five tabs, with per-tab back stacks kept across tab switches; back one level deep, at tab roots (to Home) and at Home (leaves the app) | Pass on both builds |
| Device run: process death | Same run. Debug: Profile/Contact with Insurances holding details, `ClaimDetailsKey`, `ChatKey`, `HelpCenterQuestionKey`. Minified staging: insurance details with Profile holding Contact, `ChatKey`, `HelpCenterQuestionKey`. Two staging restores viewed in screenshots by the integrator (independent) | Each restored to the same screen and stacks; no R8 or serialization error |
| Device run: deep links and screens | Same run. Deep links logged out (held, landed after login) and logged in (`/payments`, `/inbox`, a help center question); help center topic and question; claim details with audio playback and the add files sheet; the connect payment entry and the Trustly and Swish method list (nothing selected); chat input with the gallery, camera and file pickers opened and closed, and a rotation with a picker open | Pass |
| Device run: stability and writes | Same run: crash buffer, `FATAL EXCEPTION`, `ANR in`; Chucker on the debug build | No crash or ANR on either build; the only mutations were the three login side effects (`MemberUpdateLanguage`, `MemberLogDevice`, `MemberDeviceRegister`) |
| Desktop walkthrough, demo mode | Headless harness at `f674f564ce`: every registered key pushed in the real desktop graph. Author; the integrator viewed screenshots (independent) | 120 visits: 90 rendered (41 show the generic error because demo mode has no data for them), 0 threw, 0 timed out, 29 need an id, 1 not available (licenses), 0 reached the fallback |
| Desktop walkthrough, real staging session | Same harness at `f674f564ce` on a logged-in session, ids from the app's cache, every GraphQL operation logged. Author; the integrator viewed screenshots (independent) | 134 visits: 84 rendered with real data, 6 generic errors (harness id choice or failure screens by design), 0 threw, 0 timed out, 15 need an id, 1 not available, 0 fallback, 28 excluded; 914 operations, 0 mutations |
| Seen by a person on desktop | The owner, on staging, macOS. At `961f975c97`: BankID QR login; the app's log also records navigation to Home, `OnboardingKey`, its steps and `SetupSwishPayinKey`, which shows those screens were navigated to, not that a person judged them. At `f674f564ce`: Home, the tabs, the help center (where the owner found the four problems fixed at `ed80d6b267`), with the log recording insurance details, chat, claim details, payments and profile screens | Login works; four problems found and fixed. The fixes are covered by headless tests, not yet by a person |
| Follow-ups at `884ee23e96` | Per-platform `PermissionManager`, production stdout filter, `java.net.URI` file paths, clearing an unsupported stored language. Tests by the author; diff read by an independent reviewer; Android graph compared for the permission binding by the author. Not run on a device or in a desktop window (the device run stays at `ed80d6b267`) | Pass |
| PR CI | None | Never run on these branches: no PR is open |

### Not verified

- PR CI on any of these branches.
- Chat with content, on both platforms. The test account's only conversation is empty on the Android device and opened
  empty on desktop, so it is data, but history paging, older timestamps, images in the image viewer, inline video,
  TalkBack descriptions and sending (text, photo, document, failed upload and retry) were not exercised after the
  conversion on either platform.
- The 28 desktop screens that write on entry: `TrustlyKey` (`SetupTrustlyPayoutMutation`), `ClaimChatKey`
  (`ClaimIntentStartMutation`), `ClaimDetailsKey` for a closed claim (`ClaimAcknowledgeClosedStatusMutation`), every
  moving flow key (`MoveIntentV2CreateMutation`), every change tier key (`ChangeTierDeductibleCreateIntentMutation`),
  every addon purchase key (`AddonGenerateOfferMutation`), `PuppyGuideArticleKey` (`PuppyGuideEngagementMutation`),
  `SetupSwishPayinKey` (`SetupSwishPayinMutation`) and `SwedishLoginKey` (starts a BankID attempt). Also the 15
  screens that need an id the session did not provide.
- Everything merged after `f674f564ce` on desktop by walkthrough: the library conversions, the `jvmDefault` change,
  the chat camera change, the four desktop fixes and the four follow-ups. `tool/desktop-walkthrough` is 31 commits
  behind `884ee23e96`.
- Datadog RUM view names on Android against the previous release. Look up version 14.6.3 (43) of the staging build
  (`com.hedvig.app`) on 2026-11-13 UTC, 09:25:35 to 09:26:56 (logged out) and 09:45:01 to 09:54:49 (logged in); the
  debug build (`com.hedvig.dev.app`) ran 09:22:18 to 09:25:10 and 09:28:58 to 09:44:30.
- Production flavour builds: no Android release build, no desktop run with `-Phedvig.desktop.production=true`.
- APK size against `develop`; startup time; build time and CI duration.
- Lint: it analyses nothing in the 17 converted library modules (a JVM-only KMP module gets `lintJvm`, which sees
  `jvmMain` only).
- Staging network traffic on the device: the staging build is not debuggable.
- On Android: the notification permission prompt (the device had it denied and it was left so), a cross-sell link
  with `requiresAuthorization = true`, details of a past payment, opening a claim file other than audio.
- On desktop: `DeleteAccountKey` stayed on its loading indicator in the demo run; every return to Home fires
  `ActiveInsuranceContractTypes`, `MemberActions` and `AvailableSelfServiceOnContracts` twice each (not shown to be
  desktop-specific); in a real window, the back keys with nothing focused, Cmd+[ on a non-US keyboard layout, the
  mouse back button over a sheet or dialog, reopening the Home hero with a real trackpad, the help center payments
  link, and whether one Enter press on the email login screen submits twice.
- Linux and Windows: never run. `JvmFileService` parses Windows `file:/C:/...` URIs in tests only.

## For other developers

### Moved files

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
| Library sources | `src/main/kotlin`, `src/main/graphql`, `src/test/kotlin` of 17 libraries that were plain JVM modules (`auth-event-core`, `auth-core-public`, `core-demo-mode`, `core-app-review`, `theme`, `experiments-public`, `navigation-activity`, `notification-badge-data-public`, `language-data`, `data-addons`, `data-changetier`, `data-cross-sell-after-claim-closed`, `data-cross-sell-after-flow`, `data-display-items`, `data-paying-member`, `data-settings-datastore-public`, `member-reminders-public`) | `src/commonMain/kotlin`, `src/commonMain/graphql`, `src/jvmTest/kotlin`; `:<module>:test` is now `:<module>:jvmTest` |

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

Both affect only mouse, trackpad and keyboard input; touch input produces neither event and is unchanged.

| Change | File |
|---|---|
| On Home with the hero collapsed, an upward, mostly vertical mouse wheel scroll that the list leaves unused reopens the hero in one animation | `feature-home` `HomeDestination.kt` |
| Navigation bar items drop their hover or focus highlight when the bar moves between scenes: a new collector first receives the end of every hover and focus the previous one saw start. Press ripples are unchanged | `design-system-hedvig` `internal/MappedInteractionSource.kt` |

### Observed on the Android device run

Seen on 14.6.3 (43) built from `ed80d6b267`; not compared against a build of `develop`.

- A deep link from outside the app (`am start` VIEW intent) stacks a second `MainActivity` on the existing task, so
  after landing, one Back on Home appears to do nothing while the second activity unwinds.
- The login screen in dark mode shows white status bar icons over a light background.
- `MissedPayment` is queried on the login screen while logged out, which forces a redundant logout.
- On a cold start with a stored session, `LoginKey` is navigated to about 30 ms before `HomeKey`, behind the splash;
  it may record a Login view in Datadog RUM. It does not happen on every cold start.
- Production `link.hedvig.com` links in help center answers are not matched by the develop and staging flavours and
  go to the OS (Google Play on the device); only release builds navigate them in the app.

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

