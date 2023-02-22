plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apollo)
  implementation(projects.coreDatastore)
  implementation(projects.coreCommon)
  implementation(projects.coreResources)

  implementation(libs.androidx.other.preference)
  implementation(libs.koin.android)
  implementation(libs.androidx.datastore.preferencesCore)
}

android {
  namespace = "com.hedvig.android.market"
}
