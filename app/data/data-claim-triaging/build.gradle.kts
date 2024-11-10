plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.navigationComposeTyped)
}
