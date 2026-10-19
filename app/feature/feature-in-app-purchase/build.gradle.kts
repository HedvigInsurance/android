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
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.coil.coil)
  implementation(libs.coroutines.core)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composeUi)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataContract)
  implementation(projects.dataProductVariantPublic)
  implementation(projects.designSystemHedvig)
  implementation(projects.featureInAppPurchaseNavigation)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.uiTiersAndAddons)
}
