plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.composeCompilerGradlePlugin)
}

dependencies {
  api(projects.memberRemindersPublic)

  implementation(libs.accompanist.pagerIndicators)
  implementation(libs.accompanist.permissions)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.immutable.collections)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.notificationPermission)
}
