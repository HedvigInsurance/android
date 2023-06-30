plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.language.languageCore)
}

android {
  namespace = "com.hedvig.android.language.test"
}
