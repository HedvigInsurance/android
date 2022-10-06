package com.hedvig.android

import com.android.build.api.dsl.CommonExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
  commonExtension: CommonExtension<*, *, *, *>,
) {
  val libs = the<LibrariesForLibs>()

  commonExtension.apply {
    @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
      @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
      minSdk = libs.versions.minSdkVersion.get().toInt()
    }

    buildTypes {
      create("staging")
    }

    compileOptions {
      @Suppress("UnstableApiUsage")
      isCoreLibraryDesugaringEnabled = true
      @Suppress("UnstableApiUsage")
      sourceCompatibility = JavaVersion.VERSION_11
      @Suppress("UnstableApiUsage")
      targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
      // Treat all Kotlin warnings as errors (disabled by default)
      allWarningsAsErrors = properties["warningsAsErrors"] as? Boolean ?: false

      freeCompilerArgs = freeCompilerArgs + listOf(
        "-opt-in=kotlin.RequiresOptIn",
        // Enable experimental coroutines APIs, including Flow
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=kotlinx.coroutines.FlowPreview",
        "-opt-in=kotlin.Experimental",
        // Enable experimental kotlinx serialization APIs
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        // Fixes "Inheritance from an interface with '@JvmDefault' members is only allowed with -Xjvm-default option"
        "-Xjvm-default=enable",
      )

      @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
      jvmTarget = JavaVersion.VERSION_11.toString()
    }
  }

  dependencies {
    add("coreLibraryDesugaring", libs.coreLibraryDesugaring.get())
  }
}

private fun CommonExtension<*, *, *, *>.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
  (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}
