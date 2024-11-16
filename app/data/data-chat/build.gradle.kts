plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
}

hedvig {
  serialization()
}

dependencies {
  api(libs.paging.common)
  api(libs.room.runtime)

  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.room.paging)
  implementation(libs.sqlite.bundled)
  implementation(libs.uuid)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreUiData)
  implementation(projects.dataContractPublic)
  implementation(projects.dataProductVariantPublic)

  ksp(libs.room.ksp)
}

val schemaDirectory = project
  .rootDir
  .resolve("app")
  .resolve("database")
  .resolve("schemas")
  .absolutePath

// workaround for https://issuetracker.google.com/issues/379337774 Remove the `ksp` block completely when this is fixed
ksp {
  arg("room.schemaLocation", schemaDirectory)
}

room {
  schemaDirectory(schemaDirectory)
}
