plugins {
  id("hedvig.android.application")
  id("hedvig.android.application.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.composeCompilerGradlePlugin)
}

android {
  namespace = "com.hedvig.android.design.showcase"

  buildFeatures {
    buildConfig = true
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
    val release by getting {
      signingConfig = debug.signingConfig // uncomment to run release build locally
      applicationIdSuffix = ".app"
      isMinifyEnabled = true
      isShrinkResources = true
      setProguardFiles(
        listOf(
          getDefaultProguardFile("proguard-android.txt"),
          "proguard-rules.pro",
        ),
      )
    }
  }
}

dependencies {
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.graphicsShapes)
  implementation(libs.coil.coil)
  implementation(libs.datadog.sdk.core)
  implementation(libs.zoomable)
  implementation(projects.designSystemHedvig)
  implementation(projects.loggingAndroid)
  implementation(projects.trackingCore)
  implementation(projects.trackingDatadog)
}
