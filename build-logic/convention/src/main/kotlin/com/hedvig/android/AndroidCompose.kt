package com.hedvig.android

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(commonExtension: AndroidCommonExtension) {
  val libs = the<LibrariesForLibs>()

  commonExtension.apply {
    with(pluginManager) {
      apply(libs.plugins.composeCompilerGradlePlugin.get().pluginId)
    }
    buildFeatures {
      compose = true
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
      configureComposeCompiler(this@configureAndroidCompose)
    }
    dependencies {
      val bom = libs.androidx.compose.bom
      add("implementation", platform(bom))
      add("androidTestImplementation", platform(bom))

      add("implementation", libs.androidx.compose.uiToolingPreview)
      add("debugImplementation", libs.androidx.compose.uiTooling)
    }
  }
}
