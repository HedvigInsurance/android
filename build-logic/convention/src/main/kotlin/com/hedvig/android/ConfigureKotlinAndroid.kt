package com.hedvig.android

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Configure base Kotlin with Android options for both application and library modules
 */
internal fun Project.configureKotlinAndroid(commonExtension: AndroidCommonExtension) {
  val project = this@configureKotlinAndroid
  val libs = the<LibrariesForLibs>()

  commonExtension.apply {
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
      minSdk = libs.versions.minSdkVersion.get().toInt()
    }

    compileOptions {
      isCoreLibraryDesugaringEnabled = true
      sourceCompatibility = JavaVersion.VERSION_21
      targetCompatibility = JavaVersion.VERSION_21
    }

    configureAutomaticNamespace(this)
  }

  project.configure<KotlinAndroidProjectExtension> {
    compilerOptions.configureKotlinCompilerOptions()
  }

  fun Project.isLoggingPublicModule(): Boolean {
    return name == "logging-public"
  }

  fun Project.isTrackingCoreModule(): Boolean {
    return name == "tracking-core"
  }
  dependencies {
    val koinBom = libs.koin.bom
    add("implementation", platform(koinBom))

    add("coreLibraryDesugaring", libs.coreLibraryDesugaring.get())
    add("lintChecks", project(":hedvig-lint"))
    // Add logging-public and tracking-core to all modules except themselves
    if (!project.isLoggingPublicModule() && !project.isTrackingCoreModule()) {
      add("implementation", project(":logging-public"))
      add("implementation", project(":tracking-core"))
    }
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
