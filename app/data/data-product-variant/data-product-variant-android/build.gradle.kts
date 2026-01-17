plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreResources)
  implementation(projects.dataContract)
  implementation(projects.dataProductVariantPublic)
}
