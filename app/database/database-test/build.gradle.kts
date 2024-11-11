plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  api(libs.junit)
  api(libs.room.runtime)
  api(libs.sqlite.bundled)
}
