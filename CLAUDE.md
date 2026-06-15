# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Hedvig Android app - A modern Android application built with Jetpack Compose, Apollo GraphQL, and Kotlin. The app uses a highly modular architecture with 80+ modules organized into feature, data, and core layers.

Two foundations are newer than most of the codebase and are easy to get wrong if you assume the old patterns:

- **Dependency injection is Metro** (`dev.zacsweers.metro`), a compile-time DI framework. **Koin is gone.** If you find yourself writing a `module { }` or calling `get()`, stop — you are following a stale pattern.
- **Navigation is Navigation 3** (`androidx.navigation3`) on top of a single app-owned back stack. **There is no `NavController`, no `NavHost`, no route strings, no `navgraph`/`navdestination`.** Destinations are `@Serializable` keys; the back stack is a plain mutable list of keys.

A full narrative of *why* these look the way they do — the engineering decisions, the alternatives rejected, the invariants — lives in `docs/architecture/navigation-and-di.md`. Read it before making structural changes to navigation or DI. This file is the day-to-day quick reference.

## Essential Setup Commands

### Initial Setup
```bash
# 1. Download GraphQL schema (required before building)
./gradlew downloadOctopusApolloSchemaFromIntrospection

# 2. Download translations from Lokalise (required before building)
./gradlew downloadStrings

# 3. Build and sync the project
./gradlew build
```

**Prerequisites:**
- `lokalise.properties` file with credentials (from 1Password)
- `~/.gradle/gradle.properties` with GitHub Packages token (PAT with `read:packages` permission)
- See `scripts/ci-prebuild.sh` for reference

### Common Development Commands

```bash
# Build the app
./gradlew :app:assemble

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :feature-home:test

# Run unit tests
./gradlew testDebugUnitTest

# Formatting
./gradlew ktlintCheck          # Check formatting
./gradlew ktlintFormat         # Auto-format files

# Linting
./gradlew lint

# Clean build
./gradlew clean

# Generate module dependency graph (requires graphviz: brew install graphviz)
./gradlew :generateProjectDependencyGraph

# Find unused resources
./gradlew :app:lint -Prur.lint.onlyUnusedResources
./gradlew :app:removeUnusedResourcesDebug
```

## Architecture

### Module Structure

The codebase is organized under `/app` with 80+ modules following a strict modularization pattern:

