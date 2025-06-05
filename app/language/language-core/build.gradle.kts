plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(projects.coreCommonPublic)
}
