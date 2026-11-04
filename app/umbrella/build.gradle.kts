import org.gradle.api.internal.catalog.DelegatingProjectDependency
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
  id("com.rickclephas.kmp.nativecoroutines")
}

kotlin {
  sourceSets.configureEach {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
  }
  val frameworkName = "HedvigShared"
  val xcf = XCFramework(frameworkName)
  // Keep this list free of Compose. Every module here is linked whole (DCE is off for native
  // targets, see KotlinMultiplatformIosLibraryConventionPlugin), so one Compose UI export drags
  // Skiko and the whole Compose stack into the framework. iOS builds its UI natively.
  val projectsToExport: List<DelegatingProjectDependency> = listOf(
    projects.authlib,
    projects.coreBuildConstants,
    projects.coreDatastorePublic,
    projects.featureFlags,
    projects.languageCore,
    projects.loggingPublic,
    projects.networkClients,
  )
  listOf(
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      isStatic = true
      for (projectToExport in projectsToExport) {
        export(projectToExport)
      }
      binaryOption("bundleId", frameworkName)
      baseName = frameworkName
      xcf.add(this)
    }
  }

  sourceSets {
    commonMain.dependencies {
      for (projectToExport in projectsToExport) {
        api(projectToExport)
      }
    }
  }
}
