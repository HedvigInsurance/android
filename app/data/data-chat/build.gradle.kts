plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
}

hedvig {
  serialization()
}

dependencies {
  api(libs.paging.common)
  implementation(libs.kotlinx.datetime)
  implementation(libs.room.common)
  implementation(libs.uuid)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreUiData)
  implementation(projects.dataContractPublic)
  implementation(projects.dataProductVariantPublic)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.room.paging)
  testImplementation(libs.room.runtime)
  testImplementation(libs.sqlite.bundled)
  testImplementation(projects.testClock)
  kspTest(libs.room.ksp)
}

val schemaDirectory = project
  .rootDir
  .resolve("app/data/data-chat/build/generated/ksp/test/kotlin")
  .absolutePath

room {
  schemaDirectory(schemaDirectory)
}
