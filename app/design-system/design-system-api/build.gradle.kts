plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.material3)
}
