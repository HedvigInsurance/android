# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Hedvig Android app - A modern Android application built with Jetpack Compose, Apollo GraphQL, and Kotlin. The app uses a highly modular architecture with 80+ modules organized into feature, data, and core layers.

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

- **app/** - Main application module
- **feature/** (29 modules) - Feature modules (feature-home, feature-chat, feature-login, etc.)
- **data/** (18 modules) - Data layer modules (data-contract, data-chat, data-addons, etc.)
- **core/** (18 modules) - Core utilities (core-common, core-datastore, core-resources, etc.)
- **apollo/** (14 modules) - GraphQL client modules (apollo-octopus-public, apollo-core, etc.)
- **navigation/** (6 modules) - Navigation modules (navigation-compose, navigation-core, etc.)
- **design-system/** (3 modules) - Design system components
- **ui/** - Shared UI components
- **auth/** - Authentication modules
- **database/** - Room database modules
- **language/** - Localization modules
- **Other utilities** - payment, tracking, logging, featureflags, etc.

**Critical architectural rule:** Feature modules CANNOT depend on other feature modules. This is enforced at build time by the `hedvig.gradle.plugin`.

### Module Naming Conventions

- `{name}-public` - Public APIs and interfaces (often KMP-compatible)
- `{name}-android` - Android-specific implementations
- `{name}-test` - Test utilities
- No suffix for main implementation modules

### Build Types

- **Release** (`com.hedvig.app`) - Production builds for Play Store
- **Staging** (`com.hedvig.app`) - Internal testing via Firebase App Distribution (staging backend)
- **Develop** (`com.hedvig.dev.app`) - Development builds (staging backend)

## Key Architectural Patterns

### MVI with Molecule

The app uses Molecule (Cash App's library) for reactive state management:

```kotlin
// ViewModels delegate to Presenters
class FeatureViewModel(
  useCaseProvider: Provider<UseCase>,
) : MoleculeViewModel<FeatureEvent, FeatureUiState>(
    FeatureUiState.Loading,
    FeaturePresenter(/* ... */),
)

// Presenters contain presentation logic
class FeaturePresenter : MoleculePresenter<FeatureEvent, FeatureUiState> {
  @Composable
  override fun present(events: Flow<FeatureEvent>): FeatureUiState {
    // Composable state management logic
  }
}
```

**Flow:** User Action → Event → Presenter → UiState → UI

### Feature Module Pattern

Each feature module follows this structure:

```
feature-{name}/
├── build.gradle.kts
└── src/main/kotlin/com/hedvig/android/feature/{name}/
    ├── ui/
    │   ├── {Name}Destination.kt    # Composable entry point
    │   ├── {Name}ViewModel.kt      # MoleculeViewModel
    │   ├── {Name}Presenter.kt      # MoleculePresenter
    │   └── {Name}Layout.kt         # UI components
    ├── navigation/
    │   └── {Name}Graph.kt          # Navigation setup
    └── di/
        └── {Name}Module.kt         # Koin DI module
```

### Navigation

Uses type-safe Navigation Compose with custom extensions:

```kotlin
// Destinations are serializable sealed interfaces
sealed interface FeatureDestination : Destination {
  @Serializable
  data object Graph : FeatureDestination

  @Serializable
  data class Detail(val id: String) : FeatureDestination
}

// Navigation graphs
fun NavGraphBuilder.featureGraph(
  navigator: Navigator,
) {
  navgraph<FeatureDestination.Graph> {
    navdestination<FeatureDestination.Detail> { backStackEntry ->
      // Composable UI
    }
  }
}
```

**Top-level navigation graphs:** Home, Insurances, Forever, Payments, Profile

### Dependency Injection

Uses Koin with modular configuration:

```kotlin
// Each module has its own DI module
val featureModule = module {
  viewModel { FeatureViewModel(get()) }
  single { FeatureUseCase(get(), get()) }
}

