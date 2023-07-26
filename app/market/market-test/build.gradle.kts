plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.marketCore)
}

android {
  namespace = "com.hedvig.android.market.test"
}
