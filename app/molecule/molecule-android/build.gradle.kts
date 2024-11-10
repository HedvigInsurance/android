plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

dependencies {
  api(libs.androidx.lifecycle.viewModel)
  api(projects.moleculePublic)

  implementation(libs.coroutines.core)
  implementation(libs.molecule)
}
