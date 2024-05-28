plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.apollo)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  apolloMetadata(projects.apolloOctopusPublic)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.featureFlagsPublic)
  implementation(projects.languageCore)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.featureFlagsTest)
}

apollo {
  service("octopus") {
    packageName.set("octopus")
  }
}
