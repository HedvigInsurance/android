package com.hedvig.android

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

internal fun KotlinCommonCompilerOptions.configureKotlinCompilerOptions(project: Project) {
  when (this) {
    is KotlinJvmCompilerOptions -> {
      configureCommonKotlinCompilerOptions(project, listOf("-Xjvm-default=all"))
      jvmTarget.set(JvmTarget.JVM_17)
    }
    else -> {
      configureCommonKotlinCompilerOptions(project)
    }
  }
}

private fun KotlinCommonCompilerOptions.configureCommonKotlinCompilerOptions(
  project: Project,
  extraFreeCompilerArgs: List<String> = emptyList(),
) {
  apiVersion.set(KotlinVersion.KOTLIN_1_9)
  languageVersion.set(KotlinVersion.KOTLIN_1_9)
  freeCompilerArgs.addAll(project.commonFreeCompilerArgs().plus(extraFreeCompilerArgs))
}

private fun Project.commonFreeCompilerArgs(): List<String> {
  return buildList {
    addAll(
      listOf(
        "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
        "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
        "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
        "-opt-in=kotlin.Experimental",
        "-opt-in=kotlin.RequiresOptIn",
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=kotlinx.coroutines.FlowPreview",
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
      ),
    )

    // Get compose metrics with `./gradlew :app:assembleRelease -Pcom.hedvig.app.enableComposeCompilerReports=true`
    if (project.findProperty("com.hedvig.app.enableComposeCompilerReports") == "true") {
      addAll(
        listOf(
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
            project.layout.buildDirectory.asFile.get().absolutePath + "/compose_metrics",
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
            project.layout.buildDirectory.asFile.get().absolutePath + "/compose_metrics",
        ),
      )
    }
  }
}
