plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

hedvig {
  serialization()
}

dependencies {
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloOctopusPublic)
}
