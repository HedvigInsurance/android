plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
  alias(libs.plugins.skie)
}

hedvig {
  serialization()
}

kotlin {
  explicitApi()
  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.json)
      implementation(libs.ktor.client.logging)
    }
    jvmMain.dependencies {
      api(libs.ktor.client.okhttp)
    }
    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
    }
  }
}

skie {
  build {
    enableSwiftLibraryEvolution.set(true)
  }
}
