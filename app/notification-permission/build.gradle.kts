plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
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
