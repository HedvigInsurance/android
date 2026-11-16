# Sharing presenters and data with iOS: experiment report, step 4 edition

Date: 2026-11-16. State of `feat/desktop-app-integration` at `f446fbfaac` and of the three bridge branches
`ios/bridge-plain` (`0e30384173`), `ios/bridge-nativecoroutines` (`a48caaae8d`) and `ios/bridge-skie` (`eb11dbef4c`).
The design is `docs/superpowers/specs/2026-11-13-ios-sharing-design.md`, the plan
`docs/superpowers/plans/2026-11-14-ios-sharing.md`. Every number below comes from the run directory
`.superpowers/sdd/2026-11-14-ios-sharing/` of the `android/` clone; the file is named next to it. "Ledger" means
`ledger.md` there.

## 1. Summary

**Built.** The claim history screen's ViewModel, presenter, use case and GraphQL client run in Kotlin on iOS and feed
a SwiftUI screen in the iOS test host, `micro-apps/umbrella-consumer` (the shell). A native Metro graph in `:umbrella`
takes five Swift inputs; Swift keeps the tokens in the Keychain and owns their refresh. Twelve task branches are merged
into the integration branch; the three bridge candidates are pushed and not merged.

**Proven** on the iOS 27.2 simulator against staging: BankID login; Loading, Empty, Failed with Retry; leaving the
screen clears the ViewModel and no request follows; main-thread delivery; one shared token refresh; headers match the
iOS app's except `hedvig-device-id`. 30 iOS simulator tests pass. On Android the ViewModel, deep link and key sets and
the APK class set are unchanged, 1199 tests pass, and the compiled graph has two deliberate differences. No Compose UI
symbol is in the framework.

**Size.** 3.59 MB to 7.95 MB shipped, almost all in the step that first linked the data layer.

**Not proven.** Content rows from real data (the only BankID identity has no claims; Kotlin-built sample items were
rendered instead), a member switch, a server error on a device, Android on a device after Task 3, older iOS versions,
production configuration.

**Found.** Three existing bugs on all platforms, not fixed: no `hedvig-device-id` header (on Android since January
2026), `Authorization: Bearer null`, and a reload that never refetches. Fixed on the way: an iOS crash on any GraphQL
error, and tokens in the iOS system log.

**Bridge.** The three candidates behave alike at runtime; only SKIE makes Swift switches exhaustive. Recommended:
plain export for step 5, then SKIE or Kotlin's Swift export chosen before a second screen.

**Step 5, the real iOS app, is not started.** It waits on the owner's gate (section 3.11).

## 2. Bridge recommendation

### Measured comparison

From `bridge-comparison.md`, "Measured comparison" (device run of 2026-11-16, iPhone 18 Pro simulator, iOS 27.2, one
logged-in member with an empty history; evidence in `task-13/`, `task-14/`, `task-15/`).

| Criterion | 1: plain Objective-C export | 2: KMP-NativeCoroutines | 3: SKIE 0.10.16 |
|---|---|---|---|
| Sealed state and outcome enum in Swift | 4 casts, 2 `default` branches the compiler requires; a missing case compiles silently | same as 1: delivery changes, the element type does not | `onEnum(of:)` and real Swift enums; no `default`; a missing case is a compile error |
| Lifetime (owner deinit, ViewModel cleared, owner released about 1 s later, no request after) | holds, 3 of 3 cycles | holds, 3 of 3 | holds, 3 of 3 |
| Main thread precondition | no trap, 6 deliveries (callback plus `MainActor.assumeIsolated`) | no trap, 6 (async sequence on the main actor) | no trap, 6 (`for await` on the main actor) |
| Kotlin exception, declared with `@Throws` | caught as `NSError`, domain `KotlinException` | caught as `NSError` (domain not logged) | caught, same as 1 |
| Kotlin exception, undeclared | process ends, SIGABRT via `Kotlin_ObjCExport_trapOnUndeclaredException` | same | same; and per the candidate's report, an exception inside a SKIE-collected flow ends in `fatalError` (not probed) |
| Swift token fetch failure | Failed with Retry, no crash | same | same, through SKIE's renamed `__fetch` |
| Gradle link `--rerun`, median of 3 | 10.4 s | 7.5 s | 9.1 s |
| Incremental `xcodebuild`, median of 3 | 4.9 s | 4.7 s | 4.5 s |
| Shipped size, delta vs 7,953,504 | +1,431 | +8,237 | +399,785 |
| Kotlin code for the screen | facade 141 + state 29 lines; 1 annotation | +14 (`states`) +16 (wrapper); 2 annotations | +12 (`states`); no annotation; +12 build file lines |
| Swift glue (owner, mappers, mirror types) | 112 lines; 4 manual lifetime pieces | 116 lines; 4 lifetime pieces | 3 lifetime pieces; the 15-line outcome mirror becomes deletable |
| Header lines | 6,013 | 6,026 | 7,971, plus a 9,842-line apinotes file and 71 generated Swift files (4,949 lines) |
| Dependencies added | none | none new: the library already serves login in both apps | SKIE Gradle plugin and its Kotlin and Swift runtimes in the framework |

