plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  compose()
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.androidx.lifecycle.compose)
      implementation(libs.apollo.api)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.jetbrains.compose.animation)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.material3)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.koin.composeViewModel)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.logging)
      implementation(libs.uri.kmp)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.audioPlayerData)
      implementation(projects.audioPlayerUi)
      implementation(projects.composePhotoCaptureState)
      implementation(projects.composeResultLauncher)
      implementation(projects.designSystemHedvig)
      implementation(projects.moleculePublic)
      implementation(projects.uiClaimFlow)
    }
    androidMain.dependencies {
      implementation(libs.accompanist.permissions)
      implementation(libs.androidx.compose.uiToolingPreview)
      implementation(libs.androidx.graphicsShapes)
      implementation(libs.androidx.navigation.compose)
      implementation(libs.bundles.kmpPreviewBugWorkaround)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreFileUpload)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.designSystemHedvig)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
    }
  }
}
