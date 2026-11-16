# Sharing presenters and data with iOS: experiment report, final edition

Date: 2026-11-16. State of `feat/desktop-app-integration` at `acdedcaf45`, of the three bridge branches
`ios/bridge-plain` (`0e30384173`), `ios/bridge-nativecoroutines` (`a48caaae8d`) and `ios/bridge-skie` (`eb11dbef4c`),
which stay pushed and unmerged as the record of the comparison, and of the local Ugglan branch
`experiment/kmp-claim-history` (`275e8fdc7`, `74d00e71a`). The design is
`docs/superpowers/specs/2026-11-13-ios-sharing-design.md`, the plan `docs/superpowers/plans/2026-11-14-ios-sharing.md`.
Every number below comes from the run directory `.superpowers/sdd/2026-11-14-ios-sharing/` of the `android/` clone;
the file is named next to it. "Ledger" means `ledger.md` there.

## 1. Summary

**What ran.** The experiment ran to the end. The claim history screen's ViewModel, presenter, use case and GraphQL
client run in Kotlin and feed SwiftUI: first in the iOS test host in this repo (`micro-apps/umbrella-consumer`), then
in the real iOS app, Ugglan, on a local branch built against a local umbrella. Swift keeps the tokens in the Keychain
and owns the one refresh path.

**Proven in both, on the iOS 27.2 simulator against staging:** login; Loading, Empty, and Failed with a retry; leaving
the screen clears the ViewModel and no request follows; logout clears the Kotlin cache; request headers match the iOS
app's except `hedvig-device-id`. In Ugglan the Kotlin request also carries the app's Datadog trace headers on the
wire. Android's compiled graph has two deliberate differences and nothing else; 1199 JVM and Android tests and 43 iOS
simulator tests pass.

**Bridge.** KMP-NativeCoroutines behind one shared Kotlin host and one shared Swift owner, chosen over SKIE because
SKIE gates Kotlin upgrades.

**Size.** The shared framework went from 3.59 MB to 7.96 MB shipped, almost all of it when the data layer was first
linked.

**Do not miss:**

- Content rows from real data and a member switch could not be verified: the only staging identity has no claim
  history.
- The Ugglan screen would change what members see: a spinner and an error state, history cached for the life of the
  process instead of refetched per visit, and a detail screen that loads by id (section 5).
- What a real adoption needs, most blocking first, is in section 12.

Nothing is shipped and no PR is open. The Ugglan branch is local, never pushed, and not for merge.

## 2. Bridge decision

### Measured comparison

From `bridge-comparison.md`, "Measured comparison" (device run of 2026-11-16, iPhone 18 Pro simulator, iOS 27.2, one
logged-in member with an empty history; evidence in `task-13/`, `task-14/`, `task-15/`). This is the record of what
was measured; the screen pattern built after the decision is described further down.

| Criterion | 1: plain Objective-C export | 2: KMP-NativeCoroutines | 3: SKIE 0.10.16 |
|---|---|---|---|
| Sealed state and outcome enum in Swift | 4 casts, 2 `default` branches the compiler requires; a missing case compiles silently | same as 1: delivery changes, the element type does not | `onEnum(of:)` and real Swift enums; no `default`; a missing case is a compile error |
| Lifetime (owner deinit, ViewModel cleared, owner released about 1 s later, no request after) | holds, 3 of 3 cycles | holds, 3 of 3 | holds, 3 of 3 |
| Swift main-thread precondition (see the correction below) | no trap, 6 deliveries | no trap, 6 | no trap, 6 |
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

**Correction to the main-thread row.** The Swift `dispatchPrecondition` sat in a function isolated to the main actor,
so Swift had already put it on the main thread whatever thread Kotlin called from. It never tested Kotlin's delivery
thread: the device runs show Swift's isolation and nothing more. Kotlin's main-thread delivery is pinned by the host's
native test (`ScreenModelTest`, "states delivers the mapped state on the main thread and starts the presenter"). The
precondition was deleted in Task 16b (ledger, "Corrected claim"; `task-16b-report.md`, fix round 1, M1).

### What was weighed

- **The candidates differ on one criterion.** Lifetime, the exception behaviour, the token failure path and build time
  came out the same. Only SKIE gives compiler-enforced exhaustiveness: under the other two a new Kotlin state falls
  into a `default` and shows Failed with an error log, and a new outcome silently shows "Unknown".
- **SKIE's Swift code is the best of the three.** No casts, no `default`, no annotations in shared code, and the
  outcome mirror becomes deletable.
- **SKIE's costs.** Every Kotlin upgrade waits for a SKIE release that lists it (an unsupported Kotlin version is an
  error at install time, `task-15-availability.md`). It changes the exported API of every exported module:
  Swift-implemented suspend requirements are renamed (`fetch` became `__fetch` in two shell inputs), Kotlin enums cross
  through `_ObjectiveCBridgeable`, an underscored Swift protocol, and an exception inside a collected flow ends in
  `fatalError` per the candidate's report. It adds about 400 KB and a generated surface larger than the rest of the
  header. It sends build analytics to its vendor by default: the first link on `ios/bridge-skie` ran the upload before
  the opt-out was added, with a payload, as collected locally, of a hashed project id, git contributor, commit, branch
  and tag counts, the Mac model and memory, Kotlin, Gradle, JVM and macOS versions and the compiler configuration, and
  no project name (ledger, "DISCLOSURE"). Every build since reports the upload task SKIPPED.
- **KMP-NativeCoroutines as measured** changed delivery and not the element type: the casts and both `default`
  branches stayed, `close()` stayed manual, and its gains were a typed value and no `assumeIsolated`.
- **Kotlin's own Swift export** is Alpha since Kotlin 2.4.0: suspend as `async`, `Flow` as an `AsyncSequence`, sealed
  hierarchies as Swift enums; no generics, final classes only, it replaces `embedAndSignAppleFrameworkForXcode`, and
  coexistence with the Objective-C export that login uses is undocumented (`task-15-availability.md`). The thing to
  watch.

Before the decision, this report recommended plain export for step 5 and a choice between SKIE and Swift export before
a second feature was converted.

### The decision

The owner decided on 2026-11-16 (ledger, "BRIDGE DECISION"), in this order.

