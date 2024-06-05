package com.hedvig.android

import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun ComposeCompilerGradlePluginExtension.configureComposeCompiler(project: Project) {
  enableStrongSkippingMode = true

  // Get compose metrics with `./gradlew :app:assembleRelease -Pcom.hedvig.app.enableComposeCompilerReports=true`
  if (project.findProperty("com.hedvig.app.enableComposeCompilerReports") == "true") {
    metricsDestination.set(project.layout.buildDirectory.dir("/compose_metrics"))
    reportsDestination.set(project.layout.buildDirectory.dir("/compose_reports"))
  }
}
