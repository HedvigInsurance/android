plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("kotlin-parcelize")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.other.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreResources)
}
