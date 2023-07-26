@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.coreCommonPublic)

  api(libs.apollo.api)
  api(libs.apollo.runtime)
  api(libs.arrow.core)
  api(libs.coroutines.core)
  api(libs.okhttp.core)

  implementation(libs.apollo.normalizedCache)
}

android {
  namespace = "com.hedvig.android.apollo.core"
}
