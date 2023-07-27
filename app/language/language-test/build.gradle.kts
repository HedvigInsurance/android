plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.languageCore)
}

android {
  namespace = "com.hedvig.android.language.test"
}
