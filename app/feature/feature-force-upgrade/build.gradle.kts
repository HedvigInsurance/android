plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
}
