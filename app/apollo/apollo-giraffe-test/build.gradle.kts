@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.apollo.api)

  implementation(projects.apolloGiraffePublic)
}
