plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.paging.common)
  implementation(libs.room.paging)
  implementation(libs.room.runtime)
  implementation(libs.sqlite.bundled)
  implementation(libs.uuid)
  ksp(libs.room.ksp)
  implementation(projects.coreCommonPublic)
}

room {
  schemaDirectory(
    project
      .rootDir
      .resolve("app")
      .resolve("database")
      .resolve("schemas")
      .absolutePath,
  )
}