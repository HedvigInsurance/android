plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

hedvig {
  serialization()
}

dependencies {
  api(projects.dataContractPublic)
  implementation(libs.kotlinx.serialization.core)
}
