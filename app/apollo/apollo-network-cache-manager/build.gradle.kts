plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.apollo.runtime)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.koin.core)
}
