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
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.apollo.api)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.jetbrains.compose.animation)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.navigationevent.compose)
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
      implementation(projects.composeUi)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreFileUpload)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.designSystemHedvig)
      implementation(projects.languageCore)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCore)
      implementation(projects.networkClients)
      implementation(projects.uiClaimFlow)
      implementation(projects.uiForceUpgrade)
      implementation(projects.partnersDeflect)
    }
    androidMain.dependencies {
      implementation(libs.accompanist.permissions)
      implementation(libs.androidx.navigation.compose)
      implementation(libs.bundles.kmpPreviewBugWorkaround)
      implementation(libs.rive.android)
      implementation(projects.composeUi)
      implementation(projects.coreRive)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.notificationPermission)
    }
  }
}
