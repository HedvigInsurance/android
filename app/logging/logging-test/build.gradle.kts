plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.junit)
  api(projects.loggingPublic)
}
