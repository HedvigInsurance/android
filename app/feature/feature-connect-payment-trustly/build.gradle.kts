plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
}

dependencies {
  api(libs.androidx.navigation.common)

  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiCore)
  implementation(libs.androidx.compose.uiGraphics)
  implementation(libs.androidx.compose.uiUnit)
  implementation(libs.androidx.lifecycle.common)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.androidx.lifecycle.viewmodelCompose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.other.browser)
  implementation(libs.apollo.api)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.koin.coreViewmodel)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloNetworkCacheManager)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composeWebview)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationCore)
}
