plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  // api: ConsentBadge.from takes an AnalyticsConsent, so a consumer cannot call it without seeing it.
  api(projects.dataSettingsDatastorePublic)
  implementation(libs.androidx.compose.foundation)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
}
