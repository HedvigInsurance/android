plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

android {
  // Compose's Recomposer logs via android.util.Log in unit tests; return defaults instead of throwing.
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  // api: ConsentBadge.from and rememberAnalyticsConsentDecision take AnalyticsConsent and
  // SettingsDataStore, so a consumer cannot call them without seeing these.
  api(projects.dataSettingsDatastorePublic)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.coroutines.core)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.loggingTest)
  testImplementation(projects.moleculeTest)
  testImplementation(projects.theme)
}
