plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apollo)
  implementation(projects.coreCommonAndroid)
  implementation(projects.coreResources)

  implementation(libs.androidx.other.preference)
  implementation(libs.koin.android)
}
