plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.serialization)
}

dependencies {
  api(projects.dataContractPublic)
  implementation(libs.kotlinx.serialization.core)
}
