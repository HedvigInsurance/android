plugins {
  id("hedvig.android.feature")
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreDesignSystem)
  implementation(projects.languageCore)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.foreverUi)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
}
