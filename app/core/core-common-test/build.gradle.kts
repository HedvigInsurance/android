hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  api(libs.assertK)
  api(libs.junit)

  implementation(libs.arrow.core)
  implementation(libs.coroutines.test)
}
