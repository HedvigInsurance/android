plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  navKeys()
  viewModels()
}

dependencies {
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.authCorePublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
  implementation(projects.featureFlags)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationCore)
}
