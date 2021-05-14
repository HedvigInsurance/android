// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.dipien.releaseshub.gradle.plugin") version "2.0.2"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
    id("com.github.konifar.gradle.unused-resources-remover") version "0.3.3"
    id("org.gradle.android.cache-fix") version "2.3.1" apply false
}

subprojects {
    plugins.withType<com.android.build.gradle.BasePlugin> {
        project.apply(plugin = "org.gradle.android.cache-fix")
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
        classpath(BuildLibs.AndroidGradlePlugin)
        classpath(BuildLibs.GoogleServicesPlugin)
        classpath(BuildLibs.CrashlyticsPlugin)
        classpath(BuildLibs.KotlinPlugin)
        classpath(BuildLibs.ApolloPlugin)
        classpath(BuildLibs.LicensesPlugin)
        classpath(BuildLibs.LokalisePlugin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven(
            "https://maven.google.com"
        )
        maven(
            "https://jitpack.io"
        )
        jcenter()
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
