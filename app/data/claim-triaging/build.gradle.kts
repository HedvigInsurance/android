plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.data.claimtriaging"
}

dependencies {
  implementation(projects.app.apollo.core)
  implementation(projects.app.apollo.octopus)
  implementation(projects.app.core.common)
  implementation(projects.app.navigation.navigationComposeTyped)

  implementation(libs.apollo.normalizedCache)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
}
