plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

dependencies {
  implementation(libs.androidx.compose.animationCore)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(projects.audioPlayerData)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
}
