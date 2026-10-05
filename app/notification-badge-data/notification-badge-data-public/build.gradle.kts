plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
}

dependencies {
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.featureFlags)
  implementation(projects.languageCore)

  testImplementation(libs.apollo.testingSupport)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.apolloOctopusTest)
  testImplementation(projects.coreDatastoreTest)
  testImplementation(projects.featureFlagsTest)
}
