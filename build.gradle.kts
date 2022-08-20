@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.buildTimeTracker)
  alias(libs.plugins.cacheFix) apply false
  alias(libs.plugins.doctor)
  alias(libs.plugins.kotlin) apply false
  alias(libs.plugins.ktlint) apply false
}

buildtimetracker {
  reporters {
    register("csv") {
      options["output"] = "build/times.csv"
      options["append"] = "true"
      options["header"] = "false"
    }

    register("summary") {
      options["ordered"] = "false"
      options["threshold"] = "50"
      options["barstyle"] = "unicode"
    }

    register("csvSummary") {
      options["csv"] = "build/times.csv"
    }
  }
}

apply {
  from(file("gradle/projectDependencyGraph.gradle"))
}
