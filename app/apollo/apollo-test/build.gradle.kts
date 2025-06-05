plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.junit)

  implementation(libs.apollo.testingSupport)
  implementation(libs.atomicfu)
  implementation(libs.turbine)
}
