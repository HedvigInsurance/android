plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.room.runtime)
  implementation(projects.coreCommonPublic)
  api(projects.databaseCore)
}
