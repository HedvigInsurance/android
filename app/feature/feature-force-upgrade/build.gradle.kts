hedvig {
  compose()
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
}
