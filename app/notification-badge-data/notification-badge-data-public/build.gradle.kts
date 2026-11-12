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
      implementation(libs.androidx.datastore.core)
      implementation(libs.androidx.datastore.preferencesCore)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.arrow.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.featureFlags)
      implementation(projects.languageCore)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.coreDatastoreTest)
      implementation(projects.featureFlagsTest)
    }
  }
}
