plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("kotlin-parcelize")
  alias(libs.plugins.serialization)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.androidx.compose.foundation)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.retrofit)
  implementation(libs.retrofitArrow)
  implementation(libs.retrofitKotlinxSerializationConverter)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composePhotoCaptureState)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.coreRetrofit)
  implementation(projects.designSystemHedvig)
}
