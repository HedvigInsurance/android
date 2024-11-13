plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  serialization()
  compose()
}

dependencies {
  api(libs.androidx.navigation.common)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
}
