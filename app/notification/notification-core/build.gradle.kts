plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(platform(libs.firebase.bom))

  implementation(libs.androidx.other.core)
  implementation(libs.firebase.messaging)
}
