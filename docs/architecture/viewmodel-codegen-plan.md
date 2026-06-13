# Plan: `@HedvigViewModel` KSP codegen for ViewModel DI boilerplate

Status: **SHIPPED.** Processor implemented, all ViewModels migrated, golden tests written and
passing, both Android and iOS targets verified green. This document is retained as the design
record; the day-to-day convention now lives in `navigation-and-di.md` / `CLAUDE.md`.

## Completion summary (what actually landed)

- **Annotation** `@HedvigViewModel(val scope: KClass<*> = ActivityRetainedScope::class)` in
  `core-common-public/commonMain` next to `Scopes.kt`.
- **Processor** `app/navigation/viewmodel-processor` with three generation branches:
  no-arg `<VM>Module`; a single `SavedStateHandle` → `<VM>Factory : ViewModelAssistedFactory`
  (zero call-site args); anything else (nav args, optionally alongside a `SavedStateHandle`) →
  `<VM>Factory : ManualViewModelAssistedFactory`. A mixed VM (nav arg + `SavedStateHandle`) is
  **supported**: the handle is just another `@Assisted` factory param, supplied at the call site via
  the manual-factory lambda's `CreationExtras` — `create(key.x, extras.createSavedStateHandle())`.
  (An earlier draft of this plan called the mix a hard error; that was wrong — `metrox`'s manual
  factory lambda is `FactoryType.(CreationExtras) -> VM`, so both sources are available at once.)
- **DSL** `hedvig { viewModels(iosShared = true) }` — routes KMP modules to `kspCommonMainMetadata`
  (visible to iOS) and android-only VMs in KMP modules to `kspAndroid` via `iosShared = false`
  (e.g. `feature-claim-history`), resolving the double-emission risk.
- **Migration**: all ~73 ViewModels now use `@HedvigViewModel` (76 usages incl. infra), hand-written
  factories/`@ViewModelKey`/`@ContributesIntoMap` contributions deleted, and the ~40 manual-factory
  call sites switched from nested `.Factory` to the generated top-level `<VM>Factory`.
- **Tests**: `HedvigViewModelProcessorTest.kt` — 7 KSP golden tests (kctfork, KSP2) covering no-arg,
  assisted 1-param, assisted qualifier passthrough, `SavedStateHandle`, the `SavedStateHandle`+nav-arg
  mix (manual factory carrying both), scope override, and `internal` visibility. Tests assert on generated source + processor diagnostics rather than the
  downstream compile exit code (kctfork's embedded compiler can't read this repo's stdlib metadata
  version — a harness artifact, documented in the test header).
- **Verification**: `:app:compileDebugKotlin` (Android) and `:shareddi:compileKotlinIosSimulatorArm64`
  (iOS, proves contributions reach `IosGraph` with no duplicate-class error) both build successfully.

---

_Original blueprint follows._

## Decisions (locked)

| Decision | Choice | Rationale |
| --- | --- | --- |
| Assisted mechanism | Typed `ManualViewModelAssistedFactory`; generate top-level `<VM>Factory` | Keeps full type safety and direct `{ create(key.contractId) }` call sites. The `CreationExtras` path was rejected: it collapses the call-site type params but re-introduces stringly-typed arg marshaling at every call site — a net downgrade for nav args. |
| Processor scope | Both no-arg **and** assisted VMs | Uniformity is the goal; the no-arg win is marginal alone but every VM ends up on one pattern. |
| Rollout order | **Phase B (commonMain) spike first** | The KMP wiring is the only real risk; prove it before investing in tests + the ~40-site migration. |
| Generated factory name | `<VM>Factory` | Concise, mirrors the old nested `.Factory` 1:1. No collisions found today. |
| `SavedStateHandle` VMs | **Supported** (3rd generation branch) | A `SavedStateHandle` can't be passed at a call site; it must come from `CreationExtras`, so these use metrox's `ViewModelAssistedFactory` (keyed by VM class) instead — and need *no* call-site migration. |
| Spike module | `feature-help-center` | 6 commonMain VMs spanning both shapes + an Android target + consumed by iOS via `IosGraph`: exercises the full matrix and the double-emission risk in one module. |

Why not a Metro-native path (researched, all rejected):
- `metro { generateAssistedFactories = true }` synthesizes the factory *interface*, but Metro **bans
  `@Contributes*` on `@AssistedInject` types**, so the synthesized factory can't carry
  `@ManualViewModelAssistedFactoryKey` + `@ContributesIntoMap` — useless for the VM-multibinding case.
