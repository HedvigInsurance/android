plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  androidResources()
}

dependencies {
  implementation(projects.coreResources)
  implementation(projects.dataContractPublic)
}
