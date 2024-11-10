hedvig {
  serialization()
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.runtime)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.navigationCompose)
}
