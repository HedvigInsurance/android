plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.other.preference)
  implementation(libs.koin.core)
  implementation(projects.apolloGiraffePublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
}
