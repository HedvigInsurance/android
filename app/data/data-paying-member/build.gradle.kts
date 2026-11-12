plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.arrow.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.dataContract)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.junit)
    }
  }
}
