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
      api(libs.kotlinx.datetime)
      api(projects.dataCoinsured)
      implementation(libs.androidx.datastore.core)
      implementation(libs.androidx.datastore.preferencesCore)
      implementation(libs.apollo.api)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.coroutines.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.uuid)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.dataContract)
      implementation(projects.dataPayingMember)
      implementation(projects.dataSettingsDatastorePublic)
      implementation(projects.featureFlags)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.annotations)
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.testParameterInjector)
      implementation(libs.turbine)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.coreCommonTest)
      implementation(projects.coreDatastoreTest)
      implementation(projects.dataSettingsDatastoreTest)
      implementation(projects.featureFlagsTest)
      implementation(projects.loggingTest)
      implementation(projects.memberRemindersTest)
      implementation(projects.testClock)
    }
  }
}
