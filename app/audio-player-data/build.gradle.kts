plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
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
