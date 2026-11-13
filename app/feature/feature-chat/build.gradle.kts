plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus") {
    generateOptionalOperationVariables = false
  }
  serialization()
  compose()
  viewModels()
  navKeys()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.coil.coil)
      implementation(libs.androidx.datastore.core)
      implementation(libs.androidx.datastore.preferencesCore)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.arrow.core)
      implementation(libs.arrow.fx)
      implementation(libs.coil.compose)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.animation)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.markdown)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.paging.common)
      implementation(libs.paging.compose)
      implementation(libs.room.runtime)
      implementation(libs.uri.kmp)
      implementation(libs.uuid)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.composePhotoCaptureState)
      implementation(projects.composeResultLauncher)
      implementation(projects.composeUi)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.coreFileUpload)
      implementation(projects.coreLocale)
      implementation(projects.coreMarkdown)
      implementation(projects.coreResources)
      implementation(projects.crossSells)
      implementation(projects.dataChat)
      implementation(projects.dataClaimIntent)
      implementation(projects.dataContract)
      implementation(projects.designSystemHedvig)
      implementation(projects.experimentsPublic)
      implementation(projects.featureFlags)
      implementation(projects.languageCore)
      implementation(projects.moleculePublic)
      implementation(projects.navigationActivity)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
    }
    androidMain.dependencies {
      implementation(libs.androidx.activity.compose)
      implementation(libs.media3.exoplayer)
      implementation(libs.media3.exoplayer.dash)
      implementation(libs.media3.ui)
      implementation(libs.sqlite.bundled)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.jetbrains.compose.ui.test)
      implementation(libs.junit)
      implementation(libs.sqlite.bundled)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.authCorePublic)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonTest)
      implementation(projects.coreDatastorePublic)
      implementation(projects.databaseCore)
      implementation(projects.databaseJvm)
      implementation(projects.experimentsTest)
      implementation(projects.languageCore)
      implementation(projects.loggingTest)
      implementation(projects.networkClients)
      implementation(projects.trackingTest)
      implementation(org.jetbrains.compose.ComposePlugin.DesktopDependencies.currentOs)
    }
  }
}
