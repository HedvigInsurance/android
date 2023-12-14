plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.molecule)
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material3)
  api(libs.arrow.core)
  api(projects.coreIcons)

  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
}
