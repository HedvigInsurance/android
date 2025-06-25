plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  androidResources()
}

dependencies {
  implementation(projects.coreResources)
  implementation(projects.dataContractPublic)
}
