plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.androidx.annotation)
      implementation(libs.coroutines.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.ktor.client.core)
      implementation(projects.coreCommonPublic)

    }
    commonTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.kotlin.test)
    }
  }
}
