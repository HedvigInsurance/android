package com.hedvig.android

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
  commonExtension: AndroidCommonExtension,
) {
  val libs = the<LibrariesForLibs>()

  commonExtension.apply {
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
      minSdk = libs.versions.minSdkVersion.get().toInt()
    }

    compileOptions {
      isCoreLibraryDesugaringEnabled = true
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
      configureKotlinOptions(this@configureKotlinAndroid)
    }

    configureAutomaticNamespace(this)
  }

  dependencies {
    val koinBom = libs.koin.bom
    add("implementation", platform(koinBom))

    add("coreLibraryDesugaring", libs.coreLibraryDesugaring.get())
    add("lintChecks", project(":hedvig-lint"))
    if (this@configureKotlinAndroid.name != "logging-public") {
      add("implementation", project(":logging-public"))
    }
  }
}

/**
 * Configure base Kotlin without Android options
 */
internal fun Project.configureKotlin(kotlinCompile: KotlinCompile) {
  kotlinCompile.kotlinOptions {
    this.configureKotlinOptions(this@configureKotlin)
  }
}

/**
 * Takes the project name and creates an aptly named namespace definition for it. For example
 * project name: :notification-badge-data-fake
 * results in: com.hedvig.android.notification.badge.data.fake
 */
private fun Project.configureAutomaticNamespace(commonExtension: AndroidCommonExtension) {
  with(commonExtension) {
    if (path.contains(".") || path.contains("_")) error("Module names should just contain `-` between words")
    if (namespace == null) {
      namespace = "com.hedvig.android" + path
        .replace(":", ".") // Change the ':' suffix into a `.` to go after com.hedvig.android
        .replace("-", ".") // Change all '-' in the module name into '.'
        .replace("public", "pub") // "public" breaks the generateRFile agp task, "pub" should suffice
    }
  }
}

private fun KotlinJvmOptions.configureKotlinOptions(
  project: Project,
) {
  // Treat all Kotlin warnings as errors (disabled by default)
  allWarningsAsErrors = project.properties["warningsAsErrors"] as? Boolean ?: false

  freeCompilerArgs = freeCompilerArgs + listOf(
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
  )

  // Get compose metrics with `./gradlew :app:assembleRelease -Pcom.hedvig.app.enableComposeCompilerReports=true`
  if (project.findProperty("com.hedvig.app.enableComposeCompilerReports") == "true") {
    freeCompilerArgs = freeCompilerArgs + listOf(
      "-P",
      "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
        project.buildDir.absolutePath + "/compose_metrics",
    )
    freeCompilerArgs = freeCompilerArgs + listOf(
      "-P",
      "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
        project.buildDir.absolutePath + "/compose_metrics",
    )
  }

  jvmTarget = JavaVersion.VERSION_17.toString()
}

private fun AndroidCommonExtension.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
  (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}
