@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.navigation.compose.typed.ext"
}

dependencies {
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.kotlinx.serialization.core)
}
