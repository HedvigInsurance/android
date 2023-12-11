plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.apollo.api)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.koin.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.authEventCore)
}
