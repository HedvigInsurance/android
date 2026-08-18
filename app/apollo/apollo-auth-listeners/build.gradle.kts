plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.authEventCore)
  implementation(projects.coreCommonPublic)
}
