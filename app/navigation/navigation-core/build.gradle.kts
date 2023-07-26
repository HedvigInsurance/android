plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.navigation.core"
}

dependencies {
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.dataClaimTriaging)

  implementation(libs.androidx.annotation)
  implementation(libs.androidx.navigation.common)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
}
