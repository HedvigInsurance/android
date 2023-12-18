plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.turbine)
  implementation(projects.hanalyticsFeatureFlagsPublic)
}
