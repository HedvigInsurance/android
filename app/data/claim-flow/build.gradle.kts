plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.data.claimflow"
}

dependencies {
  implementation(projects.app.apollo.core)
  implementation(projects.app.apollo.octopus)
  implementation(projects.app.core.common)
  implementation(projects.app.core.datastore)
  implementation(projects.app.core.resources)
  implementation(projects.app.core.uiData)
  implementation(projects.app.data.claimTriaging)
  implementation(projects.app.navigation.navigationComposeTyped)

  implementation(libs.androidx.compose.runtime)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.retrofit)
  implementation(libs.retrofitArrow)
  implementation(libs.retrofitKotlinxSerializationConverter)
  implementation(libs.slimber)
}
