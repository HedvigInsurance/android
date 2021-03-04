// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
    id("com.github.ben-manes.versions") version "0.36.0"
}

buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        jcenter()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath("com.google.gms:google-services:4.3.5")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.5.0")
        classpath(kotlin("gradle-plugin", version = Dependencies.Versions.kotlin))
        classpath("com.apollographql.apollo:apollo-gradle-plugin:${Dependencies.Versions.apollo}")
        classpath("com.jaredsburrows:gradle-license-plugin:0.8.90")
        classpath("com.hedvig.android:lokalise-plugin:1.4")
    }
}

allprojects {
    repositories {
        google()
        mavenLocal()
        jcenter()
        maven(
            "https://maven.google.com"
        )
        maven(
            "https://jitpack.io"
        )
        maven(
            "https://dl.bintray.com/hedvig/hedvig-java"
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

tasks.named("dependencyUpdates", com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class.java)
    .configure {
        rejectVersionIf {
            candidate.version.contains("alpha", true)
                || candidate.version.contains("rc", true)
                || candidate.version.contains("beta", true)
        }
    }
