@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apolloGiraffePublic)

  api(libs.apollo.api)
}

android {
  namespace = "com.hedvig.android.apollo.core"
}
