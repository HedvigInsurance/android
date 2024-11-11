plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  compose()
}

dependencies {
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
}
