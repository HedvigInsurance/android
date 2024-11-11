plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  serialization()
  compose()
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.other.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreResources)
}
