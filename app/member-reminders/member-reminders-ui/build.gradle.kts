plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.memberRemindersPublic)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.kotlinx.datetime)
      implementation(projects.composePagerIndicator)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
      implementation(projects.notificationPermission)
    }
  }
}
