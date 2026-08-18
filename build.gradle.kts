plugins {
  // Every other plugin's classpath is supplied by the `hedvig.settings` convention plugin (see
  // build-logic/convention/build.gradle.kts), so project scripts apply them by id without a version.
  // Only plugins applied to the root project itself are declared here.
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.doctor)
}

apply {
  from(file("gradle/projectDependencyGraph.gradle"))
}

dependencyAnalysis {
  issues {
    all {
      onUsedTransitiveDependencies {
        severity("ignore")
      }
    }
  }
}
