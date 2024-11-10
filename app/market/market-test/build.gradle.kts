hedvig {
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(projects.marketCore)
}
