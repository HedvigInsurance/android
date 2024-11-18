plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  apollo("octopus") {
    generateOptionalOperationVariables = false
  }
  serialization()
  compose()
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
  implementation(projects.coreFileUpload)
  implementation(projects.coreMarkdown)
  implementation(projects.coreResources)
  implementation(projects.coreRetrofit)
  implementation(projects.dataChat)
  implementation(projects.designSystemHedvig)
  implementation(projects.featureFlagsPublic)
  implementation(projects.languageCore)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationActivity)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.placeholder)
}
