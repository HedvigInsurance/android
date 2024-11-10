hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

dependencies {
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
}
