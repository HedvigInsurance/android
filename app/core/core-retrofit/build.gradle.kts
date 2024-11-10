hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  api(libs.retrofitArrow)
  api(projects.coreCommonPublic)
}
