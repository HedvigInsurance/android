plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.navigation.runtime)
  implementation(libs.kiwi.navigationCompose)
  implementation(projects.composeWebview)
  implementation(projects.navigationCore)
}
