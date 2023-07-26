plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.languageCore)
}

android {
  namespace = "com.hedvig.android.language.test"
}
