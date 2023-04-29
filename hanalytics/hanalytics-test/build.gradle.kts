plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(projects.hanalytics.hanalyticsCore)

  api(libs.hAnalytics)
}
