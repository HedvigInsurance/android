plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.apollo) apply false
  alias(libs.plugins.appIconBannerGenerator) apply false
  alias(libs.plugins.cacheFix) apply false
  alias(libs.plugins.composeCompilerGradlePlugin) apply false
  alias(libs.plugins.crashlytics) apply false
  alias(libs.plugins.datadog) apply false
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.doctor)
  alias(libs.plugins.googleServices) apply false
  alias(libs.plugins.kotlin) apply false
  alias(libs.plugins.kotlinJvm) apply false
  alias(libs.plugins.kotlinter) apply false
  alias(libs.plugins.license) apply false
  alias(libs.plugins.lintGradlePlugin) apply false
  alias(libs.plugins.serialization) apply false
  alias(libs.plugins.squareSortDependencies) apply false
}

apply {
  from(file("gradle/projectDependencyGraph.gradle"))
}