- **app/** - Main application module. Owns the single Metro `AppGraph`, the single `NavDisplay`, the back stack controller, and all cross-feature navigation wiring.
- **feature/** - Feature modules (feature-home, feature-chat, feature-login, etc.)
- **feature/feature-{name}-navigation/** - Tiny modules holding *only* the public `@Serializable HedvigNavKey`s of a feature that other features may navigate to. These are the one carve-out to the "features can't depend on features" rule.
- **data/** - Data layer modules (data-contract, data-chat, data-addons, etc.)
- **core/** - Core utilities (core-common, core-datastore, core-resources, etc.)
- **apollo/** - GraphQL client modules (apollo-octopus-public, apollo-core, etc.)
- **navigation/** - Navigation infrastructure: `navigation-common` (KMP, holds `HedvigNavKey` + marker interfaces), `navigation-compose` (KMP, the `Backstack` interface, deep-link matching, decorators), `navigation-keys-processor` (KSP processor generating serializer registrations), `navigation-activity` (Android `ExternalNavigator`).
- **design-system/** - Design system components
- **ui/** - Shared UI components
- **auth/** - Authentication modules
- **database/** - Room database modules
- **language/** - Localization modules
- **shareddi/** - KMP module declaring the iOS `IosGraph` (Metro graph for the iOS target).
- **Other utilities** - payment, tracking, logging, featureflags, etc.

**Critical architectural rule:** Feature modules CANNOT depend on other feature modules. This is enforced at build time by `hedvig.gradle.plugin` (`configureFeatureModuleGuidelines()`). The single exception: any module can depend on a `feature-{name}-navigation` module, because those exist precisely to be shared cross-feature.

### Module Naming Conventions

- `{name}-public` - Public APIs and interfaces (often KMP-compatible)
- `{name}-android` - Android-specific implementations
- `{name}-test` - Test utilities
- `feature-{name}-navigation` - Public navigation keys of a feature (cross-feature depend-able)
- No suffix for main implementation modules

If a module is KMP compatible, there is no need for the `-public` or `-android` suffix. The android-specific code lives inside the `androidMain` directory instead (see `:language-core`).

### Build Types

- **Release** (`com.hedvig.app`) - Production builds for Play Store
- **Staging** (`com.hedvig.app`) - Internal testing via Firebase App Distribution (staging backend)
- **Develop** (`com.hedvig.dev.app`) - Development builds (staging backend)

## Key Architectural Patterns

### MVI with Molecule

The app uses Molecule (Cash App's library) for reactive state management. This is unchanged by the Metro/Nav3 migration:

```kotlin
// ViewModels delegate to Presenters
class FeatureViewModel(
  useCase: FeatureUseCase,
) : MoleculeViewModel<FeatureEvent, FeatureUiState>(
    initialState = FeatureUiState.Loading,
    presenter = FeaturePresenter(useCase),
)

// Presenters contain presentation logic
class FeaturePresenter : MoleculePresenter<FeatureEvent, FeatureUiState> {
  @Composable
  override fun MoleculePresenterScope<FeatureEvent>.present(lastState: FeatureUiState): FeatureUiState {
    // Composable state management logic
  }
}
```

**Flow:** User Action → Event → Presenter → UiState → UI

### Dependency Injection (Metro)

Metro is a **compile-time** DI framework. There are two scopes: a global `AppScope` graph for the whole app, plus a per-Activity `ActivityRetainedScope` graph extension (`ActivityRetainedGraph`) created once per `MainActivity` for things that must be 1:1 with one Activity's back stack. Bindings are *contributed* from any module and merged into the graph at compile time.

**Core annotations you will actually use:**

- `@Inject` — constructor (or, on `MainActivity`/Application/Service, field) injection.
- `@SingleIn(AppScope::class)` — an app-wide singleton. Apply to anything that must have exactly one instance for the whole process (stateful services, caches). Put it *wherever the binding is declared*: on the `@Inject` constructor, or on the `@Provides` method when you can't annotate the constructor.
- `@SingleIn(ActivityRetainedScope::class)` — a per-Activity singleton bound in `ActivityRetainedGraph`. Used for things tied to one Activity's back stack (e.g. `SessionReconciler`). The `BackstackController` itself isn't annotated — it's built directly by `NavRetainedViewModel` (a retained `ViewModel`) and passed into the `ActivityRetainedGraph.Factory`, which binds it as `Backstack`. See `docs/architecture/navigation-and-di.md` §I.1 and §II.4.
- `@ContributesBinding(AppScope::class)` — on an implementation class, binds it to its interface in the graph. The standard way to provide an `Impl` for an interface.
- `@Provides` inside a `@ContributesTo(AppScope::class) interface` — for bindings you can't annotate a constructor on (framework types, builders, things needing configuration). See `ApplicationMetroProviders`.
- `@ContributesIntoSet` / `@ContributesIntoMap` — multibindings. Used for sets of `SerializersModule`, deep-link matcher providers, notification senders, and the ViewModel/worker maps.
- `@Multibinds(allowEmpty = true)` — declares a multibound collection on the graph even when no module contributes to it.
- `@AssistedInject` + `@AssistedFactory` — runtime parameters (e.g. a screen's `contractId`) combined with injected dependencies.

**The app graph** is declared once, in `:app`:

```kotlin
@DependencyGraph(AppScope::class)
internal interface AppGraph : ViewModelGraph {
  val workerFactory: MetroWorkerFactory
  @Multibinds(allowEmpty = true)
  val serializersModules: Set<SerializersModule>
  fun inject(activity: MainActivity)
  fun inject(application: HedvigApplication)
  @DependencyGraph.Factory
  interface Factory { fun create(@Provides applicationContext: Context): AppGraph }
}
```

**ViewModels are resolved through Metro, not `viewModel()`:** Inside an `entry<Key> { }` block use:

```kotlin
// No runtime args:
val vm: InsuranceViewModel = metroViewModel()

// With assisted (navigation) args:
val vm: ContractDetailViewModel =
  assistedMetroViewModel<ContractDetailViewModel, ContractDetailViewModel.Factory> {
    create(key.contractId)
  }
```

To register a ViewModel, mark it `@HedvigViewModel(scope)` plus its Metro constructor annotation. The `:viewmodel-processor` KSP processor generates the `@ViewModelKey` / `@ContributesIntoMap` / factory boilerplate — you never hand-write it. Almost every ViewModel is `ActivityRetainedScope` (it can inject the per-Activity `Backstack`):

```kotlin
@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ContractDetailViewModel(
  @Assisted contractId: String,
  useCase: GetContractForContractIdUseCase,
) : MoleculeViewModel<...>(...)
```

A no-arg ViewModel uses `@Inject` + `@HedvigViewModel(ActivityRetainedScope::class)` instead. The rare `@HedvigViewModel(AppScope::class)` case is a ViewModel resolved by its own standalone Activity. The module must opt in with `viewModels()` in its `hedvig {}` block. A `MergedMetroViewModelFactory` (merging the app graph's and this Activity's `ActivityRetainedGraph` maps) is provided into the composition via `LocalMetroViewModelFactory` in `MainActivity` (read off `navRetainedViewModel.viewModelFactory`).

**Demo mode** is the one place we need two implementations of the same type. Use the `Provider<T>` fun interface and a `ProdOrDemoProvider<T>` (always `@SingleIn(AppScope::class)`), which picks `demoImpl` vs `prodImpl` off `DemoManager`. Inject `Provider<T>` and call `.provide()`. Do **not** reach for `Provider<T>` for anything else.

**WorkManager** workers are built through `MetroWorkerFactory`, a multibound `Map<KClass<out ListenableWorker>, ChildWorkerFactory>`. A worker contributes an `@AssistedFactory` `ChildWorkerFactory` keyed with `@WorkerKey`.

**Required Gradle flag:** `metro.generateContributionProviders=true` in `gradle.properties`. The Metro compiler plugin is auto-applied to every module by `hedvig.gradle.plugin` (`configureMetro`), so module `build.gradle.kts` files never apply it manually.

### Navigation (Navigation 3)

There is **one** `NavDisplay`, in `HedvigApp`, rendering a **single back stack** that is a `SnapshotStateList<HedvigNavKey>`. There is no `NavController` and no route strings.

**Destinations are keys.** A destination is a `@Serializable` class/object implementing `HedvigNavKey`:

```kotlin
@Serializable
data object InsurancesKey : HedvigNavKey, CrossSellEligibleDestination, TopLevelTabRoot {
  override val topLevelTab = TopLevelTab.Insurances
}

@Serializable
internal data class InsuranceContractDetailKey(val contractId: String) : HedvigNavKey, DeepLinkAncestry, CrossSellEligibleDestination {
  override val owningTab = TopLevelTab.Insurances
  override val syntheticParents = emptyList<HedvigNavKey>()
}
```

Keys reachable cross-feature live in the feature's `-navigation` module and are public. Keys internal to a feature stay `internal` in the feature module.

**Marker interfaces** (in `navigation-common`) let `:app` reason about a key without depending on the feature:
- `TopLevelTabRoot` — this key is the root of a bottom-nav tab (exposes `topLevelTab`).
- `DeepLinkAncestry` — how to build a synthetic back stack when this key is entered alone (exposes `owningTab` + `syntheticParents`).
- `CrossSellEligibleDestination` — the cross-sell sheet may appear here.
- `SuppressesChatPushNotification` — suppress chat push while this screen is shown.
- `DeliberateLogoutOrigin` — reaching logout from here is intentional; don't stash the session for restore.

**The back stack API.** Presenters and entries receive the `Backstack` interface (the `:app` `BackstackController` is bound to it). `entries` is the source of truth; helpers are extensions:

```kotlin
backstack.add(ChatKey(id))                              // push
backstack.popBackstack()                                 // pop one; at the root it finishes the app (Back/close exits)
backstack.popUpTo<TerminateInsuranceKey>(inclusive = true)
backstack.navigateAndPopUpTo<FooKey>(BarKey, inclusive = true)
backstack.navigateUp()                                   // task-aware up (deep links)
backstack.removeAllOf<InboxKey>()
```

Never hold a long-lived reference to `entries` snapshot contents; mutate through the controller/extensions so changes are observed and persisted.

**Registering destinations.** Each feature exposes a `fun EntryProviderScope<HedvigNavKey>.featureEntries(...)` that calls `entry<Key> { }` for each of its screens. `:app` calls all of them from `hedvigEntryProvider`. Cross-feature navigation is done by `:app` passing `navigateToX` lambdas into each feature's entries function — features never import each other's keys.

```kotlin
fun EntryProviderScope<HedvigNavKey>.insuranceEntries(backstack: Backstack, /* navigateToX lambdas */) {
  entry<InsurancesKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val vm: InsuranceViewModel = metroViewModel()
    InsuranceDestination(viewModel = vm, /* ... */)
  }
}
```

**Process-death survival is automatic but requires opt-in per module.** Add `navKeys()` to the module's `hedvig { }` block. The `navigation-keys-processor` KSP processor finds every concrete `@Serializable HedvigNavKey` in the module and generates a Metro `@ContributesIntoSet SerializersModule` provider registering them polymorphically. `:app` merges all contributed modules and uses them to (de)serialize the back stack into the Activity's `SavedStateRegistry`. **If you add a key but forget `navKeys()`, the app will crash on restore** with a missing polymorphic serializer.

**Multiple back stacks (tabs)** are handled by the "runs model" in `BackstackController`/`TopLevelRunLogic`: Home's run is always at the base of `entries`; side tabs are parked in `parkedRuns` when you switch away and restored when you switch back. Tab state (saveable state + ViewModels) of parked runs is kept alive by the retained `NavEntryDecorator`s, which consult `allLiveContentKeys`.

**Where the heavy logic lives** (read these, don't reinvent):
- `BackstackController.kt` — per-Activity controller (owned by `NavRetainedViewModel`, a retained `ViewModel`; survives config changes, dies with its Activity), owns all nav state, tab switching, login/logout stash, deep-link routing, task-aware Up.
- `NavigationStateBridge.kt` — the single seam between Activity lifecycle and the controller (seed/restore/persist + escape-to-own-task handoff).
- `SessionReconciler.kt` — auth↔back-stack reconciliation; gates the splash via `isReady`; forced logout.
- `HedvigEntryProvider.kt` — all destination registration and cross-feature lambda wiring.

### Deep Links

Each feature builds `DeepLinkMatcher`s from its `HedvigDeepLinkContainer` patterns and contributes a `DeepLinkMatcherProvider` (`@ContributesIntoSet`). `:app` aggregates them into one `HedvigDeepLinkMatcher`. `MainActivity` forwards `ACTION_VIEW` intents as raw URI strings down a `deepLinkChannel`; `HedvigApp` matches each to a key and routes it through the controller once logged in. A `DeepLinkAncestry` key entered while logged out is held as `pendingDeepLink` and landed after login.

### Data Layer

Data modules follow this structure when they are not KMP compatible:

```
data-{domain}/
├── data-{domain}-public/      # Interfaces/models
└── data-{domain}-android/     # Android implementation (optional)
```

And this structure when they are KMP compatible:

```
data-{domain}/
└── data-{domain}/             # Interfaces/models (KMP), androidMain for android-specific code
```

**Patterns:**
- Repository pattern with interfaces, bound via `@ContributesBinding(AppScope::class)`.
- Apollo GraphQL queries/mutations.
- Use cases for business logic, injected directly as typed dependencies.
- Room database for local persistence.

When a Presenter or ViewModel needs a use case, inject it directly as a typed dependency — never abstract it into an anonymous `suspend () -> T` lambda. If two separate operations are needed (e.g. payin vs payout setup), create two separate, dedicated use case classes and two separate presenters. Do not create a shared interface just to enable reuse through a single presenter.

**Critical architectural rule — never expose GraphQL types in public API:**

GraphQL is an implementation detail of the data layer. Apollo-generated types (anything from the `octopus` package — queries, mutations, fragments, their `.Data` shapes, generated input/enum types, etc.) **must not appear in the signatures of public interfaces, public functions, return types, or public data classes** that other modules consume.

Use cases and repositories should:
1. Run the GraphQL operation internally (`.query(...)`, `.mutation(...)`, `.safeExecute()`, `.safeFlow()`).
2. Map the response into a project-owned type (a plain Kotlin `data class`, sealed type, primitive, or `Unit` if only success/failure matters) before returning.
3. Keep the `octopus.*` import confined to the `internal` impl class only.

This applies even when the GraphQL type happens to be a perfect shape — wrap it. It keeps the rest of the project insulated from schema churn, makes the data source swappable, and prevents GraphQL types from leaking into KMP/iOS-facing APIs.

Example — wrong:
```kotlin
interface SetArticleRatingUseCase {
  // ❌ exposes Apollo-generated type
  suspend fun invoke(name: String, rating: Int): Either<ErrorMessage, PuppyGuideEngagementMutation.Data>
}
```

Example — right:
```kotlin
interface SetArticleRatingUseCase {
  // ✅ project-owned shape; Unit because callers only care about success/failure
  suspend fun invoke(name: String, rating: Int): Either<ErrorMessage, Unit>
}

