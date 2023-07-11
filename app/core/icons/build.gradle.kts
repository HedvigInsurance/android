@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

android {
  namespace = "com.hedvig.android.core.icons"
}

dependencies {
  implementation(libs.androidx.compose.materialIconsCore)
  implementation(libs.androidx.compose.uiCore)
}