- `@ContributesAssistedFactory` (Metro discussion #1300) is the first-party version of this idea but
  is an **unanswered proposal** (no PR, no timeline). We design `@HedvigViewModel` so a future swap to
  it is mechanical, but we can't block on it.
- No off-the-shelf Metro-targeting VM-codegen exists (Tangle/Whetstone/pixnews are all Anvil/Dagger).

## Goal

Remove the repetitive Metro DI wiring developers hand-write on every ViewModel:

- **No-arg VM today** — three annotations plus a `binding<ViewModel>()` helper and a scope import:
  ```kotlin
  @Inject
  @ViewModelKey
  @ContributesIntoMap(ActivityRetainedScope::class, binding<ViewModel>())
  internal class DeleteAccountViewModel(...) : MoleculeViewModel<...>(...)
  ```
- **Assisted VM today** — `@AssistedInject` plus a fully hand-written nested factory:
  ```kotlin
  @AssistedInject
  internal class ContractDetailViewModel(
    @Assisted contractId: String,
    useCase: GetContractForContractIdUseCase,
  ) : MoleculeViewModel<...>(...) {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(ActivityRetainedScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
      fun create(@Assisted contractId: String): ContractDetailViewModel
    }
  }
  ```

Counts (census, time of writing): **36 assisted** VMs (the painful nested factories) and **37 no-arg**
VMs. Assisted-param distribution: 26×1 param, 7×2, 2×3, 1×5; types are a mix of `String`/`String?` and
project types (`SummaryKey`, `CoInsuredFlowType`, `AddonVariant?`, …); nullable params are common. All
VMs are `internal` except `ComparisonViewModel` (public) — the processor mirrors VM visibility. One
assisted VM (`SwedishLoginViewModel`) takes a `SavedStateHandle` and is the `SavedStateHandle` branch
below. 10 of the VMs live in `commonMain` across 3 modules (help-center ×6, remove-addons ×3,
claim-chat ×1).

## The hard constraint that shapes the whole design

**KSP is additive only.** A KSP processor generates *new* files; it cannot add an annotation to an
existing class, nor insert a nested member. Therefore the processor **cannot** stamp
`@Inject` / `@AssistedInject` / `@Assisted` onto your ViewModel — Metro's own compiler plugin must see
those on the real constructor, and only a Kotlin compiler plugin (not KSP) could synthesize them.
Writing a sibling compiler plugin that co-operates with Metro's is high-risk and version-fragile, so
it is explicitly **rejected**.

Consequence: the developer still writes the one unavoidable Metro constructor annotation
(`@Inject` or `@AssistedInject`, plus `@Assisted` on assisted params) **plus** a single new marker
`@HedvigViewModel`. The processor generates everything else — the factory and/or the map contribution
— into a sibling file.

## Developer experience after this lands

No-arg:
```kotlin
@Inject
@HedvigViewModel
internal class DeleteAccountViewModel(
  private val useCase: DeleteAccountStateUseCase,
  backstack: Backstack,
) : MoleculeViewModel<...>(...)
```

Assisted (no hand-written factory at all):
```kotlin
@AssistedInject
@HedvigViewModel
internal class ContractDetailViewModel(
  @Assisted contractId: String,
  useCase: GetContractForContractIdUseCase,
) : MoleculeViewModel<...>(...)
```

`@HedvigViewModel` carries the scope and **defaults to `ActivityRetainedScope`**, centralizing the
scope decision (see `navigation-and-di.md`) so individual VMs never name a scope. The rare `AppScope`
case is `@HedvigViewModel(AppScope::class)` (e.g. `ImpersonationReceiverViewModel`, which is resolved
by its own standalone Activity through the AppScope factory).

## What the processor generates (shapes verified against metrox 1.1.1 internals)

metrox facts that make these correct:
- `@ViewModelKey` is `@MapKey(implicitClassKey = true)` with signature
  `ViewModelKey(val value: KClass<out ViewModel> = Nothing::class)` — on a `@Provides` method pass the
  class explicitly.
- `@ManualViewModelAssistedFactoryKey` is also `@MapKey(implicitClassKey = true)`; the
  `manualAssistedFactoryProviders` map is keyed by the **factory class**.
- `@ViewModelAssistedFactoryKey(VM::class)` keys the `assistedFactoryProviders` map by the **VM class**;
  its factory interface is `ViewModelAssistedFactory { fun create(extras: CreationExtras): ViewModel }`,
  resolved at the call site by the no-type-param `assistedMetroViewModel<VM>()`.
- The manual-path compose call site is
  `assistedMetroViewModel<VM, FactoryType : ManualViewModelAssistedFactory> { create(...) }` — it needs
  **both** type params, so the generated factory must be a named, referenceable top-level type.

The processor has **three** generation branches, decided by inspecting the primary constructor's
`@Assisted` params:

**(1) Assisted, no `SavedStateHandle`** → generate a top-level factory in the VM's package, named
`<VM>Factory`, mirroring the `@Assisted` constructor params (names + types + order preserved).
Visibility matches the VM:
```kotlin
@AssistedFactory
@ManualViewModelAssistedFactoryKey
@ContributesIntoMap(ActivityRetainedScope::class)
internal fun interface ContractDetailViewModelFactory : ManualViewModelAssistedFactory {
  fun create(@Assisted contractId: String): ContractDetailViewModel
}
```

**(2) Assisted, `SavedStateHandle`-only** → a `SavedStateHandle` cannot be supplied at a call site, so
use the `ViewModelAssistedFactory` (`CreationExtras`) path keyed by the VM class. No call-site change
(these are already `assistedMetroViewModel<VM>()`):
```kotlin
@AssistedFactory
@ViewModelAssistedFactoryKey(SwedishLoginViewModel::class)
@ContributesIntoMap(ActivityRetainedScope::class)
internal fun interface SwedishLoginViewModelFactory : ViewModelAssistedFactory {
  override fun create(extras: CreationExtras): SwedishLoginViewModel =
    create(extras.createSavedStateHandle())
  fun create(@Assisted savedStateHandle: SavedStateHandle): SwedishLoginViewModel
}
```
A VM mixing `SavedStateHandle` with other `@Assisted` nav args is **supported via the manual-factory
path** (branch 1): the `SavedStateHandle` becomes one more `@Assisted` param on `<VM>Factory`'s
`create(...)`. The `metrox` manual-factory call-site lambda is `FactoryType.(CreationExtras) -> VM`, so
the caller supplies the nav arg from the closure *and* the handle from the passed extras —
`assistedMetroViewModel<VM, VMFactory> { extras -> create(key.contractId, extras.createSavedStateHandle()) }`.
No stringly-typed marshaling: every value is typed at the call site. (The dedicated branch-2
`ViewModelAssistedFactory` is kept only for the *single*-`SavedStateHandle` case, purely so its call
site stays the zero-arg `assistedMetroViewModel<VM>()`.)

**(3) No-arg** → generate a contributed module equivalent to
`@ContributesIntoMap(scope, binding<ViewModel>())`:
```kotlin
@ContributesTo(ActivityRetainedScope::class)
internal interface DeleteAccountViewModelModule {
  @Provides
  @IntoMap
  @ViewModelKey(DeleteAccountViewModel::class)
  fun provide(viewModel: DeleteAccountViewModel): ViewModel = viewModel
}
```
(Metro constructs the VM via its `@Inject` constructor and feeds it into this provider.)

## Module & wiring design (decided: dedicated module + explicit marker)

- **Annotation** `@HedvigViewModel(val scope: KClass<*> = ActivityRetainedScope::class)` lives in
  `core-common-public/commonMain` next to `Scopes.kt`, so KMP ViewModels can use it.
  `@Target(CLASS)`, `@Retention(BINARY)` (KSP reads it; not needed at runtime).
- **Processor** in a new JVM module `app/navigation/viewmodel-processor`, mirroring
  `navigation-keys-processor/build.gradle.kts`:
  ```kotlin
  plugins { id("hedvig.jvm.library") }
  dependencies {
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.ksp.symbolProcessingApi)
  }
  ```
  Files:
  - `HedvigViewModelProcessor.kt` (+ `HedvigViewModelProcessorProvider`), modeled on
    `NavKeySerializerProcessor.kt`.
  - `src/main/resources/META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`
    naming the provider.
- **Why a separate module** rather than extending `navigation-keys-processor`: different opt-in,
  and crucially a **different source-set wiring** (VM contributions for shared VMs must land in
  `commonMain`, whereas nav-key serializers only need Android). Keeping them separate avoids muddying
  the clean single-responsibility nav processor.
- **DSL**: add a `viewModels()` handler to `HedvigGradlePluginExtension.kt` mirroring the existing
  `NavKeysHandler` (the nav handler applies KSP and does
  `add(kspConfiguration, project(":navigation-keys-processor"))`). Opt-in per module via
  `hedvig { viewModels() }`.

### Processor logic sketch

```
discover: KSClassDeclaration annotated @HedvigViewModel
  read scope arg (default ActivityRetainedScope)
  assisted    = primaryConstructor.params with @Assisted
  savedState  = assisted with type SavedStateHandle
  classify:
    assisted empty                       -> NO_ARG       -> emit <VM>Module
    exactly one assisted, and it is SSH  -> SAVED_STATE  -> emit <VM>Factory (ViewModelAssistedFactory + createSavedStateHandle)
    else (nav args, ± an SSH among them) -> ASSISTED     -> emit <VM>Factory (ManualViewModelAssistedFactory, all @Assisted params)
  match VM visibility on every generated type
writeTo(aggregating = true, originatingKSFiles = [vm.containingFile])
```
One generated file per VM (unlike the nav processor's one-per-package), keyed by the VM's own FQN, so
names are globally unique by construction and incremental builds are tight.

## The real risk: KMP / `commonMain` ViewModels (Phase-B spike — done first)

10 VMs live in `commonMain` (help-center ×6, remove-addons ×3, claim-chat ×1) and are compiled for
iOS via `IosGraph` (which aggregates `ActivityRetainedScope` — see `navigation-and-di.md`). Their
generated contribution **must be visible to the iOS compilation**, i.e. generated into `commonMain`
via `kspCommonMainMetadata` — **not** `kspAndroid`, which is where the nav processor puts its output
(nav-key serializers only ever need Android).

Complication: a KMP module can also hold `androidMain`-only VMs (e.g. `ClaimHistoryViewModel`), which
need the android pass. Running the processor on **both** `kspCommonMainMetadata` and `kspAndroid`
double-processes `commonMain` symbols (visible to both) → duplicate generated class.

This is the only real risk, so it is being **spiked first** (before tests or any migration). Census
makes it tractable: only 3 modules hold commonMain VMs and only 1 androidMain-only VM exists in the
whole codebase, so the painful "mixed module" case is nearly absent.

### Spike target: `feature-help-center`

Its 6 commonMain VMs span both shapes (`PuppyArticleViewModel` is assisted and already resolved from
`nativeMain`; the rest are no-arg), it has an Android target, and it is consumed by iOS via `IosGraph`
— so one module exercises the full matrix and the double-emission risk.

Spike wiring: apply KSP to **`kspCommonMainMetadata`** for KMP modules (output visible to all targets)
and deliberately **not** to `kspAndroid` for the same module, so the android compile consumes the
common-generated code rather than re-generating it — this is the double-emission guard. Mixed modules
(commonMain + androidMain-only VMs) remain an open item; with one such VM total it can be special-cased.

Spike done = both green:
- `:feature-help-center` Android compile, and
- `:shareddi:compileKotlinIosSimulatorArm64` (proves the generated map contributions reach `IosGraph`
  with no duplicate-class error).

After the spike proves the wiring, **Phase A** (the non-KMP majority, plain `ksp`) is downhill.

## Call-site migration

- **No-arg: zero changes.** Call sites use `metroViewModel<VM>()`, which references no factory.
- **`SavedStateHandle` VMs: zero changes.** Already `assistedMetroViewModel<VM>()` (keyed by VM class).
- **Manual-factory assisted: ~40 sites** (36 VMs, a few with multiple call sites).
  `assistedMetroViewModel<VM, VM.Factory> { ... }` → `assistedMetroViewModel<VM, VMFactory> { ... }`
  (nested `.Factory` → generated top-level `<VM>Factory`). Mechanical, scriptable.

## Testing

- The nav processor has **no tests**. Add `kotlin-compile-testing` (KSP) golden tests for the new
  processor: no-arg; assisted with 1 param; assisted with N params; `internal` vs `public` visibility;
  explicit scope override; a class that is a `ViewModel` but lacks `@Inject` (should error clearly).
- Backfill a couple of golden tests for `NavKeySerializerProcessor` while the harness is set up.

## Execution order (Phase B first)

1. Add `@HedvigViewModel` to `core-common-public/commonMain`.
2. Create `viewmodel-processor` module (build file, all three generation branches, ServiceLoader
   registration).
3. Add `viewModels()` DSL handler to `HedvigGradlePluginExtension.kt`, wired to `kspCommonMainMetadata`
   for KMP modules (Phase-B wiring).
4. **Spike on `feature-help-center`:** annotate its 6 commonMain VMs with `@HedvigViewModel`, delete
   their hand-written contributions/factories, and verify both `:feature-help-center` (Android) and
   `:shareddi:compileKotlinIosSimulatorArm64` are green with no duplicate-class error. **Gate: if this
   fails, stop and rethink before any further migration.**
5. Compile-testing harness + golden tests for the three generated shapes (no-arg; manual-factory
   assisted 1/N params; `SavedStateHandle`; internal vs public; explicit scope override; a `@Inject`-less
   class → clear error).
6. Phase A: extend `viewModels()` to plain `ksp` for non-KMP modules; migrate them.
7. Script the no-arg migration (drop `@ViewModelKey` + `@ContributesIntoMap` + the `binding` import;
   add `@HedvigViewModel`).
8. Script the assisted migration (delete nested factories; migrate the ~40 call sites to `<VM>Factory`).
9. Verify both targets, delete dead code, update `navigation-and-di.md` with the new convention.

## Per-target KSP: investigated and rejected (spike result)

We later spiked the obvious-looking alternative — drop `kspCommonMainMetadata` and instead wire the
processor per leaf target (`kspAndroid` + `kspJvm` + `kspIosArm64` + `kspIosSimulatorArm64`), the
"idiomatic" KMP-KSP setup — to see whether it removes the `iosShared` flag and the mixed-module
limitation. **It does not. It trades one wall for a worse one.**

Spike on `feature-help-center`: moved `helpCenterEntries()` to `androidMain` (it names generated
factory types and only `:app` calls it) and switched to per-target ksp. Result:
- **Android leaf compiled green.** `kspAndroid` generated the factories into the `androidMain` leaf
  source set; `androidMain` code (the moved entries) resolved them.
- **iOS leaf failed:** `compileKotlinIosSimulatorArm64` — `Unresolved reference
  'PuppyArticleViewModelFactory'` in `nativeMain/PuppyGuideViewControllers.kt`. The factory was
  generated into the `iosSimulatorArm64Main` **leaf**, but the iOS view controllers live in
  `nativeMain`, an **intermediate** source set (parent of both iOS leaves).
- **Controlled confirmation:** moving that one file from `nativeMain` down to `iosSimulatorArm64Main`
  compiled green. So the cause is Kotlin's source-set hierarchy, not KSP wiring: per-target KSP emits
  only into leaf source sets, and a parent/intermediate source set (`commonMain`, `nativeMain`,
  `iosMain`) cannot see symbols defined in its descendants.

Follow-up tried: "then just run KSP on the `nativeMain` source set" so the intermediate set itself
gets the generated code. **KSP has no such task.** Adding to `kspNativeMain` fails at configuration
(`Configuration with name 'kspNativeMain' not found` — it shows in the `dependencies` report only as
an unrealized `(n)` placeholder). Listing the real tasks shows KSP attaches to leaf compilations
(`kspAndroidMain`, `kspKotlinJvm`, `kspKotlinIosArm64`, `kspKotlinIosSimulatorArm64`) plus a *subset*
of shared-source-set **metadata** compilations — `kspCommonMainKotlinMetadata` and
`kspJvmAndAndroidMainKotlinMetadata` — but **not** `nativeMain` (nor `appleMain`/`iosMain`). So the
only KSP-processable ancestor of the native iOS view controllers is `commonMain` itself.

Why `kspCommonMainMetadata` is the right call: it generates into `commonMain`, the common **ancestor**
of every other source set — android, jvm, `nativeMain`, and the iOS leaves all see it — and it is the
only shared set KSP will process that sits above the native code. Per-target only ever generates into
leaves, so any shared code that references a generated factory breaks. This repo has such shared code
in two distinct places — `commonMain` entries *and* `nativeMain` iOS view controllers — and to make
per-target work the latter would have to be duplicated into every iOS leaf source set: strictly worse
than today.

**Final shape:** every KMP module wires *both* `kspCommonMainMetadata` (commonMain VMs, visible to all
targets incl. iOS) and `kspAndroid` (androidMain-only VMs). `kspAndroid` re-sees commonMain and would
double-emit, so the processor detects the single-target leaf pass (`environment.platforms.size == 1`)
and skips commonMain symbols there — only the metadata pass emits them. This started as an `iosShared`
toggle but was unified: since the dedupe is purely a function of the pass and the source path, the flag
was always-on dead config and was removed, so a single module may freely mix commonMain + androidMain
VMs with no per-module configuration.

## Open spike items
- Confirm the no-arg generated `@Provides @IntoMap @ViewModelKey(VM::class)` module resolves
  identically to the class-level `@ContributesIntoMap(scope, binding<ViewModel>())` (high confidence;
  verify by compiling one).
- Settle `commonMain` double-emission for the **mixed-module** case (commonMain + androidMain-only VMs);
  only `ClaimHistoryViewModel` hits it today, so it can be special-cased if the general fix is costly.
