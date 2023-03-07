plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  @Suppress("DSL_SCOPE_VIOLATION")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.feature.terminateinsurance"
}

dependencies {
  implementation(projects.apollo.core)
  implementation(projects.apollo.giraffe)
  implementation(projects.auth.authAndroid)
  implementation(projects.coreCommon)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.navigation.navigationComposeTyped)

  implementation(libs.accompanist.navigationAnimation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.viewmodelCompose)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.android)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.slimber)
  implementation(libs.timber)
}
