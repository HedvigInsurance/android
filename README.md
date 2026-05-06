# Hedvig app for Android

## Develop

1. Acquire Lokalise credentials (you can find them in 1Password), place in the following file:
    - `lokalise.properties`
2. Acquire gradle.properties which contain a token for Github Packages authentication. 
   Generate your own at GitHub > Settings > Developer Settings > PAT > Tokens (Classic) > Generate New Token > Give the read:packages permission    
   Append (or create) your global gradle.properties in:
    - `~/.gradle/gradle.properties`
   Look inside [ci-prebuild](scripts/ci-prebuild.sh) for inspiration.
3. Download the schema (required to consume any changes in schema as well):
    `./gradlew downloadOctopusApolloSchemaFromIntrospection`
4. Download lokalise translations (required to consume latest translations as well):
    `./gradlew downloadStrings`
5. Build and install via Android Studio

## Formatting

Formatting is handled with ktlint with extra configuration defined in [`.editorconfig`](.editorconfig)
run `./gradlew ktlintCheck` to check that the files follow the rules
run `./gradlew ktlintFormat` to make ktlint to format all files according to the rules 

## Build Types

* Release: `com.hedvig.app` Build for the customer on Play Store. Using production backend
* Staging: `com.hedvig.app` Build for internal testing using Firebase App Distribution. Using staging backend
* Develop: `com.hedvig.dev.app` Build for development. Using staging backend

## Module structure

![Module graph](misc/images/modularization-graph.png "Image showing the module dependencies graph")

Generated from `./gradlew :generateProjectDependencyGraph`*\
*Note that this requires `dot` from graphviz to be on your path. Run `brew install graphviz`

## Renovate

`renovate.json` is a file which Renovate looks at in order to be able to suggest upgrading private artifacts to their latest version.
The `username` used in there is the result of passing our jitpack username (from 1Password) through Renovate's [website](https://app.renovatebot.com/encrypt) to get the hashed version of it.

## Removing unused resources

The [android-remove-unused-resources-plugin](https://github.com/irgaly/android-remove-unused-resources-plugin/tree/main#usage)
plugin is used to achieve this. This will run on CI using [this task](./.github/workflows/unused-resources.yml), but to run locally one can also just do
`./gradlew :app:lint -Prur.lint.onlyUnusedResources`
And then
`./gradlew :app:removeUnusedResourcesDebug`

## Sharing code with iOS via HedvigShared

The `:umbrella` module produces the binary that the iOS app (Ugglan) consumes for shared KMP code — see `app/umbrella/build.gradle.kts` for the exported module list. There are two distribution paths:

- **Production**: `assembleHedvigSharedReleaseXCFramework` builds a multi-slice `HedvigShared.xcframework` (iosArm64 + iosSimulatorArm64) and the `umbrella.yml` workflow publishes it as a Swift Package. Slow round-trip (~25 min in CI) but produces the artifact CI consumes.
- **Local dev** (iOS-side fast loop): `embedAndSignAppleFrameworkForXcode` builds *only* the slice Xcode is currently asking for. Invoked by Ugglan as a pre-build phase. ~5–10s per Kotlin change.

Check out the android repo as a sibling of `ugglan/`:

```
<parent>/
├── android/   ← this repo
└── ugglan/
```

From `ugglan/`, run `scripts/use-local-umbrella.sh` to enable local mode. See the *Iterating on shared KMP code* section in Ugglan's README for the full flow.

To produce the XCFramework manually from this repo (the artifact CI publishes):

```sh
./gradlew :umbrella:assembleHedvigSharedReleaseXCFramework
```

Output: `app/umbrella/build/XCFrameworks/release/HedvigShared.xcframework`. This is what `umbrella.yml` runs in CI.

`:umbrella:embedAndSignAppleFrameworkForXcode` exists too but it's not a manual command — it expects Xcode-set env vars (`CONFIGURATION`, `SDK_NAME`, `ARCHS`, `BUILT_PRODUCTS_DIR`, …) and is only useful when invoked from an Xcode build phase. That's how Ugglan's local mode runs it.

### Heads-up for KMP/Compose contributors
the umbrella module is `isStatic = true`, and Compose Multiplatform's iOS resource reader uses `Bundle.main` to find resources at `<App>.app/compose-resources/composeResources/...`. In Ugglan's multi-target Tuist setup, the resources end up bundled inside `CoreDependencies.framework` instead of at the app bundle root, so `ugglan/scripts/post-build-action.sh` lifts them out at post-build time. If you add new compose resources to a shared module, that path is what serves them at runtime.