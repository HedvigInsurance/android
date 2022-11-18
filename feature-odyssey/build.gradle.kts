plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  id("kotlin-parcelize")
}

dependencies {
  implementation(projects.auth)
  implementation(projects.coreCommon)
  implementation(projects.coreNavigation)
  implementation(projects.hedvigLanguage)

  implementation(libs.androidx.other.activityCompose)
  implementation(libs.coil.coil)
  implementation(libs.koin.android)
  implementation(libs.odyssey)
}
