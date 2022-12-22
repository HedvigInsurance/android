plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

dependencies {
  implementation(projects.auth.authCore)
  implementation(projects.coreNavigation)

  implementation(libs.androidx.other.appCompat)
  implementation(libs.koin.android)
  implementation(libs.slimber)
}
