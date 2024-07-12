enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
  includeBuild("lokalise-gradle-plugin")
  repositories {
    google()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
  }
}

rootProject.name = "hedvigandroid"

private val File.gradleModuleDescendants: Sequence<File>
  get() = listFiles()
    ?.asSequence()
    ?.filter {
      it.isDirectory
    }?.flatMap {
      if (File(it, "build.gradle.kts").exists()) {
        sequenceOf(it)
      } else {
        it.gradleModuleDescendants
      }
    } ?: emptySequence()

rootProject.projectDir
  .resolve("app")
  .gradleModuleDescendants
  .forEach { file ->
    include(file.name)
    project(":${file.name}").projectDir = file
  }

include("design-showcase")
project(":design-showcase").projectDir = rootProject.projectDir.resolve("micro-apps").resolve("design-showcase")
include("hedvig-lint")
