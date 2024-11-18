plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

hedvig {
  apollo("octopus")
}

dependencies {

  implementation(libs.apollo.runtime)
  implementation(libs.koin.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.authEventCore)
  implementation(projects.languageCore)
}
