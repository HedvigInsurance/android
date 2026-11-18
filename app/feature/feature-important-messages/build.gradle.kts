plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus") {
    generateAsInternal = true
  }
  compose()
  sharedWithIos()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.api)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.jetbrains.compose.runtime)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.moleculePublic)
    }
    getByName("jvmAndAndroidMain").dependencies {
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(projects.designSystemHedvig)
    }
    commonTest.dependencies {
      implementation(libs.apollo.engine.ktor)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.ktor.client.mock)
    }
    jvmTest.dependencies {
      implementation(libs.jetbrains.compose.ui.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(org.jetbrains.compose.ComposePlugin.DesktopDependencies.currentOs)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
    }
    nativeTest.dependencies {
      implementation(projects.coreBuildConstants)
      implementation(projects.networkClients)
    }
  }
}
