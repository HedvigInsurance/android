package com.hedvig.android

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(
  commonExtension: CommonExtension<*, *, *, *>,
) {
  val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

  commonExtension.apply {
    @Suppress("UnstableApiUsage")
    buildFeatures {
      compose = true
    }

    @Suppress("UnstableApiUsage")
    composeOptions {
      kotlinCompilerExtensionVersion = libs.composeCompilerVersion
    }
  }
}
