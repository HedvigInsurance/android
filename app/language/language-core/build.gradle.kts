plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(projects.coreCommonPublic)
}
