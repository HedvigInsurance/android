plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.kotlinx.immutable.collections)
  implementation(projects.dataContractPublic)
}
