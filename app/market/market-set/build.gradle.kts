hedvig {
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(libs.koin.core)
  implementation(projects.languageCore)
  implementation(projects.marketCore)
}
