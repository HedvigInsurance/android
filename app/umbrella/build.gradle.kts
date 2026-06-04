import org.gradle.api.internal.catalog.DelegatingProjectDependency
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  alias(libs.plugins.composeKotlinCompilerGradlePlugin)
  alias(libs.plugins.composeJetbrainsCompilerGradlePlugin)
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
  alias(libs.plugins.kmpNativeCoroutines)
}

kotlin {
  sourceSets.configureEach {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
  }
  val frameworkName = "HedvigShared"
  val xcf = XCFramework(frameworkName)
  val projectsToExport: List<DelegatingProjectDependency> = listOf(
    projects.authlib,
    projects.coreBuildConstants,
    projects.coreDatastorePublic,
    projects.designSystemApi,
    projects.featureFlags,
    projects.featureHelpCenter,
    projects.languageCore,
    projects.loggingPublic,
    projects.networkClients,
    projects.shareddi,
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
      binaryOption("smallBinary", "true")
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
