plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.molecule)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.coroutines.core)
}
