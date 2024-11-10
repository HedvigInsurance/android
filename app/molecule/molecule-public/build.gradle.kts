hedvig {
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
  id("hedvig.kotlin.library.compose")
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.coroutines.core)
}
