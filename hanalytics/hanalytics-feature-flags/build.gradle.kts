plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreCommon)
  implementation(projects.hanalytics.hanalytics)
  implementation(projects.hedvigMarket)

  implementation(libs.koin.core)
}
