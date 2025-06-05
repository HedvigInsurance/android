plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.coroutines.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreCommonPublic)

  testImplementation(libs.assertK)
  testImplementation(libs.junit)
}
