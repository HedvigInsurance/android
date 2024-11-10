hedvig {
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

dependencies {
  api(libs.accompanist.permissions)

  implementation(libs.androidx.activity.compose)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
}
