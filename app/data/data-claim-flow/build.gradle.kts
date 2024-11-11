plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  serialization()
  compose()
}

dependencies {
  api(libs.retrofit)
  api(libs.retrofitArrow)

  implementation(libs.androidx.compose.runtime)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.retrofitKotlinxSerializationConverter)
  implementation(projects.apolloCore)
  implementation(projects.apolloNetworkCacheManager)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.audioPlayerData)
  implementation(projects.coreAppReview)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDatastorePublic)
  implementation(projects.coreResources)
  implementation(projects.coreRetrofit)
  implementation(projects.coreUiData)
  implementation(projects.dataClaimTriaging)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
}
