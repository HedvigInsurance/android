plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
}

dependencies {
  implementation(libs.arrow.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.dataContract)

  testImplementation(libs.assertK)
  testImplementation(libs.junit)
}
