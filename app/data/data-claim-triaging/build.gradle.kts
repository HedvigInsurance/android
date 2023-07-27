plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.data.claimtriaging"
}

dependencies {
  implementation(projects.apolloOctopusPublic)
  implementation(projects.navigationComposeTyped)

  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
}
