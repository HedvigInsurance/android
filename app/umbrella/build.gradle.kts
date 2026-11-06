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
  // Only what iOS calls: the auth client and the logger. Everything a module here reaches through
  // its public API is linked into the framework, so exporting a module iOS does not use only adds
  // bytes and header surface (network-clients alone dragged Apollo, Arrow and Okio in, ~2.9 MB).
  // Keep this list free of Compose UI: one such export pulls Skiko and ~70 MB of Skia, ICU and
  // HarfBuzz into the iOS app. iOS builds its UI natively. Every other KMP module keeps compiling
  // for iOS under the convention plugin, so re-exporting one later is a one-line change here.
  val projectsToExport: List<DelegatingProjectDependency> = listOf(
    projects.authlib,
    projects.loggingPublic,
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
      // -Oz instead of -O3 for the Kotlin side: measured -18% shipped bytes on the device slice.
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
