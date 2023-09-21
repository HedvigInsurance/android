plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.apollo.api)
  api(libs.apollo.runtime)
  api(libs.arrow.core)
  api(libs.coroutines.core)
  api(libs.okhttp.core)

  implementation(libs.apollo.normalizedCache)
  implementation(libs.moneta)
  implementation(projects.apolloGiraffePublic)
  implementation(projects.coreCommonPublic)
}
