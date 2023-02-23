plugins {
  id("hedvig.android.application")
  id("hedvig.android.application.compose")
  id("hedvig.android.ktlint")
  id("org.jetbrains.kotlin.android")
}

@Suppress("UnstableApiUsage")
android {
  namespace = "com.hedvig.android.design.showcase"

  buildFeatures {
    buildConfig = false
    viewBinding = false
    aidl = false
    renderScript = false
  }

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

  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.compose.material3)
}
