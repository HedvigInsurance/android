// Precompiled settings convention plugin. Applied from the root settings.gradle.kts as
// `id("hedvig.settings")`.
//
// Applying a build-logic plugin here puts build-logic's whole plugin classpath (see build.gradle.kts)
// on the settings classloader, which is the parent of every project's classloader. That is what lets
// project scripts apply plugins by id without a version, so plugin versions live only in
// gradle/libs.versions.toml. A settings `plugins {}` block cannot do this itself: it has no
// version-catalog accessors and accepts only literals, which would mean duplicating every version.
//
// Only plugins that operate on Settings belong in the block below; everything else is applied by the
// projects that need it. DeDebug is one of them: it removes the `debug` build type from every Android
// library, and it needs AGP on this classloader to do so.
plugins {
  id("com.gradle.develocity")
  id("com.autonomousapps.dedebug")
}
