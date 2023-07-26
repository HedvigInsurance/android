plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreCommonPublic)
  implementation(projects.hanalyticsCore)
  implementation(projects.marketCore)

  implementation(libs.koin.core)
}

android {
  namespace = "com.hedvig.android.hanalytics.featureflags"
}