**SKIE has the best Swift code, and it is rejected because it gates Kotlin upgrades.** The evidence is this repo's own
history and the last two Kotlin releases:

- This repo used SKIE from July to November 2025 (`728453015a`, "Add skie to authlib", 6 July 2025; `47f46e3fec`,
  "Revert to skie that works for Kotlin 2.1.21", the next day) and removed it in `a530bbe9e8` ("Temporarily remove
  SKIE", 5 November 2025), whose message says it was removed to use Kotlin 2.2.21, which supports Xcode 26, while
  "Skie is locked into using Kotlin 2.2.10". KMP-NativeCoroutines replaced it the next day (`3a019074d4`).
- Kotlin 2.4.20 was released on 7 September 2026 and merged to `develop` on 17 September (`2e17ee9b5d`); SKIE
  supported it on 25 September (0.10.15). Kotlin 2.4.21 was released on 8 October and merged to `develop` on
  14 October (`477a1491bf`); SKIE supported it on 10 November (0.10.16; its Maven metadata says 29 October). Release
  and SKIE dates are from the GitHub release pages as recorded in the ledger; merge dates are from `git log` on
  `develop`. With SKIE in the build those upgrades would have waited 8 days and 27 days (15 by the Maven date).
- The exit cost grows with every shared screen whose Swift is written against SKIE's shapes (`onEnum(of:)`, its Swift
  enums, the renamed suspend requirements).

**The bridge is KMP-NativeCoroutines, "made to feel as native as possible".** The orchestrator's ruling on its shape:
the bridge is a detail of one shared Kotlin host and one shared Swift owner, so no screen depends on it and an exit
touches no screen file. Built as Task 16b and merged at `acdedcaf45`, it replaces the facade as the screen pattern.

### The screen pattern as built (Task 16b)

**One Kotlin host.** `ScreenModel<State : Any>`, an abstract class in
`app/molecule/molecule-public/src/nativeMain/kotlin/com/hedvig/android/molecule/public/ScreenModel.kt`, exported to
Swift as `ScreenModel` through `@ObjCName(name = "HedvigSharedScreenModel", swiftName = "ScreenModel", exact = true)`.
It owns the ViewModel store and a `SupervisorJob() + Dispatchers.Main` scope, exposes `currentState`, `states` and
`close()`, and gives subclasses a hidden `viewModel(factory, class, toState)` and `send(event)`. State reaches Swift
through one call, the library's runtime function `asNativeFlow(scope)` from `kmp-nativecoroutines-core`, an
`implementation` dependency of `molecule-public`'s `nativeMain` only. No compiler plugin and no annotation is in the
screens' path and feature modules cannot see the bridge, so screens do not depend on it and it is not in the Kotlin
upgrade path for screens (login keeps its own use of the plugin in `authlib`). The element type survives the
Objective-C generic: the header declares `HedvigSharedScreenModel<State>` and
`HedvigSharedClaimHistoryScreenModel : HedvigSharedScreenModel<HedvigSharedClaimHistoryScreenState *>`
(`task-16b-report.md`).

**One Swift owner.** `Kotlin/ScreenOwner.swift` in the shell: a `@MainActor @Observable` generic
`ScreenOwner<Model, ViewState>` that seeds from `currentState`, collects `states` with `for try await` in a task that
captures it weakly, keeps a mapped `Equatable` Swift view state and skips equal states, and in `isolated deinit`
cancels the task and calls `close()`. The same file holds `oneOf`, which turns a `handle` call into a value.

**Exhaustiveness without a library.** Every Swift-facing sealed state and enum gets a Kotlin `handle` function with
one closure per case, written with an exhaustive `when`. A new case fails Kotlin first (`'when' expression must be
exhaustive`) and then every Swift call site (`missing argument for parameter 'onArchived' in call`); both errors came
from a probe that added a fifth state and a sixth outcome (`task-16b-proposal.md`, D). A value-returning generic `fold`
is erased to `id` by the Objective-C export and is not usable.

**Per screen, Kotlin** (`ClaimHistoryScreenModel.kt`; the mapping functions below it are unchanged):

```kotlin
@HiddenFromObjC
fun createClaimHistoryScreenModel(viewModelFactory: ViewModelProvider.Factory): ClaimHistoryScreenModel =
  ClaimHistoryScreenModel(viewModelFactory)

/** The claim history screen. The Swift owner's rules are on [ScreenModel]. */
class ClaimHistoryScreenModel internal constructor(viewModelFactory: ViewModelProvider.Factory) :
  ScreenModel<ClaimHistoryScreenState>() {
    private val viewModel = viewModel(viewModelFactory, ClaimHistoryViewModel::class) { it.toScreenState() }

    /** Loads the history again. Safe to call from any thread. */
    fun reload() {
      viewModel.send(ClaimHistoryEvent.Reload)
    }
  }
```

and on the state (`ClaimHistoryScreenState.kt`; the outcome enum has the same shape with five closures):

```kotlin
  fun handle(onLoading: () -> Unit, onEmpty: () -> Unit, onFailed: () -> Unit, onContent: (Content) -> Unit) {
    when (this) {
      Loading -> onLoading()
      Empty -> onEmpty()
      Failed -> onFailed()
      is Content -> onContent(this)
    }
  }
```

**Per screen, Swift** (`ClaimHistory/ClaimHistoryScreenOwner.swift`, without the `logDescription` property and the
`ClaimHistoryRow` mirror and its initializer, which maps the outcome the same way):

```swift
typealias ClaimHistoryScreenOwner = ScreenOwner<ClaimHistoryScreenModel, ClaimHistoryViewState>

extension ClaimHistoryScreenOwner {
    func reload() {
        ShellLog.info("ClaimHistoryScreenOwner reload")
        model.reload()
    }
}

enum ClaimHistoryViewState: ScreenViewState {
    case loading
    case empty
    case failed
    case content([ClaimHistoryRow])

    init(_ kotlinState: ClaimHistoryScreenState) {
        self = oneOf { pick in
            kotlinState.handle(
                onLoading: { pick(.loading) },
                onEmpty: { pick(.empty) },
                onFailed: { pick(.failed) },
                onContent: { pick(.content($0.items.map(ClaimHistoryRow.init))) }
            )
        }
    }
}
```

