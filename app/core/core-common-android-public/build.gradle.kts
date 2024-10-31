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
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.uiUnit)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.okhttp.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreResources)
}
