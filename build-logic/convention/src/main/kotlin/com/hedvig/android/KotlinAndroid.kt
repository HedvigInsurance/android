package com.hedvig.android

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *>,
    addStandardBuildTypes: Boolean,
) {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    commonExtension.apply {
        compileSdk = libs.compileSdkVersion

        defaultConfig {
            minSdk = libs.minSdkVersion
        }

        compileOptions {
            @Suppress("UnstableApiUsage")
            isCoreLibraryDesugaringEnabled = true
            @Suppress("UnstableApiUsage")
            sourceCompatibility = JavaVersion.VERSION_11
            @Suppress("UnstableApiUsage")
            targetCompatibility = JavaVersion.VERSION_11
        }

        if (addStandardBuildTypes) {
            buildTypes {
                maybeCreate("staging")
                maybeCreate("pullrequest")

                named("debug") {}
                named("staging") {}
                named("pullrequest") {}
                named("release") {}
            }
        }

        kotlinOptions {
            // Treat all Kotlin warnings as errors (disabled by default)
            allWarningsAsErrors = properties["warningsAsErrors"] as? Boolean ?: false

            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
                // Enable experimental coroutines APIs, including Flow
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlin.Experimental",
                // Enable experimental kotlinx serialization APIs
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                // Fixes "Inheritance from an interface with '@JvmDefault' members is only allowed with -Xjvm-default option"
                "-Xjvm-default=enable",
            )

            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("coreLibraryDesugaring").get())
    }
}

private fun CommonExtension<*, *, *, *>.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}
