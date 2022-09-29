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
include(":core-common-android")
include(":core-datastore")
include(":core-design-system")
include(":core-resources")
include(":core-ui")
include(":feature-businessmodel")
include(":hanalytics:hanalytics")
include(":hanalytics:hanalytics-engineering")
include(":hanalytics:hanalytics-engineering-api")
include(":hanalytics:hanalytics-engineering-noop")
include(":hanalytics:hanalytics-feature-flags")
include(":hanalytics:hanalytics-feature-flags-test")
include(":hedvig-language")
include(":hedvig-market")
include(":notification-badge-data")
include(":testdata")
