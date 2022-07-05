package com.hedvig.android

import org.gradle.api.artifacts.VersionCatalog

internal val VersionCatalog.targetSdkVersion: Int
    @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
    get() = findVersion("targetSdkVersion").get().toString().toInt()

internal val VersionCatalog.minSdkVersion: Int
    @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
    get() = findVersion("minSdkVersion").get().toString().toInt()

internal val VersionCatalog.compileSdkVersion: Int
    @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
    get() = findVersion("compileSdkVersion").get().toString().toInt()

internal val VersionCatalog.composeVersion: String
    get() = findVersion("androidx-compose").get().toString()

internal val VersionCatalog.ktlintVersion: String
    get() = findVersion("ktlint").get().toString()
