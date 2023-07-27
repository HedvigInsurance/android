plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(projects.apolloGiraffePublic)
  api(projects.marketCore)

  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.preference)
  implementation(libs.koin.core)
  implementation(projects.coreCommonPublic)
}
