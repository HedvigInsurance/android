enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  @Suppress("UnstableApiUsage")
  includeBuild("build-logic")
  @Suppress("UnstableApiUsage")
  includeBuild("lokalise-gradle-plugin")
  repositories {
    google()
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
    maven {
      url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  @Suppress("UnstableApiUsage")
  repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven {
      url = uri("https://jitpack.io")
    }
    maven {
      url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
  }
}

rootProject.name = "hedvigandroid"

include(":apollo")
include(":app")
include(":core-common")
include(":core-datastore")
include(":core-design-system")
include(":core-resources")
include(":core-ui")
include(":hanalytics")
include(":hanalytics-engineering")
include(":hanalytics-engineering-noop")
include(":hanalytics-test")
include(":hedvig-market")
include(":testdata")
