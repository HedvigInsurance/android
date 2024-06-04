plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.composeCompilerGradlePlugin)
}

dependencies {
  api(libs.androidx.navigation.common)
  api(libs.kiwi.navigationCompose)
  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
}
