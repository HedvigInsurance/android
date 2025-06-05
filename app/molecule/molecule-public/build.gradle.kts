plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.coroutines.core)
}