No lifetime code is left per screen: the owner class, weak capture, task, cancel, `close()`, `assumeIsolated`, casts
and `default` branches are in the shared owner or gone.

Line counts, total / real (no blanks, no comments), from `task-16b-report.md` and the files at `acdedcaf45`:

| Code | Facade (before) | Screen pattern (after) |
|---|---|---|
| Kotlin `ClaimHistoryScreenModel.kt` | 141 / 92 | 60 / 50 |
| Kotlin `ClaimHistoryScreenState.kt` | 29 / 22 | 57 / 46 (two `handle` functions) |
| Kotlin per screen, real lines | 114 | 96 |
| Swift `ClaimHistoryScreenOwner.swift` | 115 / 97 | 78 / 67 |
| Shared once: Kotlin `ScreenModel.kt` | | 133 / 78 |
| Shared once: Swift `Kotlin/ScreenOwner.swift` | | 76 / 58 |

The ledger's 68 and 54 real lines for the shared files predate the review round. `ClaimHistoryView.swift` grew from 99
to 143 lines, for four `#Preview`s, the DEBUG sample view and a content view that takes the state instead of the owner.

**Exit cost.** Leaving KMP-NativeCoroutines for screens touches the `states` property and its imports in
`ScreenModel.kt`, one dependency line in `molecule-public`'s build file, the host's bridge tests, and the collection
loop of `ScreenOwner.swift` (about 10 lines). No per-screen Kotlin class, Swift-facing state, Swift view state,
typealias or view changes (`task-16b-proposal.md`, F).

**Proofs.** Android and desktop: `javap` of both release graph classes identical (0 lines), 87 / 21 / 111, the
release runtime classpath byte-identical, 1199 tests. Native tests 43 of 43 (`ScreenModelTest` 14,
`ClaimHistoryScreenModelTest` 18, `GetClaimsHistoryUseCaseTest` 2, `IosGraphTest` 9). Size 7,956,904 bytes, +3,400.
Header 6,020 lines, one coroutines protocol, as before. On the simulator: Loading then Empty, three open and back
cycles with deinit and `ClaimHistoryViewModel cleared` in the same millisecond and no request after, Failed with Retry
on a token failure, and the five-item sample rendered 5 of 5 rows. The shell's Swift package `KMPNativeCoroutinesAsync`
is now exact 1.0.6, matching the Kotlin library. Adding the host's `close()` renamed Okio's `close()` to `close_()` in
Swift (order-dependent name disambiguation); nothing in the shell or Ugglan calls it.

### Known limits of the screen pattern

From the ledger, "KNOWN LIMITS", and `task-16b-report.md`:

- **Two mappings per screen**: the Kotlin UI state to the Swift-facing state, then that to the Swift view state. Every
  nested sealed type or enum Swift maps needs its own hand-written `handle` and a Swift mirror.
