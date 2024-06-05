plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.uiGraphics)
}
