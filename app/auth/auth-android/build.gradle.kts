plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.koin.android)
  implementation(projects.authCore)
  implementation(projects.navigationActivity)
}
