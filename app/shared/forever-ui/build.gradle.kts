plugins {
  id("hedvig.android.feature")
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.apollo)
}

dependencies {
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.other.core)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  runtimeOnly(libs.coroutines.android)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.koin.coreViewmodel)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.coreUiData)
  implementation(projects.designSystemHedvig)
  implementation(projects.languageCore)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.pullrefresh)
}

apollo {
  service("octopus") {
    packageName = "octopus"
    dependsOn(projects.apolloOctopusPublic, true)
  }
}
