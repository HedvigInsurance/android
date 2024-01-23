plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.cacheFix) apply false
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.doctor)
  alias(libs.plugins.kotlin) apply false
  alias(libs.plugins.kotlinJvm) apply false
  alias(libs.plugins.kotlinter) apply false
  alias(libs.plugins.lintGradlePlugin) apply false
}

apply {
  from(file("gradle/projectDependencyGraph.gradle"))
}

// region https://github.com/jeremymailen/kotlinter-gradle#custom-ktlint-version
buildscript {
  configurations.classpath {
    resolutionStrategy {
      force(
        "com.pinterest.ktlint:ktlint-rule-engine:1.1.1",
        "com.pinterest.ktlint:ktlint-rule-engine-core:1.1.1",
        "com.pinterest.ktlint:ktlint-cli-reporter-core:1.1.1",
        "com.pinterest.ktlint:ktlint-cli-reporter-checkstyle:1.1.1",
        "com.pinterest.ktlint:ktlint-cli-reporter-json:1.1.1",
        "com.pinterest.ktlint:ktlint-cli-reporter-html:1.1.1",
        "com.pinterest.ktlint:ktlint-cli-reporter-plain:1.1.1",
        "com.pinterest.ktlint:ktlint-cli-reporter-sarif:1.1.1",
        "com.pinterest.ktlint:ktlint-ruleset-standard:1.1.1",
      )
    }
  }
}
// endregion
