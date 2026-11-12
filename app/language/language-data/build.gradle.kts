plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.runtime)
      implementation(projects.apolloCore)
      implementation(projects.apolloNetworkCacheManager)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.authCorePublic)
      implementation(projects.authEventCore)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDatastorePublic)
      implementation(projects.initializable)
      implementation(projects.languageCore)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(projects.authCoreTest)
      implementation(projects.languageTest)
      implementation(projects.loggingTest)
    }
  }
}
