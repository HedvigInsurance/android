plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.runtime)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
}