- **ViewModels with navigation arguments** (assisted injection) are not supported by the host's `viewModel()` yet.
- **One ViewModel per model.** A second `viewModel()` call fails.
- **Only sealed classes are proven** through the Objective-C generic; sealed interfaces are untested.
- **A collection holding an item Swift never acknowledges stays suspended for the life of the process**, even after
  `close()`: the library waits for the next request without listening for cancellation. Pinned by a host test ("close
  while an item waits to be acknowledged clears the ViewModel but never ends that collection"); the ViewModel is still
  cleared once. Rule, written on both classes: the Swift loop must never `break` or `return` early. The same could
  happen if the owner's task were cancelled between the library buffering an item and the loop taking it; that path
  is not exercised.
- **`isolated deinit` is unverified below iOS 18.4**, while the real iOS app's minimum is 17.0. The compiler
  back-deploys it through a shim, present in the binary; the iOS 18.3 simulator runtime, which takes the shim, is
  installed but has no device, and a run needs a login there. Ugglan's copy of the owner avoids it with an ordinary
  `deinit` and a teardown helper (section 4); the test host has not adopted that yet.
- **The next login on the test host is the first run of the login code on Swift package 1.0.6.** It compiles
  unchanged; a login needs a logout first.

### Open points of the comparison

Not measured and not needed for the decision: SKIE's release lag over more than two Kotlin versions, a second screen
with events or a larger state, and SKIE's flow exception path. Real-data content and the member switch remain
unverifiable for every variant (section 13). If Kotlin's Swift export reaches a stable level with documented
coexistence with the Objective-C export, the exit cost above is the Swift-side cost of moving to it.

## 3. What was built, and the goals

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
| 4 | 16b (`acdedcaf45`, not in the plan) | The screen pattern after the owner's bridge decision: shared Kotlin `ScreenModel` host, shared Swift `ScreenOwner`, `handle` functions; replaces the facade | Android graph and runtime classpath identical; 43 native tests; lifetime, Failed and sample content on the simulator (section 2) |
| 5 | 17 (gate, no code) | The owner's go-ahead to switch `android/` and `ugglan/` to experiment branches | `owner-answers.md`, "Task 17 gate" |
| 5 | 18 (nothing to apply) | The owner's answers on logout and Datadog were already the merged behaviour | ledger |
| 5 | 19 (Ugglan `275e8fdc7`, `74d00e71a`, local only) | Ugglan's claim history screen on the shared screen model, and a DEBUG header logger | Section 4 |

### Goals 1 to 7 of the design

| Goal | Result | Evidence |
|---|---|---|
| 1. The test host shows loading, error with a working retry, empty and content, from the shared ViewModel, against staging | **Partly met.** Loading, Empty and Failed shown; Retry re-runs the request (it failed again under the forced token failure) and recovery was seen after a relaunch, not inside one process. Content from real data not verifiable; a Kotlin-built five-item sample rendered 5 of 5 rows | `task-12-report.md` a and e; `bridge-comparison.md`; `task-16b-report.md` |
| 2. The same in Ugglan's real screen on an experiment branch | **Partly met.** Loading, Empty, and Failed with "Try again" shown; recovery seen after a relaunch. Content not verifiable, and no sample path was added in Ugglan | `task-19-report.md` checks 1 and 4; `i1-claim-history-empty.png`, `i4-failed.png`, `i4-recovered.png` |
| 3. Leaving the screen stops the work | **Met.** Owner deinit and `ClaimHistoryViewModel cleared` together, no ClaimsHistory request after: 3 cycles per bridge candidate, 3 on the screen pattern in the test host, 3 in Ugglan | `task-12-report.md` b; `bridge-comparison.md`; `task-16b-report.md`; `i2-i3-cycles-excerpt.txt` |
| 4. Login and logout keep both clients consistent; the Kotlin client sends the token iOS holds | **Partly met.** Kotlin's token comes from the app's own fetcher in both hosts; logout reaches the Kotlin cache listener (Ugglan: 0.24 s after the app's logout) and no Kotlin request follows. Member A then B not verifiable (one identity); the cache clear is pinned by a native test | `parity.md`; `logout-excerpt.txt`; `IosGraphTest` |
| 5. Android's compiled DI graph and behaviour unchanged | **Met for the compiled graph, partly for behaviour.** Two deliberate graph differences, nothing else; 87 / 21 / 111 unchanged; APK class set unchanged. Behaviour on a device checked once, after Task 3 | Section 7 |
| 6. Shipped size recorded after every step | **Met** | Section 6, `size-log.md` |
| 7. The report lists every skip | **Met.** Nothing was skipped under the experiment-only rule; reductions are listed | Section 9 |

## 4. Step 5: the real iOS app

### Design as built

Ugglan's feature modules cannot import `HedvigShared`; only the App target links it. So the screen is split
(`task-19-proposal.md`, section 2; Ugglan commit `275e8fdc7`, 11 files, +464 lines, -24):

- **Claims module, Swift only.** A main-actor protocol with a Swift view state, a provider resolved once per screen in
  `onAppear` and kept in `@State` (Ugglan's `@Inject` resolves on every read), and a Swift implementation over the
  existing client for demo mode:

  ```swift
  @MainActor
  public protocol ClaimHistoryModel: AnyObject, Observable {
      var state: ClaimHistoryViewState { get }
      func reload()
  }

  @MainActor
  public protocol ClaimHistoryModelProvider {
      func makeModel() -> any ClaimHistoryModel
  }
  ```

- **App target, the Kotlin conformance.** The test host's generic `ScreenOwner`, copied, conforms to that protocol for
  this one screen, and maps the Kotlin state through `handle`:

  ```swift
  typealias ClaimHistoryModelKotlin = ScreenOwner<ClaimHistoryScreenModel, ClaimHistoryViewState>

  extension ScreenOwner: ClaimHistoryModel where Model == ClaimHistoryScreenModel, ViewState == ClaimHistoryViewState {
      func reload() {
          model.reload()
      }
  }

  struct ClaimHistoryModelProviderKotlin: ClaimHistoryModelProvider {
      func makeModel() -> any ClaimHistoryModel {
          ClaimHistoryModelKotlin(KotlinGraph.holder.claimHistoryScreenModel())
      }
  }
  ```

  DI registers the Kotlin provider in both non-demo branches and the Swift one in demo mode, so demo mode never
  creates a Kotlin screen model.
- **The five inputs over the app's own code.** `AccessTokenFetcher` calls Ugglan's `TokenRefresher.shared.refreshIfNeeded()`,
  then reads the Keychain token, and throws instead of returning nil, so Kotlin never sends an unauthenticated
  request or `Bearer null`. `DeviceIdFetcher` reads the app's stored device id. `LanguageStorage` reads the app's
  locale. `IosDemoMode` reads and writes `UgglanStore.isDemoMode`. `AppBuildConfig` is built from `Environment.current`,
  `Bundle.main` and `UIDevice`. The graph holder is created once, in `didFinishLaunchingWithOptions`, after the force
  logout hook and the locale are set.
- **One refresh path.** Both clients go through the same `TokenRefresher` and Keychain item; Kotlin has no refresh of
  its own.
- **Session calls.** `sessionChanged(loggedIn: true)` after the login exchange saves the token in
  `AuthenticationClientAuthLib.exchange(code:)` (not on refresh); `sessionChanged(loggedIn: false)` after the token is
  deleted in `AppDelegate.logout()`. No call at launch with a stored session: Kotlin's cache is empty at process start.
- **An iOS 17-safe teardown.** Ugglan's minimum is iOS 17.0, so the owner does not use `isolated deinit`. Its ordinary
  `deinit` hands the work to a small helper that cancels the collection and closes the model on the main thread, at
  once when already there, else on the next main-queue turn:

  ```swift
  func run() {
      screenLog.debug("\(self.name, privacy: .public) owner deinit")
      collection?.cancel()
      if Thread.isMainThread {
          model.close()
      } else {
          DispatchQueue.main.async { self.model.close() }
      }
  }
  ```

  The review found it correct on iOS 17 by reading; it ran on iOS 27.2 only. The test host still uses `isolated
  deinit`; adopting this teardown there was ruled a later one-file change and was not made.
- **Navigation.** A row pushes the claim detail by id and partner flag, `ClaimDetailView(claim: nil, type:)`, the
  path deep links already use.

### Feasibility

Xcode 27.2, Ugglan at `origin/master` (`c6a43377f`), the `android/` clone at `acdedcaf45` (`task-19-report.md`):

| Step | Result |
|---|---|
| `:umbrella:linkDebugFrameworkIosSimulatorArm64` in `android/` | 33 s (cache) |
| `scripts/use-local-umbrella.sh` | 174 s; one tracked file changed by the script (`local-mode-appdependencies.diff`) |
| Unmodified Ugglan master in local mode, signed simulator Debug | `BUILD SUCCEEDED` in 147 s |
| Modified Ugglan | `BUILD SUCCEEDED` after one missing import; 0 Swift warnings in the touched files; Ugglan's `ClaimsTests` 11 of 11, none exercising the new code |
| `scripts/use-released-umbrella.sh` | exit 0; the tracked project file back to its committed state, the local-mode marker gone |

The mode switch runs `tuist generate`, which deleted about 85 untracked generated leftovers in `ugglan/`. The branch
builds only in local mode.

### On the device

Ugglan Debug (staging app `com.hedvigForsakring.test.app`), iPhone 18 Pro simulator, iOS 27.2, a staging member with
an empty history, logged in by the owner with BankID. From `task-19-report.md`; evidence in `task-19/`.

| Check | Result | Evidence |
|---|---|---|
| Login | One Kotlin `LoggedIn` event to the demo and cache listeners, with empty token fields | report, "Proven in Ugglan" |
| Open | Seeded Loading, then Empty; exactly one Kotlin ClaimsHistory request, 200; the app's refresher consulted twice for it (F2), no refresh needed | `i1-open-excerpt.txt`, `i1-claim-history-empty.png` |
| Leave | 3 cycles: owner deinit and `ClaimHistoryViewModel cleared` on the main thread about 0.5 s after the back tap; 0 Kotlin requests after | `i2-i3-cycles-excerpt.txt`, `i2-after-back-1.png`, `i2-after-back-3.png` |
| Reopen | Answered from Kotlin's cache within 1 ms, no network request (today's Swift screen refetches on every visit) | `i2-i3-cycles-excerpt.txt`, `i3-reopen-1.png` |
| Failed | DEBUG `-kotlinFailTokenFetch YES`: the app's error view with "Try again"; Kotlin got `ApolloNetworkException` from the Swift throw; 0 requests sent; "Try again" ran the query and stayed Failed; no crash. Relaunched without it: one request, Empty | `i4-failed-retry-excerpt.txt`, `i4-failed.png`, `i4-recovery-excerpt.txt`, `i4-recovered.png` |
| Headers | User-Agent, `Hedvig-App-Version`, `apollographql-client-name`, both language headers and `Hedvig-TimeZone` identical on the Kotlin and Swift requests; `hedvig-device-id` absent on Kotlin, present on Swift (F3) | `i5-headers-kotlin.txt`, `i5-headers-swift.txt` |
| Datadog | The Kotlin request carries `x-datadog-trace-id`, `-parent-id`, `-origin`, `-sampling-priority`, `-tags`, `traceparent`, `tracestate` and `baggage`, the same set as the Swift request, added by the app's Datadog SDK (Kotlin installs none): **verified on the wire**. "Exactly one RUM resource per Kotlin request" was **not observable** on the simulator (the SDK's debug output logs batches, not resources) and the read-only RUM query was **not run** | `i5-headers-all.txt`, `i5-datadog-sdk-output-summary.txt` |
| Side by side, one minute | 14 Swift requests, 5 refresher checks, 0 refreshes, 0 forced logouts, 0 Swift errors, 0 Kotlin warnings or errors, 0 crash markers | `i6-side-by-side-summary.txt` |
| Demo mode | Swift demo data (Empty); no Kotlin screen model created, 0 Kotlin requests; leaving demo through logout reached the Kotlin listeners, nothing sent | `i7-demo-summary.txt`, `i7-demo-claim-history.png`, `i7-after-demo-logout.png` |
| Logout | Kotlin `LoggedOut` to the demo and cache listeners 0.24 s after the app's logout; 0 Kotlin requests after | `logout-excerpt.txt`, `logout-after.png` |
| Secrets | 58,912 log lines: 0 JWT-shaped strings, 0 `Bearer` with a credential, 0 non-empty token fields | `i8-secret-scan.txt` |

The request header check used a DEBUG-only logger on the branch (`74d00e71a`) that records headers after Datadog's
instrumentation, with credentials reduced to their shape. The raw `task-19/log-stream.txt` contains member data from
response bodies (body logging, section 12) and is not quoted here.

Not verifiable in Ugglan: content rows, the partner label and route, detail navigation from a row (no row to tap),
member A then B, and the same device id from both clients (Kotlin sends none).

## 5. Member-visible behaviour changes in the Ugglan screen

From the review of the Ugglan branch (ledger, "Task 19 review"). Whether to keep each is a product decision.

| Change | Inherent to the shared presenter today, or this experiment's Swift |
|---|---|
| History is cached for the life of the process: a second visit sends nothing, and a claim closed during the session appears only after a relaunch or a logout | **Inherent**: `GetClaimsHistoryUseCase` reads CacheFirst (F4, F5), the same on Android |
| No refetch per visit, including on return from a detail screen; no refresh short of a relaunch or a logout | **Inherent**, same cause |
| The cache is not cleared on a language change | **Inherent**: only login and logout clear it |
| The detail screen opens by id with `claim: nil`: it shows a loading state and fetches on every tap, fails offline, and its chat toolbar is empty until loaded (previously it opened pre-filled) | **This experiment's Swift**: the shared item carries no full claim, so the row routes by id. Not flagged by the implementer; found in review |
| A spinner on the first visit (previously a brief empty state while loading) | **This experiment's Swift**: hand-built loading view |
| A failed load shows an error with "Try again" (previously a silent empty state) | **This experiment's Swift**: hand-built error view, not the app's `ProcessingState` |
| A partner claim submitted on a given day may show the previous day west of UTC (Kotlin turns the date into UTC midnight; Swift parsed a local date) | **Inherent** to the shared mapping; not checked |

## 6. Size

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
| step 4 (screen pattern, Task 16b) | `b835fc17f9` | 7,956,904 | +3,400 vs 7,953,504 | +4,363,734 | 13,924 | 0 | `size-log.md`; measured at `8154d2cf15`, which differs only in how the build file names the same dependency and in one test |

The candidate rows are deltas against step 3, not against each other; the merged state of the branch is the screen
pattern row.

Steps 0 and 1 cost nothing because `:umbrella` did not link the new modules until Task 9. The 4.36 MB of step 2,
approximate `__text` growth by package from `nm` address gaps (`size-log.md`, `task-9-report.md`):

| Package | Growth |
|---|---|
| Compose runtime (Molecule) | +756 KB |
| Apollo runtime | +406 KB |
| kotlinx-datetime | +357 KB |
| Apollo normalized cache | +257 KB |
| Okio | +257 KB |
| DataStore core and preferences, kotlinx-serialization-protobuf (dead code, see section 12) | +172 KB |
| generated `octopus` code | +163 KB |
| Hedvig code | +143 KB |
| coroutines flow | +79 KB |
| androidx.collection | +55 KB |

These rows add up to about 2.6 MB of `__text`; the sources do not break down the rest. The auth arrangement chosen in
Task 9 is worth about 50 KB against the plan's (7,946,780 against 7,997,209 on the scratch build,
`task-9-proposal.md`). The header grew from 781 to 6,006 lines (`task-9-report.md`) and is 6,020 lines with the screen
pattern (`task-16b-report.md`, fix round 1). For comparison, the design
recorded about 1.3 MB for a Molecule ViewModel and about 2.9 MB for the network clients measured separately.

