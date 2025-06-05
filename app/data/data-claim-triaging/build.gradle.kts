plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

dependencies {
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloOctopusPublic)
}
