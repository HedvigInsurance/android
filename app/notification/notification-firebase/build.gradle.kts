plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(platform(libs.firebase.bom))
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.apollo.runtime)
  implementation(libs.firebase.messaging)
  implementation(libs.koin.android)
  implementation(libs.koin.workManager)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.authCorePublic)
  implementation(projects.authEventCore)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.featureFlagsPublic)
  implementation(projects.notificationCore)
}
