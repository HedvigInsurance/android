plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(platform(libs.firebase.bom))

  implementation(libs.androidx.other.coreKtx)
  implementation(libs.firebase.messaging)
  implementation(projects.coreResources)
}
