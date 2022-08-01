plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.hanalytics)

  api(libs.hAnalytics)
}
