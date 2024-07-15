plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(projects.memberRemindersPublic)

  implementation(projects.composePagerIndicator)
  implementation(libs.accompanist.permissions)
  implementation(libs.kotlinx.datetime)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.notificationPermission)
}
