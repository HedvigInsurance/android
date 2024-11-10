plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("kotlin-parcelize")
  alias(libs.plugins.serialization)
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material3)
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
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreRetrofit)
}
