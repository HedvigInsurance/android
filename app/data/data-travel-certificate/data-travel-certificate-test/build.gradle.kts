plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.arrow.core)
  implementation(libs.turbine)
  implementation(projects.coreCommonPublic)
  implementation(projects.dataTravelCertificatePublic)
}
