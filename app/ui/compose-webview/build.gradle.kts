plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.runtime)
}
