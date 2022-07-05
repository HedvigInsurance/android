@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.buildTimeTracker)
  alias(libs.plugins.cacheFix) apply false
  alias(libs.plugins.doctor)
}

subprojects {
  plugins.withType<com.android.build.gradle.BasePlugin> {
    project.apply(plugin = "org.gradle.android.cache-fix")
  }
}

buildscript {
  dependencies {
    classpath(libs.android.gradlePlugin)
    classpath(libs.kotlin.gradlePlugin)
  }
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