The size in Ugglan itself was not measured (ruling): the framework links statically into the app, so the app grows by
at most about the framework growth; a simulator Debug build is not comparable (`task-19-proposal.md`, section 6).
Step 5 changed no Kotlin, so the last row stands.

## 7. Android and desktop impact

Every compiled-graph difference introduced, from `javap` of the release graph classes against the parent (P2):

| Task | `AppGraph$Impl` | `ActivityRetainedGraphImpl` | `DesktopGraph$Impl` |
|---|---|---|---|
| 3 | 6 lines, all the `AuthTokenStorage` key: the field renamed `providesDataStoreAuthTokenStorageProvider` (declaration, one `putfield`, two `getfield`), and the factory `AuthTokenStorage$MetroFactory` replaced by `DataStoreAuthTokenStorageContributions$ToAppScope$ProvidesDataStoreAuthTokenStorageMetroFactory`, same two inputs and offsets | empty | not compared |
| 9 | 3 lines: the class also implements `ApolloClientMetroProviders`; one `checkcast` and the factory class change from `NetworkMetroProviders$ProvideApolloClientMetroFactory` to `ApolloClientMetroProviders$ProvideApolloClientMetroFactory`, same five inputs and offsets | empty | the same 3 lines |
| 2, 4, 5, 7, 8 | none | none | not compared |
| 6, 9b, 10, 11, 12 | no Android-compiled production change (nativeMain, nativeTest or Swift only) | | |
| 16b | none (0 lines against `cec22c80c7`); `releaseRuntimeClasspath` byte-identical; changes in `nativeMain`, `nativeTest`, Swift and a `nativeMain` dependency only | none | not compared |

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

