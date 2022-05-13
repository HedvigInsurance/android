// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
    id("com.github.konifar.gradle.unused-resources-remover") version "0.3.3"
    id("org.gradle.android.cache-fix") version "2.5.3" apply false
    id("com.osacky.doctor") version "0.8.0"
    id("com.github.ben-manes.versions") version "0.41.0"
    id("nl.littlerobots.version-catalog-update") version "0.3.1"
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

val betaAlphaRc = Regex("""(alpha|beta|rc)""")
fun notStableVersion(version: String) = version.contains(betaAlphaRc)

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf { notStableVersion(candidate.version) }
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
