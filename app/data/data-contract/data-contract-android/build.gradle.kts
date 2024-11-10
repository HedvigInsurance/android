hedvig {
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(projects.coreResources)
  implementation(projects.dataContractPublic)
}
