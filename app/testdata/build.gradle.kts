plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.apolloGiraffePublic)
  implementation(libs.adyen)
}

android {
  namespace = "com.hedvig.lib.testdata"
}
