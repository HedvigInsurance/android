plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

dependencies {
  api(projects.dataContractPublic)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloOctopusPublic)
}
