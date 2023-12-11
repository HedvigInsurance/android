package com.hedvig.android

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(commonExtension: AndroidCommonExtension) {
  val libs = the<LibrariesForLibs>()

  commonExtension.apply {
    buildFeatures {
      compose = true
    }

    composeOptions {
      kotlinCompilerExtensionVersion = libs.versions.androidx.composeCompiler.get()
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
