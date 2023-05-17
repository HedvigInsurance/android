plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.coreCommon)
  implementation(projects.app.hanalytics.hanalyticsCore)
  implementation(projects.app.hedvigMarket)

  implementation(libs.koin.core)
}

android {
  namespace = "com.hedvig.android.hanalytics.featureflags"
}
