plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.dataContractPublic)
}