## 8. Network client parity

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

In Ugglan (Task 19, `i5-headers-kotlin.txt`, `i5-headers-swift.txt`), the Kotlin and Swift requests carry the same
`User-Agent` (`com.hedvigForsakring.test.app 14.0.13 (iOS 27.2)`), `Hedvig-App-Version` (`ios;14.0.13`),
`apollographql-client-name` (`iOS:com.hedvigForsakring.test.app`), `Accept-Language` and `hedvig-language` (`en-SE`)
and `Hedvig-TimeZone`; Kotlin adds its five `X-*` headers; `hedvig-device-id` is on the Swift request only. The
Datadog row is no longer only read: in Ugglan the app's SDK added its trace headers to the Kotlin request (section 4);
the RUM resource count stays unobserved. Token refresh in Ugglan is the app's own `TokenRefresher`, consulted twice per
Kotlin request (F2).

Rows still resting on reading rather than observation: timeouts, language re-read, the client header names, logout on
`UNAUTHENTICATED`, and one RUM resource per Kotlin request (`research-ugglan-auth-and-datadog.md` read dd-sdk-ios
3.17.0 source: every `URLSessionTask` in the process is tracked automatically).

The device id gap is the one that matters: with it the backend cannot tell a Kotlin request from the same device as
the app's own. It is not an iOS problem; see F3 in section 11.

## 9. Skips and reductions

**Nothing was skipped under the experiment-only skip rule (owner decision 12).** No piece was left out because sharing
it would have made Android's `commonMain` much worse; the ledger's skip column is empty for every task. These were
reduced for other reasons:

| What | Why | What completing it would take |
|---|---|---|
| iOS simulator tests observe only the Loading state of the presenter | The display link does not tick in a Kotlin/Native test process, so the presenter composes once and never recomposes (`task-10-report.md`) | A frame clock that ticks in a test process, a change to `molecule-public` this work did not make. The manual runs covered every state except content and a reload that reaches the network |
| Content rows and the partner label from real data, in both hosts | The only BankID identity has no claims, and a claim takes 30 days to reach history (owner, ledger). The test host rendered a Kotlin-built sample instead; Ugglan has no sample path (ruling) | A staging member with claim history, including partner claims |
| Member A then member B | One BankID identity | A second staging identity |
| A server GraphQL error and `UNAUTHENTICATED` on a device | Needs the owner or a backend toggle | Same; both paths are pinned by native tests on a mock engine |
| Retry recovering inside one process | The forced token failure is a launch argument that cannot be turned off in the process; the network toggle was not used | One run with the network switched off and on |
| Owner login and logout through Android's new token storage | Judged unnecessary after the upgrade-in-place check (ledger, Task 3) | One login and logout on the Pixel |
| Task 7 ran no P3 or P5; Task 6 ran no P2 or P3 | Nothing Android-compiled or linked changed in a way they would see | Covered later: P3 at the `b1683bc248` integration proof, P5 at Task 9 |
| The test host's demo mode switch (design B, "if cheap") | Not built; demo mode was checked in Ugglan instead | A toggle in the test host |
| The SKIE flow exception path | Not probed; SKIE was not chosen | A probe that throws inside a collected flow |
| Desktop run of claim history | Optional in Task 5, skipped | `./gradlew :app-desktop:run` |
| App-level size of Ugglan | Not measured (ruling): the umbrella row bounds the growth | Two Release device builds of Ugglan, one per umbrella mode |
| The iOS 18.3 run of `isolated deinit` | Not requested once Ugglan used a teardown that does not rely on it | A device on the 18.3 runtime and a login, for the test host |

## 10. Deviations from the design and plan

