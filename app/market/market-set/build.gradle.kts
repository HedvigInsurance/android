plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(libs.koin.core)
  implementation(projects.languageCore)
  implementation(projects.marketCore)
}
