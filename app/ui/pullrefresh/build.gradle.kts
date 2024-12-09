plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.animationCore)
  implementation(libs.androidx.compose.runtime)
  implementation(projects.designSystemHedvig)
}
