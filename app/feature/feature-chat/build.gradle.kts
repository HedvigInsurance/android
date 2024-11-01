plugins {
  id("hedvig.android.feature")
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.apollo)
  alias(libs.plugins.serialization)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.androidx.navigation.common)
  api(libs.coil.coil)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.coil.compose)
  implementation(libs.compose.richtext)
  implementation(libs.compose.richtextCommonmark)
  implementation(libs.compose.richtextUi)
  implementation(libs.coroutines.core)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.paging.common)
  implementation(libs.paging.compose)
  implementation(libs.retrofit)
  implementation(libs.retrofitArrow)
  implementation(libs.retrofitKotlinxSerializationConverter)
  implementation(libs.room.runtime)
  implementation(libs.sqlite.bundled)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composePhotoCaptureState)
  implementation(projects.composeUi)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreFileUpload)
  implementation(projects.coreIcons)
  implementation(projects.coreMarkdown)
  implementation(projects.coreResources)
  implementation(projects.coreRetrofit)
  implementation(projects.coreUi)
  implementation(projects.dataChat)
  implementation(projects.designSystemHedvig)
  implementation(projects.featureFlagsPublic)
  implementation(projects.languageCore)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationActivity)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.placeholder)
}

apollo {
  service("octopus") {
    packageName = "octopus"
    generateOptionalOperationVariables.set(false)
    dependsOn(projects.apolloOctopusPublic, true)
  }
}
