plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apolloGiraffePublic)
  implementation(libs.adyen)
}

android {
  namespace = "com.hedvig.lib.testdata"
}
