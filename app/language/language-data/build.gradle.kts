plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.apollo)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  apolloMetadata(projects.apolloOctopusPublic)

  implementation(libs.apollo.runtime)
  implementation(libs.koin.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.authEventCore)
  implementation(projects.languageCore)
}

apollo {
  service("octopus") {
    packageName.set("octopus")
  }
}
