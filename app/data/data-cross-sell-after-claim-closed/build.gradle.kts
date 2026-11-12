plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.arrow.core)
      implementation(libs.coroutines.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.dataCrossSellAfterFlow)
    }
  }
}
