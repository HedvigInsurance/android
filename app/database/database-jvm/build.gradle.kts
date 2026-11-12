plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(projects.databaseCore)
  implementation(libs.room.runtime)
  implementation(libs.sqlite.bundled)
  implementation(projects.coreCommonPublic)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.uuid)
}
