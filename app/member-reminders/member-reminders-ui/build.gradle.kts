hedvig {
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

dependencies {
  api(projects.memberRemindersPublic)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.kotlinx.datetime)
  implementation(projects.composePagerIndicator)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
  implementation(projects.notificationPermission)
}
