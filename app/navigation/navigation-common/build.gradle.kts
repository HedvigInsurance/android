plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

kotlin {
  android {
    androidResources.enable = false
    withHostTest {}
  }
  sourceSets {
    commonMain.dependencies {
      api(libs.androidx.navigation3.runtime)
      api(libs.jetbrains.compose.runtime)
      implementation(libs.kotlin.reflect)
    }
    commonTest.dependencies {
      implementation(libs.kotlinx.serialization.core)
    }
    getByName("androidHostTest").dependencies {
      implementation(libs.androidx.junit)
      implementation(libs.junit)
      implementation(libs.robolectric)
    }
  }
}

// These run on the Android host only through their Robolectric subclasses, since they need the Android framework.
tasks.named { it == "testAndroidHostTest" }.withType<Test>().configureEach {
  filter {
    excludeTestsMatching("com.hedvig.android.navigation.compose.HedvigDeepLinkMatcherTest")
    excludeTestsMatching("com.hedvig.android.navigation.common.HedvigNavKeySavedStateTest")
  }
}
