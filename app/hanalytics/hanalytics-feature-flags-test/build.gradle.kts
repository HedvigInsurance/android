plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.hAnalytics)

  implementation(libs.turbine)
  implementation(projects.hanalyticsFeatureFlagsPublic)
}

android {
  namespace = "com.hedvig.android.hanalytics.featureflags.test"
}
