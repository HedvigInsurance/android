plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.molecule)
}

dependencies {
  api(projects.app.molecule.moleculePublic)

  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.coroutines.core)
}

android {
  namespace = "com.hedvig.android.molecule.android"
}
