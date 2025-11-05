import org.gradle.api.internal.catalog.DelegatingProjectDependency
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
//  alias(libs.plugins.skie)
}

kotlin {
  val frameworkName = "HedvigShared"
  val xcf = XCFramework(frameworkName)
  val projectsToExport: List<DelegatingProjectDependency> = listOf(
    projects.authlib,
    projects.featureClaimChat,
  )
  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      for (projectToExport in projectsToExport) {
        export(projectToExport)
      }
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

kotlin.targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
  binaries.all {
    freeCompilerArgs += "-Xdisable-phases=DevirtualizationAnalysis,DCEPhase"
  }
}

//skie {
//  build {
//    enableSwiftLibraryEvolution.set(true)
//  }
//}
