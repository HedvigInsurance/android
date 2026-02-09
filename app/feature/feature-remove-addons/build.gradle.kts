plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
  id("hedvig.multiplatform.library.android")
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
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.navigationevent.compose)
      implementation(libs.koin.composeViewModel)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.composeUi)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.dataContract)
      implementation(projects.designSystemHedvig)
      implementation(projects.moleculePublic)
    }
    androidMain.dependencies {
      api(libs.androidx.navigation.common)
      implementation(libs.androidx.navigation.compose)
      implementation(libs.bundles.kmpPreviewBugWorkaround)
      implementation(projects.composeUi)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
      implementation(projects.navigationComposeTyped)
    }
  }
}
