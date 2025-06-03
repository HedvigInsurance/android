# Convention Plugins

The `build-logic` folder defines project-specific convention plugins, used to keep a single
source of truth for common module configurations.

This approach is heavily inspired from
[https://github.com/android/nowinandroid](https://github.com/android/nowinandroid)
which in turn is heavily based on
[https://developer.squareup.com/blog/herding-elephants/](https://developer.squareup.com/blog/herding-elephants/)
and [https://github.com/jjohannes/idiomatic-gradle](https://github.com/jjohannes/idiomatic-gradle).

By setting up convention plugins in `build-logic`, we can avoid duplicated build script setup,
messy `subproject` configurations, without the pitfalls of the `buildSrc` directory.

`build-logic` is an included build, as configured in the root
[`settings.gradle.kts`](../settings.gradle.kts).

Inside `build-logic` is a `convention` module, which defines a set of plugins that all normal
modules can use to configure themselves.

`build-logic` also includes a set of `Kotlin` files used to share logic between plugins themselves,
which is most useful for configuring Android components (libraries vs applications) with shared
code.

These plugins are *additive* and *composable*, and try to only accomplish a single responsibility.
Modules can then pick and choose the configurations they need.
If there is one-off logic for a module without shared code, it's preferable to define that directly
in the module's `build.gradle`, as opposed to creating a convention plugin with module-specific
setup.

Current list of convention plugins:
- [`hedvig.gradle.plugin`](convention/src/main/kotlin/HedvigGradlePlugin.kt)
  The base hedvig gradle plugin, enforcing module relationship rules, applying other relevant 
  plugins, allowing modules to opt-in into features like serialization, enabling compose etc.
- [`hedvig.android.apollo`](convention/src/main/kotlin/ApolloConventionPlugin.kt),
  Setup apollo compiler and common tasks, like downloading the schema or editing the downloaded schema to not contain double white-spaces.
- [`hedvig.android.ktlint`](convention/src/main/kotlin/KtlintConventionPlugin.kt),
  Setup ktlint plugin looking at configuration coming from [`.editorconfig`](../.editorconfig)
- [`hedvig.android.application`](convention/src/main/kotlin/ApplicationConventionPlugin.kt),
  [`hedvig.android.library`](convention/src/main/kotlin/LibraryConventionPlugin.kt),
  Configures common Android and Kotlin options.
- [`hedvig.kotlin.library`](convention/src/main/kotlin/KotlinLibraryConventionPlugin.kt),
  Configures pure Kotlin options without any Android references.
- [`hedvig.multiplatform.library`](convention/src/main/kotlin/KotlinMultiplatformLibraryConventionPlugin.kt),
  Configures Kotlin multiplatform options without with android + TODO TARGETS configured.
- [`hedvig.android.application.compose`](convention/src/main/kotlin/ApplicationComposeConventionPlugin.kt),
  [`hedvig.android.library.compose`](convention/src/main/kotlin/LibraryComposeConventionPlugin.kt):
  Configures Jetpack Compose options
