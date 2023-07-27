plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

android {
  namespace = "com.hedvig.android.navigation.core"
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.navigation.common)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.dataClaimTriaging)
}