| Deviation | Reason |
|---|---|
| The plan's own five, accepted before execution: `AuthTokenServiceImpl` stays in `commonMain`; the smoke test is split between `feature-claim-history` and `:umbrella` (design K said one module); the `android/` clone needs a new branch, not the integration branch; the Ugglan work needs a branch in `ugglan/` itself; Task 7 before Task 9 | The design text was unbuildable on those points (ledger, pre-flight scan) |
| Task 3: `auth-core-public` keeps its DataStore dependencies in `commonMain` | `BankIdSandboxState` is a `commonMain` `@Inject` class that takes `DataStore<Preferences>` |
| Task 9, option C: the Apollo client is provided per platform, and native reaches no token storage, `AuthRepository`, `AuthTokenService` or `MemberIdService`. Dropped: `IosTokenStore`, `NativeAuthTokenStorage`, `IosMetroProviders`, the `AuthMetroProviders` move. `logsOutOnUnauthenticated` and its three actuals deleted. `sessionChanged` takes a Boolean, not the tokens | Nothing in the slice reads tokens; the shared provider reached auth only for the logout interceptor, which native does not install. One refresh path, owned by Swift. The Boolean replaced the tokens after review found them printed to the iOS log (section 11). Cost: Kotlin cannot read tokens on iOS (section 12) |
| The per-platform Apollo provider reverses part of the desktop work's arrangement, in which the client and its interceptor chain were built once in `network-clients` `commonMain` for every graph | Native must not install the logout interceptor; the shared parts stay in one internal builder, and `ApolloInterceptorsTest` still pins the Android and JVM chain |
| Task 9b, a native `RumLogger`, not in the plan | Finding F1: without it any GraphQL error would crash the iOS app |
| The test host is `micro-apps/umbrella-consumer` evolved in place, not a new `app/app-ios/`: name, location and bundle id kept; committed token and hardcoded `JAVA_HOME` removed; dd-sdk-ios replaced by `KMPNativeCoroutinesAsync` for login (1.0.5, exact 1.0.6 since Task 16b); iOS 17.0, Swift 6 | Owner answer 3 (keep it); its synchronized source folder means new Swift files never touch the project file. `authlib` exports only KMP-NativeCoroutines forms |
| Xcode 27.2 instead of 26.4.1; simulator iPhone 18 Pro, iOS 27.2; Ugglan based on `origin/master` (`c6a43377f`) instead of `ff89c395e` with a local patch | Xcode updated mid-session; master needs 27.2; iPhone 17 Pro exists only on the 26.5 runtime now |
| Task 10 reduced; content checked through a sample in the test host | Section 9 |
| Task 16b, not in the plan: the facade's callback API replaced by the shared `ScreenModel` host and Swift `ScreenOwner` | The owner's bridge decision (section 2) |
| Task 18 applied nothing | The owner's answers on logout and Datadog were already the merged behaviour |
| Task 19: no token store input in Ugglan (the plan still named `IosTokenStore` over `KeychainHelper`) | Option C has no token store input |
| Task 19: `ClaimStore.historyClaims` and `fetchHistoryClaims` kept, now unused | Removing them changes a persisted store for no gain on a local branch (ruling) |
| Task 19: no `KMPNativeCoroutinesAsync` bump in Ugglan (Swift 1.0.5 against the Kotlin runtime 1.0.6) | Ugglan has shipped that pair for login since 2026-09-16, and the two tags have identical Swift sources (`task-19-proposal.md`, section 6) |
| Task 19: no DEBUG sample-content path in Ugglan | Ruling; the sample was checked in the test host |
| Task 19: the owner in Ugglan uses an ordinary `deinit` with a teardown helper, not the test host's `isolated deinit` | Ugglan's minimum is iOS 17.0 and `isolated deinit` was verified on 27.2 only |
| Task 19: no fix round on the Ugglan branch after review | The branch is never merged; the review's findings are this experiment's output and are in sections 5 and 12 |

## 11. Findings in existing code

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
- **In Ugglan's tooling, not fixed:** the mode switch scripts run `tuist generate`, which deletes untracked generated
  leftovers in `ugglan/`; Ugglan's pre-commit hook expects a swift-format binary under `build/` and prints
  "-i: command not found" when it is missing (`task-19-report.md`, findings 6 and 7).

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

## 12. What a real adoption needs that this experiment lacks

Most blocking first. Three items are bugs on `develop` that affect Android today and now also matter for parity on
iOS; they are labelled **fix on `develop`**.

