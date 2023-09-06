plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.hedvig.authlib)
  implementation(libs.turbine)
}
