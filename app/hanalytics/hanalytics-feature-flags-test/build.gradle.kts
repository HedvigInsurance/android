plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.hanalytics.hanalyticsFeatureFlags)

  api(libs.hAnalytics)
}

android {
  namespace = "com.hedvig.android.hanalytics.featureflags.test"
}