| # | Need | What it would take |
|---|---|---|
| 1 | A published umbrella with these types. The Ugglan branch builds only in local mode | Merge the integration work, publish an umbrella version, bump Ugglan's pin |
| 2 | Body logging gated correctly on iOS. Kotlin's `isProduction` is `buildType == "release" && applicationId == "com.hedvig.app"`, and the Ktor client logs request and response bodies whenever it is false. On iOS that means body logging is **on** in Ugglan Debug, in Release builds of the staging app (the TestFlight build), and in Debug builds of the production app (production member data in the device log), and **off** only in Release of the production app. Separately, the iOS logger writes every priority to the system log, and `LoggingInterceptor`'s ERROR line and DEBUG `Response data` line carry response data | An iOS-correct gate for body logging, and priority filtering in the iOS logger or redaction of those lines in common code (which also changes Android's logs) |
| 3 | **Fix on `develop`, F3:** the authenticated Kotlin client sends no `hedvig-device-id` (on Android since `b93c73e300`, January 2026). In Ugglan the Swift request has it and the Kotlin one does not | Install `DeviceIdInterceptor` on the client `provideHttpClient` builds |
| 4 | **Fix on `develop`, F2:** the token fetcher is called twice per request, which sends `Authorization: Bearer null` when the second read is null. Ugglan's fetcher throws instead of returning nil, so it cannot send `Bearer null`, but the app's refresher runs twice per Kotlin request | One fetcher call per request in `addAuthPlugin` |
| 5 | **Fix on `develop`, F5:** reload is answered from the cache and never refetches; with F4, history is cached for the process (section 5) | A decided cache policy for the use case (network-first on reload, refetch per visit, or an expiry), which changes Android too |
| 6 | GraphQL errors on iOS reach only the system log, not the app's Datadog RUM | A Swift-supplied `RumLogger` backed by Datadog, installed before `createIosGraphHolder`, which already keeps an earlier install |
| 7 | A way back to the Swift path without a release | A kill switch (feature flag) choosing the Swift or the Kotlin provider |
| 8 | Tests for the new Swift code (none of Ugglan's 11 `ClaimsTests` exercise it) | Tests of the provider, the mapping and the teardown |
| 9 | The detail screen decision: it now loads by id with a loading state (section 5) | Either carry enough in the shared item to open it pre-filled, or accept the change |
| 10 | The app's own loading and error components | Use `ProcessingState` instead of the hand-built views; move `ClaimHistoryModelSwift` out of `DemoImplementation/`, since it is the general Swift implementation |
| 11 | The cost of creating the Kotlin graph on the main thread at launch is unknown | A launch measurement with timestamps around the creation |
| 12 | Datadog: "one RUM resource per Kotlin request" unchecked; no duration breakdown for Kotlin requests | A read-only RUM query of a staging session; register Ktor's session delegate class for the breakdown (needs the class visible to Swift, unverified), or accept the gap as `authlib` does |
| 13 | Ugglan's custom environment (`Environment.custom`, launch argument only) sends Swift to its own router while Kotlin uses the dev router | An optional GraphQL URL on `AppBuildConfig` that the build constants prefer |
| 14 | Kotlin learns the session only from login and logout calls | An `isLoggedIn` input at graph creation |
| 15 | One teardown for both hosts: Ugglan's ordinary `deinit` with a helper; the test host's `isolated deinit` is unverified below iOS 18.4 | Adopt Ugglan's teardown in the test host (one file) |
| 16 | The framework header carries Apollo's `ClaimsHistoryQuery` family, Metro's `ClaimHistoryViewModelModule` and prefixed Apollo, Okio and datetime types (about 345 lines) | Internal Apollo codegen for the module, which touches Android-compiled code; the sources propose no fix for the Metro module |
| 17 | About 172 KB of dead DataStore code is linked | `NativeDataStoreMetroProviders` as a `@BindingContainer` object, or `DeviceIdFetcher` moved out of `core-datastore-public`; each with its own graph check |
| 18 | Kotlin cannot read tokens on iOS; a later screen that injects `AuthTokenService` or `MemberIdService` fails to compile | A Swift token storage input and a native `AuthRepository` provider (the plan's option A, measured at about 50 KB) |
| 19 | Two GraphQL caches: a Swift mutation does not update Kotlin's cached data, and the reverse | Per feature: refetch policy or explicit invalidation |
| 20 | The screen pattern's known limits (section 2): nav-argument ViewModels unsupported, one ViewModel per model, two mappings per screen, the never-exit-early loop rule | Design input for the second feature |
| 21 | The KMP-NativeCoroutines Swift package and Kotlin library move together; Ugglan pins the Swift package at 1.0.5; the Kotlin library is 1.0.6, and 1.0.7 is the Kotlin 2.4.21 release | Bump both sides together |
| 22 | Minor: Kotlin's `setDemoMode(false)` lands 0.24 s after a logout and could overwrite a demo entry made in that window (practically unreachable); `setLanguageTag` is asynchronous; Swift sees prefixed names (`Network_clientsAccessTokenFetcher`) | Order the write before the logout returns; `typealias`es in the host |

## 13. What was not verified

- Content rows from real data, the partner label and route from real data, `Identifiable` diffing with real items,
  detail navigation from a history row.
- A member switch (A then B never shows A's claims) on a device; only the cache clear is tested and logged.
- A server GraphQL error or `UNAUTHENTICATED` on a device.
- Retry recovering inside one process; recovery was seen after a relaunch.
- Datadog: one RUM resource per Kotlin request (not observable on the simulator; query not run). The trace headers
  on the wire are verified.
- The cost of creating the Kotlin graph on the main thread at launch.
- Whether the backend returns claim type text already translated for the member's language; the row shows the
  string as received.
- The partner submission day west of UTC.
- Any iOS version other than 27.2, any device other than the simulator. In particular the test host's `isolated
  deinit` below iOS 18.4, and Ugglan's teardown on iOS 17 (correct by reading).
- Login on the KMP-NativeCoroutines Swift package 1.0.6 in the test host (its next login is the first; the Swift
  sources of 1.0.5 and 1.0.6 are identical).
- Kotlin's main-thread delivery on a device: the Swift precondition could not observe it (section 2); it rests on
  `ScreenModelTest`. In Ugglan the deinit and clear lines ran on the process's main thread.
- Any production configuration: every run used staging.
- Android on a device after Task 3, any minified build on a device, a login and logout through the new storage.
- Timeouts and language re-read parity.
- The SKIE flow exception path; candidate 2's declared exception domain.
- R8's representative swaps in Tasks 3 and 9 as the cause of the mapping differences.
- In the test host: a Keychain write failure, an exchange failure after a scan, the login polling pause
  off-foreground, two overlapping requests that both complete after one refresh.
- A response arriving while the app is suspended in the background.
- App-level size of Ugglan.

## 14. Later work, already decided

Navigation out of presenters, piloted on `feature-payments`; the 44 leak files in 24 modules beyond claim history;
presenters holding `TextFieldState`, last (owner decision 4); policy effects out of `HedvigApp`; the other 25 features.

## 15. State left behind

- `feat/desktop-app-integration` at `acdedcaf45`, pushed, with every merged task of this work.
- `docs/desktop-app-claude-md` holds this report, the design and the plan.
- `ios/bridge-plain`, `ios/bridge-nativecoroutines`, `ios/bridge-skie`: pushed, unmerged, kept as the record of the
  comparison.
- `ugglan/` on the local branch `experiment/kmp-claim-history` (`275e8fdc7`, `74d00e71a` on `origin/master`
  `c6a43377f`), never pushed, back in released umbrella mode; the owner's modified
  `.claude/agents/accessibility-agent.md` untouched. Restore with `git -C ugglan switch fix/firebase-user-id-after-consent`.
- `android/` on `experiment/ugglan-local-umbrella` at `acdedcaf45`, clean. Restore with
  `git -C android switch develop` and delete the branch.
- Simulator iPhone 18 Pro (iOS 27.2): the test host installed and logged in; Ugglan installed, logged out,
  terminated.
- 16 worktrees under `android/.claude/worktrees/` named `ios-*`, one per task branch of this work, plus
  `desktop-integration` (the integration branch) and `docs-desktop-claude-md` (the docs branch).
- The run directory `android/.superpowers/sdd/2026-11-14-ios-sharing/`. Its raw simulator logs (the `log-stream*.txt`
  files under `task-11/` to `task-16b/` and `task-19/`) contain member data from staging responses and must not be shared;
  the `*-excerpt.txt` and `*-summary.txt` files are safe to read.

### Suggested next decisions

- Whether to commit to the approach, with sections 5, 12 and 13 in view.
- The three fixes on `develop` (F3, F2, F5), which are worth making whatever is decided, since they affect Android.
- When to open PRs for the desktop and iOS work on the integration branch, and in what split.
- If committing: which feature is second, with the screen pattern's known limits (section 2) as its design input.
