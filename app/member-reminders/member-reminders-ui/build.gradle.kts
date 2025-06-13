plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  api(projects.memberRemindersPublic)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.kotlinx.datetime)
  implementation(projects.composePagerIndicator)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
  implementation(projects.notificationPermission)
}
