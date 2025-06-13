plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(libs.koin.core)
  implementation(libs.room.runtime)
  implementation(projects.coreCommonPublic)
  implementation(projects.databaseCore)
}
