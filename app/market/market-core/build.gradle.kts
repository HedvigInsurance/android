plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.apolloGiraffePublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)

  implementation(libs.androidx.other.preference)
  implementation(libs.koin.core)
}

android {
  namespace = "com.hedvig.android.market"
}