internal class SetArticleRatingUseCaseImpl(...) : SetArticleRatingUseCase {
  override suspend fun invoke(...) = either {
    val data = apolloClient.mutation(PuppyGuideEngagementMutation(...)).safeExecute()
      .mapLeft { ErrorMessage() }.bind()
    ensure(data.puppyGuideEngagement.success) { ErrorMessage() }
  }
}
```

## Technology Stack

### UI
- **Jetpack Compose** - 100% Compose, no XML layouts
- **Navigation 3** (`androidx.navigation3`) - single `NavDisplay` over an app-owned back stack
- **Material 3** - Window size classes, theming. Only used internally by our design-system-internals
- **Coil** - Image loading (SVG, GIF, PDF support)
- **ExoPlayer** (Media3) - Video playback

### Networking & Data
- **Apollo GraphQL** (v4.x) - Primary data source (Octopus backend)
  - Normalized caching with `MemoryCacheFactory`
  - Response-based code generation
  - Client-side schema modifications
- **Ktor Client** - HTTP client with custom interceptors
- **Room Database** - Local persistence

### Async & Reactive
- **Kotlin Coroutines** - Asynchronous programming
- **Kotlin Flow** - Reactive streams
- **Molecule** - Reactive state management
- **Arrow** - Functional programming utilities (Either, raceN, etc.)

### Dependency Injection
- **Metro** (`dev.zacsweers.metro`) - compile-time DI, single `AppScope` graph
- **metro-viewmodel / metro-viewmodel-compose** - ViewModel resolution on Android

### Other
- **kotlinx.serialization** - JSON serialization, polymorphic back-stack persistence
- **Timber** - Logging
- **Datadog** - Analytics and RUM
- **Firebase** - Crashlytics, Analytics, Messaging
- **Kotlin Multiplatform** - Many modules support KMP

## Build Configuration

### Convention Plugins (in build-logic/)

The project uses custom Gradle convention plugins for consistent configuration:

- **hedvig.gradle.plugin** - Base plugin with:
  - Feature module dependency enforcement (features can't depend on features, except `-navigation` modules)
  - Auto-application of the **Metro** compiler plugin to every Kotlin module
  - Auto-addition of metro-viewmodel deps to Android modules
  - Ktlint configuration
  - Common dependencies (Compose BOM, logging, tracking auto-injected)

- **hedvig.android.application** - Android app configuration
- **hedvig.android.library** - Android library configuration
- **hedvig.jvm.library** - Pure Kotlin (JVM) libraries
- **hedvig.multiplatform.library** - KMP support
- **hedvig.multiplatform.library.android** - adds an android target to a KMP module when it needs android-specific code

### HedvigGradlePluginExtension DSL

Use this in module `build.gradle.kts` files:

```kotlin
plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")     // Enable Apollo codegen with the given generated package
  compose()             // Enable Jetpack Compose
  serialization()       // Enable kotlinx.serialization
  androidResources()    // Enable Android resources
  room(false) { ... }   // Enable Room database
  navKeys()             // Wire the nav-keys KSP processor (REQUIRED if the module declares HedvigNavKeys)
}

