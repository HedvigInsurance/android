plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.retrofitArrow)
  api(projects.coreCommonPublic)
}
