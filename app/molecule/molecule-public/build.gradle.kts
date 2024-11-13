plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.coroutines.core)
}
