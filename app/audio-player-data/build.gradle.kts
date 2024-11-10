hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.coroutines.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(projects.coreCommonAndroidPublic)

  testImplementation(libs.assertK)
  testImplementation(libs.junit)
}
