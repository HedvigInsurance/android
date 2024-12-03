plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(libs.koin.core)
  implementation(libs.room.runtime)
  implementation(projects.coreCommonPublic)
  implementation(projects.databaseCore)
}
