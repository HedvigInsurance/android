plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.data.claimtriaging"
}

dependencies {
  implementation(projects.app.apollo.octopus)
  implementation(projects.app.navigation.navigationComposeTyped)

  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
}
