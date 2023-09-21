plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.hAnalytics)

  implementation(projects.hanalyticsCore)
}
