plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.feature.legacyclaimtriaging"
}

dependencies {
  implementation(projects.app.apollo.core)
  implementation(projects.app.apollo.octopus)
  implementation(projects.app.core.common)
  implementation(projects.app.core.designSystem)
  implementation(projects.app.core.resources)
  implementation(projects.app.core.ui)
  implementation(projects.app.data.claimTriaging)
  implementation(projects.app.navigation.core)
  implementation(projects.app.navigation.navigationComposeTyped)

  implementation(libs.accompanist.navigationAnimation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.arrow.core)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.android)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.slimber)
}
