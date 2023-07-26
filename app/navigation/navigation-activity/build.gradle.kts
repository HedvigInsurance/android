plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

android {
  namespace = "com.hedvig.android.navigation.activity"
}

dependencies {
  implementation(projects.coreCommonAndroidPublic)
}