dependencies {
  implementation(projects.coreCommonPublic)
  implementation(projects.navigationCompose)
  implementation(projects.designSystemHedvig)
}
```

## Code Style

### Formatting (ktlint)

Configuration in `.editorconfig`:
- Code style: `ktlint_official`
- Indent: 2 spaces
- Max line length: 120 characters
- Trailing commas: enabled
- No wildcard imports
- Function naming: Composables exempted from normal rules

**Always run `./gradlew ktlintFormat` before committing.**

### Naming Conventions

- **Composable functions:** PascalCase (e.g., `FeatureScreen()`)
- **Regular functions:** camelCase
- **ViewModels:** `{Feature}ViewModel`
- **Presenters:** `{Feature}Presenter`
- **Destinations / nav keys:** `{Feature}Key` (e.g. `InsurancesKey`, `ChatKey`)
- **Entry functions:** `{feature}Entries`
- **Use cases:** `{Action}{Domain}UseCase` (e.g., `GetHomeDataUseCase`)

## Working with GraphQL

### Apollo Schema

```bash
# Download schema from backend
./gradlew downloadOctopusApolloSchemaFromIntrospection

# Generate GraphQL code (happens automatically on build)
./gradlew :feature-{name}:generateApolloSources
```

**Schema locations:**
- `app/apollo/apollo-octopus-public/src/main/graphql/` - Main schema
- Client-side schema modifications supported via build plugin

### Writing GraphQL Queries

Place `.graphql` files in module's `src/main/graphql/`. Apollo generates type-safe Kotlin code automatically. Keep generated `octopus.*` types confined to internal impl classes (see the data layer rule above).

## Testing

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :feature-home:test
./gradlew :data-contract:test

# Run unit tests only
./gradlew testDebugUnitTest
```

