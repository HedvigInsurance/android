plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(projects.hanalyticsCore)

  api(libs.hAnalytics)
}
