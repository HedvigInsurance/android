plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloOctopusPublic)
}