**Test patterns:**
- Unit tests: `src/test/kotlin/` (or `src/commonTest/` for KMP)
- Android tests: `src/androidTest/kotlin/`
- Use Turbine for testing Flows
- Use test modules for shared test utilities
- Navigation invariants are covered by `ExhaustiveBackStackSerializationTest` (every `HedvigNavKey` round-trips through serialization) and `BackstackTest`. If you add a key, these guard process-death survival.

## CI/CD

GitHub Actions workflows (in `.github/workflows/`):
- **pr.yml** - PR checks (lint, test, build)
- **staging.yml** - Staging builds
- **upload-to-play-store.yml** - Production releases
- **graphql-schema.yml** - Schema updates
- **strings.yml** - Translation updates
- **unused-resources.yml** - Resource cleanup checks
- **umbrella.yml** - Comprehensive checks

## Important Files

- **build-logic/convention/** - Gradle convention plugins (Metro wiring, feature isolation, the `hedvig {}` DSL)
- **app/app/.../di/AppGraph.kt** - the global Metro graph; **app/app/.../di/ActivityRetainedGraph.kt** - the per-Activity graph extension (built by **app/app/.../navigation/NavRetainedViewModel.kt**)
- **app/app/.../navigation/BackstackController.kt** - the per-Activity source of navigation truth
- **app/navigation/** - navigation infrastructure + KSP processor
- **docs/architecture/navigation-and-di.md** - the deep design spec for navigation + DI
- **settings.gradle.kts** - Module discovery and configuration
- **gradle.properties** - Project properties (`metro.generateContributionProviders=true` lives here)
- **.editorconfig** - Code style configuration

## Common Tasks

### Adding a New Feature Module

1. Create directory: `app/feature/feature-{name}/`
2. Add `build.gradle.kts`:
```kotlin
plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  navKeys()          // if the module declares any HedvigNavKey
  apollo("octopus")  // if needed
}

dependencies {
  implementation(projects.coreCommonPublic)
  implementation(projects.navigationCompose)
  implementation(projects.designSystemHedvig)
}
```
3. Create standard structure: `ui/`, `navigation/`, `di/` (contributions live next to the classes they bind via Metro annotations, not in a central `di` module).
4. Define `@Serializable` `HedvigNavKey`s. Put any cross-feature-reachable keys in a `feature-{name}-navigation` module.
5. Expose a `fun EntryProviderScope<HedvigNavKey>.{name}Entries(...)` and call it from `HedvigEntryProvider` in `:app`.
6. Module will be auto-discovered by `settings.gradle.kts`.

### Adding a New Screen/Destination

1. Define `@Serializable {Name}Key : HedvigNavKey` (add marker interfaces as needed).
2. Register it: `entry<{Name}Key> { key -> ... }` in the feature's entries function.
3. Resolve the ViewModel with `metroViewModel()` / `assistedMetroViewModel(...)`.
4. Ensure the module has `navKeys()` so the key survives process death.
5. For cross-feature entry, thread a `navigateToX` lambda from `:app` rather than importing the key.

### Adding a New GraphQL Query

1. Create `.graphql` file in `src/main/graphql/`.
2. Enable Apollo in `build.gradle.kts`: `hedvig { apollo("octopus") }`.
3. Build generates type-safe Kotlin code.
4. Use the generated query in an internal repository/use case impl; return a project-owned type.

### Working with Translations

```bash
# Download latest translations
./gradlew downloadStrings
```

**IMPORTANT:** String resource XML files (`strings.xml`) are fully managed by Lokalise and regenerated on every `./gradlew downloadStrings` run. **Never add new strings directly to any `strings.xml` file** — they will be overwritten and lost.

When new UI text is needed that does not yet exist as a string resource:
1. Hardcode the English string directly in the Kotlin/Compose code.
2. Add a `// TODO: Add "<English text>" / "<Swedish text>" to Lokalise` comment on the same line or the line above.

Example:
```kotlin
// TODO: Add "This is some text for feature X" / "Detta är lite text för feature X" to Lokalise
Text("This is some text for feature X")
```

## Debugging

### Common Issues

**Build fails with "Cannot find schema":**
```bash
./gradlew downloadOctopusApolloSchemaFromIntrospection
```

**Missing translations:**
```bash
./gradlew downloadStrings
```

**App crashes on process-death restore / "polymorphic serializer not found":**
- The module declaring the key is missing `navKeys()` in its `hedvig {}` block, or the key isn't `@Serializable`.

**Metro "cannot find binding" / duplicate binding errors:**
- Check the type is contributed (`@ContributesBinding`/`@Provides`/`@ContributesIntoMap`) into `AppScope`.
- Confirm `metro.generateContributionProviders=true` is present in `gradle.properties`.

**Dependency resolution failures:**
- Check `~/.gradle/gradle.properties` has GitHub PAT with `read:packages`.

**Ktlint formatting errors:**
```bash
./gradlew ktlintFormat
```

## Module Discovery

Modules are auto-discovered via `settings.gradle.kts`:
- All directories under `app/` with `build.gradle.kts` are included
- Micro-apps under `micro-apps/` are manually included
- No need to manually register new modules

## Performance

- **Build cache** enabled via Gradle Develocity
- **Configuration cache** enabled (incubating)
- **Type-safe project accessors** for faster builds
- **Parallel builds** supported
- **Dependency analysis** plugin monitors dependency health
