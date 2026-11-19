import com.hedvig.android.registerUmbrellaClasspathGuard
import org.gradle.api.internal.catalog.DelegatingProjectDependency
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
  id("com.rickclephas.kmp.nativecoroutines")
}

val iosTestHookInFramework = providers.gradleProperty("hedvig.ios.testHook").orNull.toBoolean()

kotlin {
  sourceSets.configureEach {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
  }
  val frameworkName = "HedvigShared"
  val xcf = XCFramework(frameworkName)
  // Only what iOS calls: the auth client, the logger and the holder with the read failures. A module
  // with reads for iOS is added here, and nothing else changes. Everything a module here reaches
  // through its public API is linked into the framework, so exporting a module iOS does not use only
  // adds bytes and header surface (network-clients alone dragged Apollo, Arrow and Okio in, ~2.9 MB).
  // Keep this list free of Compose UI: one such export pulls Skiko and ~70 MB of Skia, ICU and
  // HarfBuzz into the iOS app. iOS builds its UI natively. Every other KMP module keeps compiling
  // for iOS under the convention plugin, so re-exporting one later is a one-line change here.
  val projectsToExport: List<DelegatingProjectDependency> = listOf(
    projects.authlib,
    projects.iosDataBridge,
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
    nativeMain.dependencies {
      implementation(libs.apollo.runtime)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDatastorePublic)
      implementation(projects.languageCore)
      implementation(projects.networkClients)
      implementation(projects.trackingCore)
    }
    nativeTest.dependencies {
      implementation(libs.apollo.normalizedCache)
      implementation(libs.assertK)
      implementation(libs.kotlinx.serialization.json)
      implementation(projects.dataPayingMember)
    }
    // The test hook: a graph over a canned HTTP engine for the umbrella's tests and the iOS app's simulator tests.
    // Tests always compile it. A simulator framework carries it only when built with -Phedvig.ios.testHook=true, for
    // the iOS app's tests in local umbrella mode; the release workflow never sets it, so no published framework does.
    getByName(if (iosTestHookInFramework) "iosSimulatorArm64Main" else "iosSimulatorArm64Test") {
      kotlin.srcDir("src/iosTestHook/kotlin")
      dependencies {
        implementation(libs.ktor.client.mock)
      }
    }
  }
}

registerUmbrellaClasspathGuard()
