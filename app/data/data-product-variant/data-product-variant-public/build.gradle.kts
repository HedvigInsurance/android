hedvig {
  serialization()
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  api(projects.dataContractPublic)
  implementation(libs.kotlinx.serialization.core)
}
