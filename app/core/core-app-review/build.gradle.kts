plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}
dependencies {
  implementation(libs.androidx.other.constraintLayout)
  implementation(libs.androidx.datastore.core)
  implementation(libs.koin.core)
  implementation(libs.koin.android)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.playReview)
}
