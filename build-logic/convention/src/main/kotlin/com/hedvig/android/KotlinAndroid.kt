package com.hedvig.android

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

/**
 * Configure base Kotlin with Android options
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
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }

    fun AndroidCommonExtension.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
      (this as ExtensionAware).extensions.configure("kotlinOptions", block)
    }
    kotlinOptions {
      configureKotlinOptions(project)
    }

    configureAutomaticNamespace(this)
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

private fun Project.isLoggingPublicModule(): Boolean {
  return name == "logging-public"
}

private fun Project.isTrackingCoreModule(): Boolean {
  return name == "tracking-core"
}

/**
 * Configure base Kotlin without Android options
 */
internal fun Project.configureJavaAndKotlin() {
  kotlinExtension.forEachCompilerOptions {
    configureKotlinOptions(this@configureJavaAndKotlin)
  }

  project.extensions.getByType(JavaPluginExtension::class.java).apply {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
  }
  project.tasks.withType(JavaCompile::class.java).configureEach {
    options.release.set(17)
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

private fun KotlinProjectExtension.forEachCompilerOptions(block: KotlinCommonCompilerOptions.() -> Unit) {
  when (this) {
    is KotlinJvmProjectExtension -> compilerOptions.block()
    is KotlinAndroidProjectExtension -> compilerOptions.block()
    is KotlinMultiplatformExtension -> {
      targets.all {
        compilations.all {
          compilerOptions.configure {
            block()
          }
        }
      }
    }

    else -> error("Unknown kotlin extension $this")
  }
}

/**
 * Same as [configureKotlinOptions] but for the JVM library convention plugin.
 * There is no common interface for these two to apply them together. https://youtrack.jetbrains.com/issue/KT-58956
 */
private fun KotlinCommonCompilerOptions.configureKotlinOptions(project: Project) {
  freeCompilerArgs.addAll(project.commonFreeCompilerArgs())

  apiVersion.set(KotlinVersion.KOTLIN_1_9)
  languageVersion.set(KotlinVersion.KOTLIN_1_9)

  when (this) {
    is KotlinJvmCompilerOptions -> {
      freeCompilerArgs.add("-Xjvm-default=all")
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }
}

private fun KotlinJvmOptions.configureKotlinOptions(project: Project) {
  freeCompilerArgs = freeCompilerArgs + project.commonFreeCompilerArgs()

  jvmTarget = JavaVersion.VERSION_17.toString()
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