The timings are noise: the first run of each candidate included task graph calculation, another app used about two
cores throughout, and the spread inside a candidate is larger than between candidates (`task-13-15-device-report.md`).

### Recommendation

To the owner, not a decision: **use plain Objective-C export for step 5 and for as long as one screen is shared, and
choose between SKIE and Kotlin's Swift export before a second feature is converted.** KMP-NativeCoroutines stays where
it is, for login, and is not adopted for screens.

What decides it:

1. **The candidates differ on one criterion only.** Lifetime, main-thread delivery, exception behaviour, the token
   failure path and build time came out the same for all three. The one real difference is exhaustiveness, and only
   SKIE fixes it. So the choice is SKIE or not, and KMP-NativeCoroutines buys little: its element type is still the
   Objective-C class, it keeps the casts and both `default` branches, `close()` is still manual, and its only gains are
   a typed value and no `assumeIsolated`, for 4 more Swift lines (`bridge-comparison.md`, candidate 2).
2. **SKIE's costs are paid from the first day and are hard to back out of later; plain export's cost grows with each
   screen but stays cheap to change while there is one.** With SKIE every Kotlin upgrade waits for a SKIE release that
   lists it (an unsupported Kotlin version is an error at install time; 0.10.16 is the first with 2.4.21,
   `task-15-availability.md`), on top of Metro, which already has to move with Kotlin. It changes the exported API of
   every exported module: Swift-implemented suspend requirements are renamed (`fetch` became `__fetch` in two shell
   inputs), Kotlin enums cross through `_ObjectiveCBridgeable`, an underscored Swift protocol, and an exception thrown
   inside a collected flow ends in `fatalError` per the candidate's report. It sends build analytics to its vendor by
   default: in this experiment the first link on `ios/bridge-skie` ran the upload before the opt-out was added; the
   payload as collected locally was a hashed project id, git contributor, commit, branch and tag counts, the Mac model
   and memory, Kotlin, Gradle, JVM and macOS versions and the compiler configuration, with no project name (ledger,
   "DISCLOSURE"). Every build since reports the upload task SKIPPED. Swift code written against `onEnum(of:)` and
   SKIE's enums is tied to it. Moving one screen from plain export to SKIE later rewrites the owner class and the state mapper,
   about 55 Swift lines on this screen, and lets the facade drop about 14 net Kotlin lines.
3. **The thing worth waiting for is Kotlin's own Swift export.** It is Alpha since Kotlin 2.4.0 and exports suspend
   functions as `async`, `Flow` as an `AsyncSequence` and sealed hierarchies as Swift enums for an exhaustive `switch`,
   which is SKIE's main benefit without a third party. It does not support generics, supports only final classes
   inheriting from `Any`, replaces `embedAndSignAppleFrameworkForXcode`, and does not document coexisting with the
   Objective-C export that login uses today (`task-15-availability.md`). Not usable now; it could be within the time it
   takes to share a second feature.

The case against this recommendation, stated as strongly as the evidence allows. The owner's rule is that iOS must
never be second class. Under plain export a new Kotlin state or outcome is a silent fallback in Ugglan: the state
becomes Failed with an error log, a new outcome shows "Unknown", and nothing fails to compile. Across 26 features that
is a recurring class of bug that Android, with `when` over a sealed type, never has. SKIE removes it today with no
annotations in shared code, which fits the rule against Swift-only annotations in common presenters, and 400 KB is
about 5% of the framework, where size is monitored and not a limit. If the owner reads "never second class" as
requiring compile-time exhaustiveness on iOS from the first shipped screen, SKIE is the right choice now, with the
analytics opt-out in the first configuration and a test of the flow exception path.

**Cost per screen under the recommendation**, from this screen: a `nativeMain` facade of 141 lines (92 non-blank) and a
public state file of 29, one export line, and on the Swift side about 112 lines of owner, mappers and mirror types next
to the view, with one cast per state, two forced `default` branches and four manual lifetime pieces (owner created on
appear, weak capture, `assumeIsolated`, cancel and `close()` in `deinit`). The design's estimate of about 30 lines of
mapping per screen (design E) was low: the facade also carries the subscription, the lifetime rules and the date
conversion.

**What would change the call:**

- The owner deciding that compile-time exhaustiveness is part of iOS parity: SKIE now.
- Swift export reaching a stable or Beta level with documented coexistence with Objective-C export: Swift export, and
  no SKIE.
