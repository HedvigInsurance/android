plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreResources)
}
