plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

dependencies {
  implementation(projects.authCore)
  implementation(projects.navigationActivity)

  implementation(libs.koin.android)
  implementation(libs.slimber)
}

android {
  namespace = "com.hedvig.android.auth.android"
}