- A second screen whose state has more cases, nested sealed types or events as sealed types: the casts and `default`
  branches grow per case, which strengthens SKIE.
- Public state types in `commonMain` reading well under one bridge (design E): under SKIE the Kotlin sealed state could
  be consumed directly, which might remove the per-screen facade mapping and change the per-screen cost more than
  anything measured here. Not tried; the mirror types were kept under every candidate on purpose.
- A SKIE release lagging a Kotlin upgrade the repo needs: against SKIE.

**What this comparison cannot say.** One screen, one sealed state with four cases and one five-case enum, no events as
types. Timings within noise. Real-data content and the member switch were not verifiable on any candidate; the content
mapping was checked with five Kotlin items built in Swift (5 of 5 rows on all three). Main-thread delivery for
KMP-NativeCoroutines and SKIE rests on the Swift `Task` inheriting the main actor, which held here. To choose with
confidence: the same comparison on a second screen with events and a larger state; SKIE's release lag over its last
few Kotlin versions against the repo's upgrade cadence (not measured); a probe of SKIE's flow exception path; and the
Swift export status at the next Kotlin release.

## 3. Step 4 report

### 3.1 What was built, by design step

| Design step | Task (merged at) | What it did | What it proved |
|---|---|---|---|
| 0 | 2 (`6873c777c5`) | `Language.label` moved next to its two UI callers; `language-core` `commonMain` no longer depends on `core-resources` | `components-resources` on the iOS classpath of `network-clients`: 2 to 0 (`task-2-report.md`) |
| 0 | 3 (`7f953d2bab`) | `AuthTokenStorage` became an interface; DataStore token storage, demo manager, token provider and logout use case moved to `jvmMain` | Android graph differs only in the `AuthTokenStorage` provider (6 lines); persisted tokens read after an upgrade in place on the Pixel |
| 0 | 4 (`17c3b5010d`) | Opt-in `sharedWithIos()` guard against Compose UI imports and dependencies in `commonMain` | On `feature-help-center` as a negative check it flagged 389 imports and 5 dependencies; no module's graph changed |
| 1 | 5 (`b1683bc248`) | `feature-claim-history` split by source set, iOS targets, partner flag | Both iOS compiles green; the KSP metadata pass works with UI in `jvmAndAndroidMain` and no convention change |
| 1 | 6 (`56fbfe3c09`) | Public facade in `nativeMain`: `ClaimHistoryScreenModel`, sealed `ClaimHistoryScreenState`, `NSDate?` dates | Android and JVM compile tasks came from cache: nothing they see changed |
| 2 | 7 (`8db4df9648`) | Native defaults: no logout interceptor, no Kotlin Datadog plugin, `Hedvig-TimeZone`, iOS user agent and client name | Android and JVM headers and interceptor chain unchanged (tests); native header tests |
| 2 | 8 (`f1916a1362`) | `apollo-auth-listeners` builds for iOS | Listener still in Android's `Set<AuthEventListener>`; graph identical |
| 2 | 9 (`cec22c80c7`) | Native Metro graph in `:umbrella`, five Swift inputs, per-platform Apollo provider | The framework links with the ViewModel reachable; Android graph differs by the Apollo provider (3 lines per graph) |
| 2 | 9b (`caf1afd086`, not in the plan) | A `RumLogger` installed on native | A GraphQL error no longer throws out of the client (finding F1) |
| 3 | 10 (`1887f845dc`) | iOS simulator tests for the facade and the graph | 28 tests, then 30 after Task 9b; only Loading is observable in a test process |
| 3 | 11 (`3afe3fbfdd`) | The shell: BankID QR login through `authlib`, Keychain session, token refresh | Login, relaunch keeps the session, logout deletes the Keychain item and clears the Kotlin cache |
| 3 | 12 (`f446fbfaac`) | Claim history in the shell | Loading, Empty, Failed with Retry; leaving clears the ViewModel; header parity; shared refresh; logout during refresh |
| 4 | 13, 14, 15 (pushed, not merged) | Three bridge candidates and device measurements | Section 2 |

Goals 1 to 7 of the design, as of step 4:

