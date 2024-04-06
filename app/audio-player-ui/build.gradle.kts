plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.compose.animation.core)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.androidx.lifecycle.common)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.audioPlayerData)

  testImplementation(libs.assertK)
  testImplementation(libs.junit)
}
