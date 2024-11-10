hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(libs.androidx.compose.materialIconsCore)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.navigationCore)
}
