plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.application")
}

hedvig {
  compose()
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

  sourceSets {
    getByName("main") {
      res.srcDirs("resources/main")
    }
  }

  buildTypes {
    @Suppress("UNUSED_VARIABLE")
    val debug by getting {
      isDebuggable = true
    }
    val release by getting {
      signingConfig = debug.signingConfig
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
  implementation(libs.androidx.compose.runtime)
  implementation(libs.datadog.sdk.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.timber)
  implementation(projects.composeUi)
  implementation(projects.designSystemHedvig)
  implementation(projects.loggingAndroid)
  implementation(projects.trackingCore)
  implementation(projects.trackingDatadog)
}
