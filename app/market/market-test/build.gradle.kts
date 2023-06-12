plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.market.marketCore)

  implementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.market.test"
}
