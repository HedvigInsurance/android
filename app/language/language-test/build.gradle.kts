plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.language.languageCore)

  implementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.language.test"
}
