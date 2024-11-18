plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.room.paging)
  implementation(libs.room.runtime)
  implementation(projects.dataChat)

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