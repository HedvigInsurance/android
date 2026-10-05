plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
}

dependencies {

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

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(projects.authCoreTest)
  testImplementation(projects.languageTest)
  testImplementation(projects.loggingTest)
}
