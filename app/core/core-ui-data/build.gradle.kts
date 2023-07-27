@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.core.uidata"
}

dependencies {
  implementation(projects.apolloOctopusPublic)

  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.serialization.core)
}
