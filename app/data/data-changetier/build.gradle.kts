plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.apollo)
  alias(libs.plugins.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
}

dependencies {

  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreUiData)
  implementation(projects.dataContractPublic)
  implementation(projects.featureFlagsPublic)
  implementation(projects.dataProductVariantPublic)
  implementation(projects.dataProductVariantAndroid)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)

  ksp(libs.room.ksp)
  api(libs.paging.common)
  api(libs.room.runtime)
}

room {
  schemaDirectory(
    project
      .rootDir
      .resolve("app")
      .resolve("database") //todo: check here!!
      .resolve("schemas")
      .absolutePath,
  )
}

apollo {
  service("octopus") {
    packageName = "octopus"
    dependsOn(projects.apolloOctopusPublic, true)
  }
}
