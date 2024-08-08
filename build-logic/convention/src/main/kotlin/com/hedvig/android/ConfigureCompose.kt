package com.hedvig.android

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.assign
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun ComposeCompilerGradlePluginExtension.configureComposeCompiler(project: Project) {
  with(project) {
    fun Provider<String>.onlyIfTrue() = flatMap { provider { it.takeIf(String::toBoolean) } }

    fun Provider<*>.relativeToRootProject(dir: String) = flatMap {
      rootProject.layout.buildDirectory.dir(projectDir.toRelativeString(rootDir))
    }.map { it.dir(dir) }

    // Get compose metrics with `./gradlew :app:assembleRelease -Pcom.hedvig.app.enableComposeCompilerReports=true`
    project.providers.gradleProperty("com.hedvig.app.enableComposeCompilerReports")
      .onlyIfTrue()
      .relativeToRootProject("compose-metrics")
      .let(metricsDestination::set)

    project.providers.gradleProperty("com.hedvig.app.enableComposeCompilerReports").onlyIfTrue()
      .relativeToRootProject("compose-reports")
      .let(reportsDestination::set)
  }
}
