plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.accompanist.permissions)

  implementation(libs.androidx.activity.compose)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
}
