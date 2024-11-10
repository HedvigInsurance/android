hedvig {
  serialization()
  compose()
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.other.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreResources)
}