| Goal | Result | How it was checked |
|---|---|---|
| 1. Shell shows loading, error with retry, empty, content | Loading, Empty and Failed shown. Retry re-runs the request (it failed again under the forced token failure); recovery was observed only after a relaunch. Content from real data not shown; Kotlin-built items rendered through each bridge | Simulator log and screenshots, `task-12-report.md` a and e; sample probe in `bridge-comparison.md` |
| 2. Same in Ugglan | Step 5 not started | |
| 3. Leaving stops the work | Holds: `ClaimHistoryScreenOwner deinit` and `ClaimHistoryViewModel cleared` in the same millisecond, no ClaimsHistory request in the next 10 to 11 s, 3 cycles per candidate | Log lines, `task-12-report.md` b, `bridge-comparison.md` |
| 4. Login and logout keep both clients consistent | Kotlin sends the token Swift holds (Swift's fetcher). Member A then B not verifiable; cache clearing on logout pinned by a native test | `parity.md` Authorization row; `IosGraphTest` "sessionChanged false clears the Apollo store" |
| 5. Android graph and behaviour unchanged | Graph: two deliberate differences, nothing else. Behaviour on a device checked once, after Task 3 | Section 3.3 |
| 6. Size recorded after every step | Yes | Section 3.2 |
| 7. Every skip listed | Section 3.5 | |

### 3.2 Size

Shipped bytes of the `iosArm64` release framework (`__TEXT` plus non-zerofill `__DATA`, as `umbrella.yml` computes
them). Rows from `size-log.md` unless the source column says otherwise.

| Step | Commit | Shipped bytes | Delta vs previous | Running vs baseline | kfun | UI symbols | Source |
|---|---|---|---|---|---|---|---|
| baseline | `99815f5a70` | 3,593,170 | | 0 | 6,059 | 0 | `size-log.md` |
| step 0 (Tasks 2 to 4) | `b1683bc248` | 3,593,170 | 0 | 0 | 6,059 | 0 | `size-log.md` |
| step 1 (Task 5; also Tasks 7, 8) | `b1683bc248` | 3,593,170 | 0 | 0 | 6,059 | 0 | `size-log.md`, the same link as step 0 |
| step 2 (Task 9) | `4152719dde` | 7,950,696 | +4,357,526 | +4,357,526 | 13,917 | 0 | `size-log.md`; **callout, over 3 MB in one step** |
| Task 9b, includes Task 9's fix round | `35a377e29a` | 7,953,504 | +2,808 | +4,360,334 | 13,919 | 0 | `task-9b-report.md`; not added to `size-log.md` |
| step 3 (Tasks 10 to 12) | `f446fbfaac` | 7,953,504 | 0 | +4,360,334 | | | ledger: no Kotlin production change since Task 9b; not re-linked |
| step 4, candidate 1 (with probes) | `ios/bridge-plain` | 7,954,935 | +1,431 | +4,361,765 | 13,923 | 0 | `bridge-comparison.md` |
| step 4, candidate 2 (with probes) | `ios/bridge-nativecoroutines` | 7,961,741 | +8,237 | +4,368,571 | 13,934 | 0 | `bridge-comparison.md` |
| step 4, candidate 3 (with probes) | `ios/bridge-skie` | 8,353,289 | +399,785 | +4,760,119 | 14,262 | 0 | `bridge-comparison.md` |

Steps 0 and 1 cost nothing because `:umbrella` did not link the new modules until Task 9. The 4.36 MB of step 2,
approximate `__text` growth by package from `nm` address gaps (`size-log.md`, `task-9-report.md`):

| Package | Growth |
|---|---|
| Compose runtime (Molecule) | +756 KB |
| Apollo runtime | +406 KB |
| kotlinx-datetime | +357 KB |
| Apollo normalized cache | +257 KB |
| Okio | +257 KB |
| DataStore core and preferences, kotlinx-serialization-protobuf (dead code, see 3.8) | +172 KB |
| generated `octopus` code | +163 KB |
| Hedvig code | +143 KB |
| coroutines flow | +79 KB |
| androidx.collection | +55 KB |

These rows add up to about 2.6 MB of `__text`; the sources do not break down the rest. The auth arrangement chosen in
Task 9 is worth about 50 KB against the plan's (7,946,780 against 7,997,209 on the scratch build,
`task-9-proposal.md`). The header grew from 781 to 6,006 lines (`task-9-report.md`). For comparison, the design
recorded about 1.3 MB for a Molecule ViewModel and about 2.9 MB for the network clients measured separately.

### 3.3 Android and desktop impact

Every compiled-graph difference introduced, from `javap` of the release graph classes against the parent (P2):

| Task | `AppGraph$Impl` | `ActivityRetainedGraphImpl` | `DesktopGraph$Impl` |
|---|---|---|---|
| 3 | 6 lines, all the `AuthTokenStorage` key: the field renamed `providesDataStoreAuthTokenStorageProvider` (declaration, one `putfield`, two `getfield`), and the factory `AuthTokenStorage$MetroFactory` replaced by `DataStoreAuthTokenStorageContributions$ToAppScope$ProvidesDataStoreAuthTokenStorageMetroFactory`, same two inputs and offsets | empty | not compared |
| 9 | 3 lines: the class also implements `ApolloClientMetroProviders`; one `checkcast` and the factory class change from `NetworkMetroProviders$ProvideApolloClientMetroFactory` to `ApolloClientMetroProviders$ProvideApolloClientMetroFactory`, same five inputs and offsets | empty | the same 3 lines |
| 2, 4, 5, 7, 8 | none | none | not compared |
| 6, 9b, 10, 11, 12 | no Android-compiled production change (nativeMain, nativeTest or Swift only) | | |

