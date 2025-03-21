plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  api(libs.junit)

  implementation(libs.apollo.testingSupport)
  implementation(libs.atomicfu)
  implementation(libs.turbine)
}
