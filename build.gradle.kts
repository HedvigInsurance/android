import org.gradle.api.internal.catalog.DelegatingProjectDependency

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.buildTimeTracker)
  alias(libs.plugins.cacheFix) apply false
  alias(libs.plugins.doctor)
  alias(libs.plugins.binaryCompatibilityValidator)
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

apiValidation {
  val allProjects = project
    .subprojects
    .map(Project::getName)
  val projectsToRunApiValidationOn = setOf(
    // Add projects here which we want to be part of API check.
    projects.hanalytics,
  )
    .map(DelegatingProjectDependency::getName)
    .toSet()
  val ignoredForApiValidation = allProjects.subtract(projectsToRunApiValidationOn)
  ignoredProjects.addAll(ignoredForApiValidation)
}

apply {
  from(file("gradle/projectDependencyGraph.gradle"))
}
