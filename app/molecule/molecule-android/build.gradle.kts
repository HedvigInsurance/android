plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
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
