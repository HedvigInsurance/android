plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.hanalyticsFeatureFlagsPublic)

  api(libs.hAnalytics)

  implementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.hanalytics.featureflags.test"
}
