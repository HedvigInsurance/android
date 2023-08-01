plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.moneta)
  implementation(projects.apolloGiraffePublic)
  implementation(projects.apolloCore)
  implementation(projects.languageCore)
}
