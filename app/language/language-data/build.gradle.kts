plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
}

dependencies {

  implementation(libs.apollo.runtime)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.authEventCore)
  implementation(projects.coreCommonPublic)
  implementation(projects.languageCore)
}
