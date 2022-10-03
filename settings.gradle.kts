import org.gradle.internal.credentials.DefaultPasswordCredentials

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  @Suppress("UnstableApiUsage")
  includeBuild("build-logic")
  @Suppress("UnstableApiUsage")
  includeBuild("lokalise-gradle-plugin")
  repositories {
    google()
    mavenCentral()
    maven {
      url = uri("https://jitpack.io")
    }
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
    maven {
      url = uri("https://jitpack.io")
    }
    maven {
      url = uri("https://jitpack.io")
      name = "odysseyRepository"
      credentials(PasswordCredentials::class)
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
include(":feature-businessmodel")
include(":hanalytics")
include(":hanalytics-engineering")
include(":hanalytics-engineering-api")
include(":hanalytics-engineering-noop")
include(":hanalytics-test")
include(":hedvig-language")
include(":hedvig-market")
include(":notification-badge-data")
include(":testdata")
