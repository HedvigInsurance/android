plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.animationCore)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(projects.audioPlayerData)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
}
