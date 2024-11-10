hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
  alias(libs.plugins.serialization)
}

dependencies {
  api(projects.dataContractPublic)
  implementation(libs.kotlinx.serialization.core)
}