Sources: `task-3-report.md`, `task-9-report.md`, `integration-b1683bc248-report.md` (which confirmed the six Task 3 lines
as the only difference from `99815f5a70` at that tip), the other task reports.

Counts and builds, every task: 87 ViewModels (48, 37, 2), 21 deep link matcher providers, 111 registered keys, each set
identical to its base. JVM and Android tests 1197 at the baseline and 1199 from Task 5 on (two new
`GetClaimsHistoryUseCaseTest` cases), 0 failures. Build-logic tests 8 to 10 (Task 4). `checkCommonMainImports
:app:assembleDebug :app-desktop:compileKotlinJvm :app-desktop:jvmTest` and `test ktlintCheck` green on every task that
changed Android-compiled code; `DesktopGraphTest` 4 of 4 after Task 9, including logout on an unauthenticated response.

Minified staging builds (`minifyStagingWithR8` executed, not from cache, except at the baseline): APK defined classes
21,642 with an identical class list at every check. `mapping.txt` classes 24,633 at the baseline, 24,638 after Task 9.
Every added entry is either an R8-removed class from a moved or new file (`LanguageLabelKt` from Task 2, Task 7's file
facade rename, Task 9's Apollo provider classes) or a swap in which of a group of same-shaped Metro factories R8 keeps
as the merged representative (Tasks 3 and 9). The swap reading is inferred; no second base build ruled out run-to-run
variance.

On a device: once. After Task 3 the orchestrator installed the debug build over a logged-in session on the Pixel,
cold-started to Home, force-stopped and relaunched to Home, with no crash, which exercised reading the persisted
tokens through the new binding (ledger). A login and logout through the new storage was not done. Task 7 (the
Datadog Ktor plugin moved to `androidMain`, same bytecode) and Task 9 (the Apollo provider) changed Android-compiled
code after that and were not run on a device, nor was any minified build. The desktop app was not opened on claim
history (the optional run in Task 5 was skipped).

### 3.4 Network client parity

`parity.md`, verified in Task 12 on the simulator by a DEBUG header logger, 4 requests with identical headers
(`task-12/d-headers.txt`).

| Concern | Position | Verified |
|---|---|---|
| `Accept-Language`, `hedvig-language` | Match iOS through Swift's `LanguageStorage` | Observed: both `en-SE`. Re-read per request not exercised |
| `User-Agent` | Match iOS | Observed: `com.hedvig.umbrella-consumer 1.0 (iOS 27.2)`, the iOS format |
| `Hedvig-App-Version` | Matches with `platformName = "ios"` | Observed: `ios;1.0` |
| `hedvig-device-id` | Match iOS through Swift's `DeviceIdFetcher` | **Observed not sent**, 0 of 4 requests (finding F3) |
| `Hedvig-TimeZone` | Add on native | Observed: `Europe/Stockholm` |
| `X-Build-Version`, `X-App-Version`, `X-System-Version`, `X-Platform`, `X-Model` | Keep Kotlin's | Observed: all five |
| `apollographql-client-name`, `-version` | Match iOS | Observed: `iOS:com.hedvig.umbrella-consumer`, `1.0`. Header names read from apollo-ios 2.4.0 source |
| `Authorization` | Swift's fetcher | Observed: `Bearer`, 510-character JWT-shaped value; a throwing fetcher sends nothing |
| Timeouts | Keep Kotlin's | Not exercised |
| Cache | Kotlin's, cleared on Swift's login and logout | Observed: reload in the same process answered from cache; logout dispatches to the cache listener. Member A then B not exercised |
| Token refresh | Stays in Swift | Observed: one refresh for two overlapping requests, the surviving request sent with the new token |
| Logout on `UNAUTHENTICATED` | Off on native (owner) | Read and tested only: no server `UNAUTHENTICATED` on a device; pinned by `NativeApolloClientTest` and `IosGraphTest` "an UNAUTHENTICATED response does not log out" |
| Datadog tracing | None from Kotlin on native (ruling, pending owner objection) | Observed: no `x-datadog-*` header. The shell links no Datadog, so this shows only that Kotlin adds none |

Rows resting on reading rather than observation: timeouts, language re-read, the client header names, logout on
`UNAUTHENTICATED`, and everything about Ugglan's Datadog SDK picking up Kotlin requests (`research-ugglan-auth-and-datadog.md`
read dd-sdk-ios 3.17.0 source: every `URLSessionTask` in the process is tracked automatically, so Kotlin's Darwin
requests should appear in Ugglan's RUM with trace headers and no Kotlin work; never observed).

The device id gap is the one that matters: with it the backend cannot tell a Kotlin request from the same device as
Ugglan's. It is not an iOS problem; see F3 in 3.7.

### 3.5 Skips and reductions

**Nothing was skipped under the experiment-only skip rule (owner decision 12).** No piece was left out because sharing
it would have made Android's `commonMain` much worse; the ledger's skip column is empty for every task. These were
reduced for other reasons:

| What | Why | What completing it would take |
|---|---|---|
| iOS simulator tests cover only the Loading state; loading to content, to failed, empty, reload and "no callback after close" with a second state were moved to the manual run | The display link does not tick in a Kotlin/Native test process, so the presenter composes once and never recomposes (`task-10-report.md`) | A frame clock that ticks in a test process; that is a change to `molecule-public` this work did not make. The manual run covered all of them except content and a reload that reaches the network |
| Content rows from real data, the Partner label from real data | The only BankID identity has no claims, and a claim takes 30 days to reach history (owner, ledger). Replaced by a DEBUG probe with five Kotlin-built items through each bridge | A staging member with claim history, including partner claims |
| Member A then member B | One BankID identity | A second staging identity |
| A server GraphQL error and `UNAUTHENTICATED` on a device | Needs the owner or a backend toggle | Same; both paths are pinned by native tests on a mock engine |
| Retry recovering inside one process | The forced token failure is a launch argument that cannot be turned off in the process; the network toggle was not used | One run with the network switched off and on |
| Owner login and logout through Android's new token storage | Judged unnecessary after the upgrade-in-place check (ledger, Task 3) | One login and logout on the Pixel |
| Task 7 did not run P3 or P5; Task 6 ran no P2 or P3 | Nothing Android-compiled or linked changed in a way they would see | Covered later: P3 at the `b1683bc248` integration proof, P5 at Task 9 |
| The shell's demo mode switch (design B, "if cheap") | Not built; `IosDemoMode` is fixed to false | A toggle in the shell |
| The SKIE flow exception path | Not probed | A probe that throws inside a collected flow |
| Desktop run of claim history | Optional in Task 5, skipped | `./gradlew :app-desktop:run` |

### 3.6 Deviations from the design and plan

| Deviation | Reason |
|---|---|
| The plan's own five, accepted before execution: `AuthTokenServiceImpl` stays in `commonMain`; the smoke test is split between `feature-claim-history` and `:umbrella` (design K said one module); the `android/` clone needs a new branch, not the integration branch; the Ugglan work needs a branch in `ugglan/` itself; Task 7 before Task 9 | The design text was unbuildable on those points (ledger, pre-flight scan) |
| Task 3: `auth-core-public` keeps its DataStore dependencies in `commonMain` | `BankIdSandboxState` is a `commonMain` `@Inject` class that takes `DataStore<Preferences>` |
| Task 9, option C: the Apollo client is provided per platform, and native reaches no token storage, `AuthRepository`, `AuthTokenService` or `MemberIdService`. Dropped from the plan: `IosTokenStore`, `NativeAuthTokenStorage`, `IosMetroProviders`, the `AuthMetroProviders` move. `logsOutOnUnauthenticated` and its three actuals deleted. `sessionChanged` takes a Boolean, not the tokens | Nothing in the claim history slice reads tokens; the shared provider reached auth only for the logout interceptor, which native does not install. One refresh path, owned by Swift; no second Keychain reader in Kotlin. The Boolean replaced the tokens after review found them printed to the iOS log (3.7). Cost: Kotlin cannot read tokens on iOS (3.8) |
| The per-platform Apollo provider reverses part of the desktop work's arrangement, in which the client and its interceptor chain were built once in `network-clients` `commonMain` for every graph | Native must not install the logout interceptor; the shared parts stay in one internal builder, and `ApolloInterceptorsTest` still pins the Android and JVM chain from the provider's own list |
| Task 9b, a native `RumLogger`, not in the plan | Finding F1: without it any GraphQL error would crash the iOS app |
| The shell is `micro-apps/umbrella-consumer` evolved in place, not a new `app/app-ios/`: name, location and bundle id kept; committed token and hardcoded `JAVA_HOME` removed; dd-sdk-ios replaced by `KMPNativeCoroutinesAsync` 1.0.5 for login; iOS 17.0, Swift 6 | Owner answer 3 (keep it); evolving loses nothing and its synchronized source folder means new Swift files never touch the project file. `authlib` exports only KMP-NativeCoroutines forms, so login needs the package |
| Xcode 27.2 instead of 26.4.1; simulator iPhone 18 Pro, iOS 27.2 | Xcode updated mid-session; iPhone 17 Pro exists only on the 26.5 runtime now |
| Task 10 reduced; Task 12 content via a sample probe | 3.5 |

### 3.7 Findings in existing code

**Pre-existing on Android or all platforms, not fixed here:**

- **F3, no `hedvig-device-id` on authenticated requests.** `provideHttpClient` builds the authenticated client with
  `baseHttpClient.config {}`, which drops the `DeviceIdInterceptor` installed on the base client. Verified on the Pixel
  in Chucker (the last 8 apollo-router requests have `Hedvig-App-Version` and `Authorization`, no device id) and on iOS
  (0 of 4). The same code is on `develop`, there since `b93c73e300` (2026-01-09, the OkHttp to Ktor migration), so
  production Android has likely sent no device id since then; production not checked. Proposed as a fix on `develop`.
- **F2, `Authorization: Bearer null`.** `addAuthPlugin` calls the token fetcher twice per request; a token on the
  first read and null on the second sends exactly `Bearer null` (pinned by a native test).
- **F5, reload served from cache.** `reload()` from a loaded state is answered by the normalized cache (the use case's
  default CacheFirst) and never refetches, on Android too. A pull to refresh built on it would not reach the network.
  Related, F4: after the presenter's 5 s stop, a new observer is also served from the cache.
- **The auth event log line prints tokens in debug logcat.** `AuthEventBroadcaster` logs the whole `LoggedIn` event at
  VERBOSE. On Android release builds nothing ships it (the Firebase tree takes DEBUG and above, Datadog above DEBUG), so
  it is debug logcat only; on iOS it now prints empty strings.
- **A throwing `AuthEventListener` ends the broadcaster's collection**, and on native an uncaught exception in
  `MainScope` terminates the process.
- A platform difference, not introduced here: a refresh that fails with an unknown error logs the member out on iOS
  and keeps the tokens on Android (`research-ugglan-auth-and-datadog.md`).

**Found and fixed in this work:**

- **F1:** no `RumLogger` was installed on iOS, and `LoggingInterceptor` calls it on any non-`UNAUTHENTICATED` GraphQL
  error; the default throws, which in `viewModelScope` would have crashed the app. Fixed in Task 9b with a native logger
  that logs attribute keys only.
- **Tokens in the iOS system log:** `sessionChanged(tokens)` sent a `LoggedIn` event with the real tokens, the
  broadcaster logged it, and the iOS logger writes every priority to `NSLog`. Fixed in Task 9's review round: Swift
  passes a Boolean and Kotlin sends empty strings. Secret scans of every later simulator log found no token.
- **The Datadog KMP plugin on native** kept the `network-clients` iOS test binary from linking at all
  (`framework 'DatadogCore' not found`). Moved to `androidMain` in Task 7.
- **An expired impersonation JWT committed in `micro-apps/umbrella-consumer`** (`IosInterfaces.swift:23`) is removed from
  the tip. It stays in history (`5883e22c80`, `a36a96f4a8`); rewriting history is the owner's call.

### 3.8 Shipping gaps

What must be true before any of this ships to the real iOS app.

| Gap | What it would take |
|---|---|
| GraphQL errors on iOS reach only `NSLog`, not the iOS app's Datadog RUM | A Swift-supplied `RumLogger`, exported through the umbrella and installed before `createIosGraphHolder`, which already keeps an earlier install |
| The iOS logger writes every priority to the system log; `LoggingInterceptor`'s ERROR line and its DEBUG `Response data` line include response data, and the Ktor logger prints bodies on non-production builds | Priority filtering in the iOS logger for release builds, or redaction of those lines in common code (which also changes Android's logs) |
| `isolated deinit` in the Swift owner was verified on iOS 27.2 only; the shell and Ugglan target iOS 17.0 | Check its availability on older iOS, or a non-isolated `deinit` that hops to the main actor, or teardown from the view's `onDisappear` |
| No duration breakdown for Kotlin requests in the iOS app's Datadog, and no check that each Kotlin request appears once with a trace id | Register Ktor's session delegate class for the breakdown (needs the class visible to Swift, unverified), or accept the gap as `authlib` does today; one staging RUM check |
| Apollo's `ClaimsHistoryQuery` family, Metro's `ClaimHistoryViewModelModule` and prefixed Apollo, Okio and datetime types are in the framework header (about 345 lines) | Internal Apollo codegen for the module, which touches Android-compiled code; the sources propose no fix for the Metro module |
| About 172 KB of dead DataStore code linked because the graph implements every contributed provider interface | `NativeDataStoreMetroProviders` as a `@BindingContainer` object, or `DeviceIdFetcher` moved out of `core-datastore-public`; each with its own graph check |
| Kotlin cannot read tokens on iOS; a later screen that injects `AuthTokenService` or `MemberIdService` fails to compile | A Swift token storage input and a native `AuthRepository` provider (the plan's option A, measured at about 50 KB) |
| Reload never refetches (F5); the presenter keeps the old state during a reload | A network-first reload in the use case, which changes Android too |
| No `hedvig-device-id` (F3), `Bearer null` (F2) | Fixes on `develop` |
| Two GraphQL caches: a Swift mutation does not update Kotlin's cached data, and the reverse | Per feature: refetch policy or explicit invalidation; not addressed |
| The shell's Release configuration requests x86_64 and fails in the Gradle phase | `ONLY_ACTIVE_ARCH = YES` for Release, or an x86_64 simulator target. The shell is a test host; Release with `ARCHS=arm64` built with 0 warnings |
| Swift sees prefixed names (`Network_clientsAccessTokenFetcher`) | `typealias`es in the host; `@ObjCName` would be a Swift-facing annotation in shared code |
| KMP-NativeCoroutines 1.0.6 targets Kotlin 2.4.20; the repo is on 2.4.21 and 1.0.7 is the matching release | Bump; it builds and works on 1.0.6 today |

### 3.9 What was not verified

- Content rows from real data, the Partner label from real data, `Identifiable` diffing with real items.
- A member switch (A then B never shows A's claims) on a device; only the cache clear is tested.
- A server GraphQL error or `UNAUTHENTICATED` on a device.
- Retry recovering inside one process; recovery was seen after a relaunch.
- Datadog's header rewrite on Ktor-created requests, and Kotlin requests appearing once in Ugglan's RUM.
- Any iOS version other than 27.2, and any device other than the simulator.
- Any production configuration: the shell runs staging only.
- Android on a device after Task 3, any minified build on a device, a login and logout through the new storage.
- Timeouts and language re-read parity.
- The SKIE flow exception path; candidate 2's declared exception domain (only the Swift type was logged).
- R8's representative swaps in Tasks 3 and 9 as the cause of the mapping differences.
- In the shell: a Keychain write failure, an exchange failure after a scan, the login polling pause off-foreground, two
  overlapping requests that both complete after one refresh.
- The background case beyond what happened: the response arrived during the background transition, before suspension;
  a response arriving while suspended was not seen.

### 3.10 Later work, already decided

Navigation out of presenters, piloted on `feature-payments`; the 44 leak files in 24 modules beyond claim history;
presenters holding `TextFieldState`, last (owner decision 4); policy effects out of `HedvigApp`; the other 25 features.

### 3.11 Step 5: not started

What the gate (plan Task 17) needs from the owner:

- **The `android/` clone.** It is on `develop` today. The Ugglan loop builds only from `android/`; the go-ahead is for
  `git -C android switch -c experiment/ugglan-local-umbrella origin/feat/desktop-app-integration`.
- **The `ugglan/` clone and base.** It is on `fix/firebase-user-id-after-consent` at `0431fb12d`. The go-ahead is for a
  local branch `experiment/kmp-claim-history`, never pushed, and its base. Ugglan master needs Xcode 27.2, which is now
  installed, so `origin/master` is the plan's option (not yet built here); the alternative is `ff89c395e` with the local
  `ToolbarButtonView` patch. Xcode must be closed
  before `use-local-umbrella.sh` runs.
- **The bridge for Ugglan.** Plain export unless the owner chooses otherwise after section 2.
- **Already answered** (`owner-answers.md`): Kotlin logout on `UNAUTHENTICATED` stays off on iOS, as Ugglan never
  reacts to it; no Kotlin-side Datadog on iOS, since dd-sdk-ios 3.17.0 tracks Ktor's requests automatically (a ruling
  pending the owner's objection); `micro-apps/umbrella-consumer` is kept, and is now the shell.

Constraints recorded for step 5:

- `sessionChanged` sits next to the login exchange in `AuthenticationClientAuthLib.exchange(code:)` and next to
  `ApolloClient.deleteToken()` in `AppDelegate.logout()`, not next to `saveToken`, which also runs on every refresh.
- The plan's Task 19 still names `IosTokenStore` over `KeychainHelper`; under option C there is no token store input.
  `setDemoMode(false)` arrives on the main thread after a logout.
- Do not copy `isolated deinit` without checking Ugglan's deployment target (3.8).
- The `AppBuildConfig` values Kotlin derives for Ugglan's production configuration (`isProduction`, the Octopus URL)
  must be checked; nothing here ran production.
- `apollographql-client-version` matches iOS only if Swift supplies the short version as `versionName` (Task 7 report).
- Ugglan pairs the KMP-NativeCoroutines Swift package 1.0.5 with Kotlin 1.0.6; the shell builds and runs with that
  pair.
- Never commit a project generation made in local mode, the local-umbrella marker or the `ToolbarButtonView` patch.
- A staging RUM check that each Kotlin request appears once, with a trace id (owner answer 2).
- Every simulator build that runs is signed, and `xcodebuild test` runs with `-parallel-testing-enabled NO`.
