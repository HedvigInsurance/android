// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.buildTimeTracker)
    alias(libs.plugins.unusedResourcesRemover)
    alias(libs.plugins.cacheFix) apply false
    alias(libs.plugins.doctor)
}

subprojects {
    plugins.withType<com.android.build.gradle.BasePlugin> {
        project.apply(plugin = "org.gradle.android.cache-fix")
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }

    dependencies {
        classpath(libs.android.gradlePlugin)
        classpath(libs.apollo.gradlePlugin)
        classpath(libs.crashlytics.gradlePlugin)
        classpath(libs.googleServices.gradlePlugin)
        classpath(libs.kotlin.gradlePlugin)
        classpath(libs.licenses.gradlePlugin)
        classpath(libs.lokalise.gradlePlugin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven(
            "https://jitpack.io"
        )
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
