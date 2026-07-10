plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus") {
    generateOptionalOperationVariables = false
  }
  serialization()
  compose()
  viewModels()
  navKeys()
}

dependencies {
  api(libs.coil.coil)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.datastore.core)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coil.compose)
  implementation(libs.coroutines.core)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.jetbrains.markdown)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.media3.exoplayer)
  implementation(libs.media3.exoplayer.dash)
  implementation(libs.media3.ui)
  implementation(libs.paging.common)
  implementation(libs.paging.compose)
  implementation(libs.room.runtime)
  implementation(libs.sqlite.bundled)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composePhotoCaptureState)
  implementation(projects.composeUi)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.coreFileUpload)
  implementation(projects.coreMarkdown)
  implementation(projects.coreResources)
  implementation(projects.dataChat)
  implementation(projects.dataClaimIntent)
  implementation(projects.designSystemHedvig)
  implementation(projects.featureFlags)
  implementation(projects.languageCore)
  implementation(projects.moleculePublic)
  implementation(projects.navigationActivity)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationCore)
}
