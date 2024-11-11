plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  compose()
}

dependencies {
  api(libs.androidx.lifecycle.viewModel)
  api(projects.moleculePublic)

  implementation(libs.coroutines.core)
  implementation(libs.molecule)
}
