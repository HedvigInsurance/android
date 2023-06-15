plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.navigation.core"
}

dependencies {
  implementation(projects.app.core.common)
  implementation(projects.app.core.resources)

  implementation(libs.androidx.annotation)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
}
