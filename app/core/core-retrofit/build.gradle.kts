plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.retrofitArrow)
  api(projects.coreCommonPublic)
}
