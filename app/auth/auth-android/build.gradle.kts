plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
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
