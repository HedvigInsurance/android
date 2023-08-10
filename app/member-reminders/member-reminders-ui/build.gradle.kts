plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.accompanist.permissions)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.immutable.collections)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.memberRemindersPublic)
  implementation(projects.notificationPermission)
}
