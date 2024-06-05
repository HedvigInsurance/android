plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.compose.materialIconsCore)
  implementation(libs.androidx.compose.uiCore)
}
