plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.compose")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.material3)
    }
  }
}
