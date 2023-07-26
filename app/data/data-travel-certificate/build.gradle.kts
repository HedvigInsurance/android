plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

android {
  namespace = "com.hedvig.android.data.travelcertificate"
}

dependencies {
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.hanalyticsFeatureFlagsPublic)

  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.slimber)
}
