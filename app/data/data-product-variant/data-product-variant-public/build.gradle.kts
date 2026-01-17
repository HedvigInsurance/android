plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

dependencies {
  api(projects.dataContract)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloOctopusPublic)
}
