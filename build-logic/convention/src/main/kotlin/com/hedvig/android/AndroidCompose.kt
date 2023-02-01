package com.hedvig.android

import com.android.build.api.dsl.CommonExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(
  commonExtension: CommonExtension<*, *, *, *>,
) {
  val libs = the<LibrariesForLibs>()

  commonExtension.apply {
    @Suppress("UnstableApiUsage")
    buildFeatures {
      compose = true
    }

    @Suppress("UnstableApiUsage")
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
