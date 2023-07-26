plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.molecule)
}

dependencies {
  implementation(libs.coroutines.core)
}
