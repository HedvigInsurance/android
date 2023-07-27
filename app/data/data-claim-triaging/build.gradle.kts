plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

android {
  namespace = "com.hedvig.android.data.claimtriaging"
}

dependencies {
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.navigationComposeTyped)
}
