plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.junit)
  api(libs.room.runtime)
  api(libs.sqlite.bundled)
}
