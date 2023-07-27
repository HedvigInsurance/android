plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.adyen)
  implementation(projects.apolloGiraffePublic)
}
