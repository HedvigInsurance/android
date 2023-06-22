plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  @Suppress("DSL_SCOPE_VIOLATION")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.navigation.compose.typed"
}

dependencies {
  api(libs.androidx.navigation.common)
  api(libs.kiwi.navigationCompose)
  implementation(libs.accompanist.navigationAnimation)
  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
}
