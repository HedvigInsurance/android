package com.hedvig.android

import com.android.build.api.dsl.androidLibrary
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

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
 * Configure base Kotlin with Android options for multiplatform android libraries
 */
internal fun Project.configureKotlinAndroidMultiplatform() {
  val project = this@configureKotlinAndroidMultiplatform
  val libs = the<LibrariesForLibs>()

  project.configure<KotlinMultiplatformExtension> {
//    compilerOptions.configureKotlinCompilerOptions()
//    val xcfName = "design-showcake-desktop-kit"
    listOf(
      iosX64(),
      iosArm64(),
      iosSimulatorArm64(),
    ).forEach {
//      it.binaries.framework { baseName = xcfName }
    }
    jvm()
    androidLibrary {
      configureAutomaticNamespace(
        path = path,
        namespace = namespace,
        setNameSpace = { namespace = it },
      )
      this.compileSdk = libs.versions.compileSdkVersion.get().toInt()
      this.minSdk = libs.versions.minSdkVersion.get().toInt()
      this.enableCoreLibraryDesugaring = true
      this.compilations.configureEach {
        this.compileTaskProvider.configure {
          this.compilerOptions {
            this.configureKotlinCompilerOptions()
          }
        }
//        this.compilerOptions.configure {
//          this.jvmTarget.set()
//          this.languageVersion.set()
//        }
      }
      withHostTestBuilder { }
      withDeviceTestBuilder {
        sourceSetTreeName = "test"
      }.configure {
        instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      }
      configureAutomaticNamespace(path, this.namespace, { this.namespace = it })
    }
    applyDefaultHierarchyTemplate()

    sourceSets.getByName("commonMain") {
      dependencies {
        this
          .implementation(libs.kotlin.stdlib)
      }
    }
    sourceSets.getByName("commonTest") {
      dependencies {
        implementation(libs.kotlin.test)
      }
    }
    sourceSets.getByName("androidMain") {
      dependencies {
        // Add Android-specific dependencies here. Note that this source set depends on
        // commonMain by default and will correctly pull the Android artifacts of any KMP
        // dependencies declared in commonMain.
      }
    }
    sourceSets.getByName("androidDeviceTest") {
      dependencies {
//        implementation((libs.androidx.testRunners)
        implementation(libs.androidx.test)
//        implementation((libs.androidx.junit)
      }
    }
    sourceSets.getByName("iosMain") {
      this.dependencies {
        // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
        // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
        // part of KMP’s default source set hierarchy. Note that this source set depends
        // on common by default and will correctly pull the iOS artifacts of any
        // KMP dependencies declared in commonMain.
      }
    }
    sourceSets.getByName("jvmMain") {
      this.dependencies {
      }
    }
  }

  dependencies {
    val koinBom = libs.koin.bom
//    implementation(platform(koinBom))

    add("coreLibraryDesugaring", libs.coreLibraryDesugaring.get())
//    add("lintChecks", project(":hedvig-lint"))
    // Add logging-public and tracking-core to all modules except themselves
    if (!project.isLoggingPublicModule() && !project.isTrackingCoreModule()) {
//      implementation(project(":logging-public"))
//      implementation(project(":tracking-core"))
    }
  }
}

/**
 * Takes the project name and creates an aptly named namespace definition for it. For example
 * project name: :notification-badge-data-fake
 * results in: com.hedvig.android.notification.badge.data.fake
 */
private fun Project.configureAutomaticNamespace(commonExtension: AndroidCommonExtension) {
  configureAutomaticNamespace(path, commonExtension.namespace, { commonExtension.namespace = it })
}

private fun configureAutomaticNamespace(path: String, namespace: String?, setNameSpace: (String) -> Unit) {
  if (path.contains(".") || path.contains("_")) error("Module names should just contain `-` between words")
  if (namespace == null) {
    setNameSpace(
      "com.hedvig.android" + path
        .replace(":", ".") // Change the ':' suffix into a `.` to go after com.hedvig.android
        .replace("-", ".") // Change all '-' in the module name into '.'
        .replace("public", "pub"), // "public" breaks the generateRFile agp task, "pub" should suffice
    )
  }
}

fun Project.isLoggingPublicModule(): Boolean {
  return name == "logging-public"
}

fun Project.isTrackingCoreModule(): Boolean {
  return name == "tracking-core"
}
