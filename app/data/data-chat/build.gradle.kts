plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  ksp(libs.room.ksp)
  api(libs.paging.common)
  api(libs.room.runtime)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.room.paging)
  implementation(libs.sqlite.bundled)
  implementation(libs.uuid)
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
