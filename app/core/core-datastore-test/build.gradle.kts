plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.coroutines.core)
  implementation(libs.coroutines.test)
  implementation(libs.junit)
  implementation(projects.coreDatastorePublic)
}
