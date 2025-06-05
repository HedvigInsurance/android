plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.material3)
}
