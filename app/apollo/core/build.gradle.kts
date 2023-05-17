@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.coreCommon)

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
