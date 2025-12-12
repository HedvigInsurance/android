plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.animationCore)
  implementation(libs.jetbrains.compose.runtime)
  implementation(projects.designSystemHedvig)
}
