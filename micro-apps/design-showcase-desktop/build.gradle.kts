plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.multiplatform.library")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlin.stdlib)
      }
    }
    commonTest {
      dependencies {
        implementation(libs.kotlin.test)
      }
    }
  }
}
