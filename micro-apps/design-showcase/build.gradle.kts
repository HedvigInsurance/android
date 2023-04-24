plugins {
  id("hedvig.android.application")
  id("hedvig.android.application.compose")
  id("hedvig.android.ktlint")
}

android {
  namespace = "com.hedvig.android.design.showcase"

  defaultConfig {
    applicationId = "com.hedvig.android.design.showcase"

    versionCode = 1
    versionName = "0.0.1"
  }

  buildTypes {
    @Suppress("UNUSED_VARIABLE")
    val debug by getting {
      isDebuggable = true
    }
  }
}

dependencies {
  implementation(projects.coreDesignSystem)
  implementation(projects.coreUi)

  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.other.activityCompose)
}
