plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

android {
  defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
}

// The accessibility checks are a report, not a gate: a violation should be visible without blocking
// whoever ran them. Results land in build/reports/androidTests/connected/release/index.html either way.
tasks.configureEach {
  if (this is VerificationTask && name.startsWith("connected")) {
    ignoreFailures = true
  }
}

// Every accessibility test in the project lives in this one module, behind the single
// `./gradlew accessibilityChecks` task. To cover a `public` component owned by another module, add it
// here as an `androidTestImplementation` and write the test alongside the existing ones; nothing else
// is wired up. This module is not named `feature-*`, so the feature-isolation rule in
// HedvigGradlePlugin does not apply and it may depend on feature modules directly. A feature's
// `internal` screens are out of reach from here by design; see CLAUDE.md for why that is accepted.
dependencies {
  androidTestImplementation(libs.androidx.compose.uiTestJunit4)
  androidTestImplementation(libs.androidx.compose.uiTestJunit4Accessibility)
  androidTestImplementation(libs.androidx.compose.uiTestManifest)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.junit)
  androidTestImplementation(projects.designSystemHedvig)
}
