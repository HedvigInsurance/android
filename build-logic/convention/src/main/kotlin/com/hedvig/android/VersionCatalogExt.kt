package com.hedvig.android

import org.gradle.api.artifacts.VersionCatalog

internal val VersionCatalog.targetSdkVersion: Int
    get() = findVersion("targetSdkVersion").get().toString().toInt()

internal val VersionCatalog.minSdkVersion: Int
    get() = findVersion("minSdkVersion").get().toString().toInt()

internal val VersionCatalog.compileSdkVersion: Int
    get() = findVersion("compileSdkVersion").get().toString().toInt()

internal val VersionCatalog.composeVersion: String
    get() = findVersion("androidx-compose").get().toString()
