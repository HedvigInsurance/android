plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apollo)
  implementation(projects.coreCommonAndroid)
  implementation(projects.hedvigMarket)

  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.preference)
  implementation(libs.koin.android)
}
