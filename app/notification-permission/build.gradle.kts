plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  compose()
}

dependencies {
  api(libs.accompanist.permissions)

  implementation(libs.androidx.activity.compose)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
}
