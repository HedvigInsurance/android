plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.assertK)
  api(libs.junit)

  implementation(libs.arrow.core)
  implementation(libs.coroutines.test)
}
