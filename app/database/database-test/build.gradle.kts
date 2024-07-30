plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.room.runtime)
  api(libs.sqlite.bundled)
  implementation(libs.junit)
}