// All modules are included in ApplicationModule
val applicationModule = module {
  includes(
    featureModule,
    dataModule,
    networkModule,
    // ... 40+ modules
  )
}
```

**Patterns:**
- Use `Provider<T>` for lazy initialization
- Each feature/data module has its own DI module
- Common dependencies (logging, tracking) auto-injected by build plugin

### Data Layer

Data modules follow this structure:

```
data-{domain}/
├── data-{domain}-public/      # Interfaces/models (KMP-ready)
│   └── src/commonMain/
└── data-{domain}-android/     # Android implementation (optional)
```

**Patterns:**
- Repository pattern with interfaces in `-public` modules
- Apollo GraphQL queries/mutations
- Use cases for business logic
- Room database for local persistence

## Technology Stack

### UI
- **Jetpack Compose** - 100% Compose, no XML layouts
- **Material 3** - Window size classes, theming
- **Coil** - Image loading (SVG, GIF, PDF support)
- **ExoPlayer** (Media3) - Video playback

### Networking & Data
- **Apollo GraphQL** (v4.x) - Primary data source (Octopus backend)
  - Normalized caching with `MemoryCacheFactory`
  - Response-based code generation
  - Client-side schema modifications
- **OkHttp** - HTTP client with custom interceptors
- **Ktor Client** - Alternative HTTP client
- **Room Database** - Local persistence

### Async & Reactive
- **Kotlin Coroutines** - Asynchronous programming
- **Kotlin Flow** - Reactive streams
- **Molecule** - Reactive state management
- **Arrow** - Functional programming utilities (Either, etc.)

### Other
- **Koin** - Dependency injection (with BOMs)
- **kotlinx.serialization** - JSON serialization
- **Timber** - Logging
- **Datadog** - Analytics and RUM
- **Firebase** - Crashlytics, Analytics, Messaging
- **Kotlin Multiplatform** - Many modules support KMP

## Build Configuration

### Convention Plugins (in build-logic/)

The project uses custom Gradle convention plugins for consistent configuration:

- **hedvig.gradle.plugin** - Base plugin with:
  - Feature module dependency enforcement (features can't depend on features)
  - Ktlint configuration
  - Common dependencies (Koin BOM, Compose BOM, OkHttp BOM)
  - Auto-injection of logging and tracking

- **hedvig.android.application** - Android app configuration
- **hedvig.android.library** - Android library configuration
- **hedvig.jvm.library** - Pure Kotlin (JVM) libraries
- **hedvig.multiplatform.library** - KMP support

### HedvigGradlePluginExtension DSL

Use this in module `build.gradle.kts` files:

```kotlin
plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")              // Enable Apollo codegen
  compose()                       // Enable Jetpack Compose
  serialization()                 // Enable kotlinx.serialization
  androidResources()              // Enable Android resources
  room(false) { /* config */ }    // Enable Room database
}

dependencies {
  // Use type-safe project accessors
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
- **Destinations:** `{Feature}Destination`
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

Place `.graphql` files in module's `src/main/graphql/`:

```graphql
# GetHomeData.graphql
query GetHomeData {
  currentMember {
    id
    firstName
    lastName
  }
}
```

Apollo generates type-safe Kotlin code automatically.

## Testing

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :feature-home:test
./gradlew :data-contract:test

# Run unit tests only
./gradlew testDebugUnitTest

# Run with coverage (if configured)
./gradlew testDebugUnitTestCoverage
```

**Test patterns:**
- Unit tests: `src/test/kotlin/`
- Android tests: `src/androidTest/kotlin/`
- Use Turbine for testing Flows
- Use test modules for shared test utilities

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

- **build-logic/convention/** - Gradle convention plugins
- **settings.gradle.kts** - Module discovery and configuration
- **gradle.properties** - Project properties
- **lokalise.properties** - Translation service credentials
- **.editorconfig** - Code style configuration
- **renovate.json** - Dependency update configuration

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
  apollo("octopus")  // if needed
}

dependencies {
  implementation(projects.coreCommonPublic)
  implementation(projects.navigationCompose)
  implementation(projects.designSystemHedvig)
}
```
3. Create standard structure: `ui/`, `navigation/`, `di/`
4. Module will be auto-discovered by `settings.gradle.kts`

### Adding a New Data Module

1. Create `-public` module for interfaces (KMP-compatible)
2. Create `-android` module for implementations (if needed)
3. Add Koin module in `di/`
4. Use Repository pattern for data access

### Adding a New GraphQL Query

1. Create `.graphql` file in `src/main/graphql/`
2. Enable Apollo in `build.gradle.kts`: `hedvig { apollo("octopus") }`
3. Build generates type-safe Kotlin code
4. Use generated query class in repository/use case

### Working with Translations

```bash
# Download latest translations
./gradlew downloadStrings

# Translations are managed via Lokalise
# String resources in app/core/core-resources/
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

**Dependency resolution failures:**
- Check `~/.gradle/gradle.properties` has GitHub PAT with `read:packages`
- See `scripts/ci-prebuild.sh` for required format

**Ktlint formatting errors:**
```bash
./gradlew ktlintFormat  # Auto-fix
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