plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(projects.app.hanalytics.hanalyticsCore)

  api(libs.hAnalytics)
}
