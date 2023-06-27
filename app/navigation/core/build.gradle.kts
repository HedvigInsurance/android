plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.navigation.core"
}

dependencies {
  implementation(projects.app.core.common)
  implementation(projects.app.core.resources)
  implementation(projects.app.data.claimTriaging)
  implementation(projects.app.navigation.navigationComposeTyped)

  implementation(libs.androidx.annotation)
  implementation(libs.androidx.navigation.common)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
}
