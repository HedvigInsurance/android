plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(libs.slimber)
}

android {
  namespace = "com.hedvig.android.core.common.android.test"
}
