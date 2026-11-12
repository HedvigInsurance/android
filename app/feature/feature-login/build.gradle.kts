plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  androidResources()
  compose()
  viewModels()
  serialization()
  navKeys()
}

kotlin {
  android {
    withHostTest {
      isReturnDefaultValues = true
    }
  }
  sourceSets {
    commonMain.dependencies {
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.components.resources)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.runtime.saveable)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.lifecycle.viewmodel)
      implementation(libs.jetbrains.lifecycle.viewmodel.compose)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.uri.kmp)
      implementation(projects.apolloCore)
      implementation(projects.authCorePublic)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.coreLocale)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
      implementation(projects.languageCore)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
      implementation(projects.networkClients)
    }
    androidMain.dependencies {
      implementation(libs.media3.exoplayer)
      implementation(libs.media3.ui)
    }
    jvmTest.dependencies {
      implementation(libs.androidx.datastore.core)
      implementation(libs.androidx.datastore.preferencesCore)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.jetbrains.compose.ui.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(org.jetbrains.compose.ComposePlugin.DesktopDependencies.currentOs)
      implementation(projects.authCoreTest)
      implementation(projects.authEventCore)
      implementation(projects.authEventFake)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonTest)
      implementation(projects.coreDatastoreTest)
      implementation(projects.languageTest)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
    }
    getByName("androidHostTest").dependencies {
      implementation(libs.androidx.datastore.core)
      implementation(libs.androidx.junit)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.robolectric)
      implementation(libs.turbine)
      implementation(projects.authCoreTest)
      implementation(projects.authEventCore)
      implementation(projects.authEventFake)
      implementation(projects.coreCommonTest)
      implementation(projects.coreDatastoreTest)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
    }
  }
}
