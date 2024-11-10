hedvig {
  compose()
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  api(libs.androidx.lifecycle.viewModel)
  api(projects.moleculePublic)

  implementation(libs.coroutines.core)
  implementation(libs.molecule)
}
