plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.hanalyticsFeatureFlagsPublic)

  api(libs.hAnalytics)

  implementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.hanalytics.featureflags.test"
}
