plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.apollo)
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  apolloMetadata(projects.apolloOctopusPublic)

  implementation(libs.apollo.normalizedCache)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.kotlinx.immutable.collections)
  implementation(projects.coreDemoMode)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.runtime)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.koin.android)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.dataTravelCertificatePublic)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
}

apollo {
  service("octopus") {
    packageName.set("octopus")
  }
}
