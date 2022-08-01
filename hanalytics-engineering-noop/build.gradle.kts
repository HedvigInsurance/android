plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.hanalyticsEngineeringApi)
  implementation(libs.koin.android)
}
