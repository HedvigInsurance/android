hedvig {
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(libs.slimber)
  implementation(libs.timber)
  implementation(projects.loggingPublic)
}
