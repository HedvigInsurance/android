plugins {
  id("hedvig.android.application")
  id("hedvig.android.application.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
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
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.graphicsShapes)
  implementation(libs.coil.coil)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.zoomable)
  implementation(projects.audioPlayer)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreUi)
  implementation(projects.loggingAndroid)
  implementation(projects.memberRemindersPublic)
  implementation(projects.memberRemindersUi)
  implementation(projects.notificationPermission)
  implementation(projects.trackingCore)
  implementation(projects.trackingDatadog)
}
