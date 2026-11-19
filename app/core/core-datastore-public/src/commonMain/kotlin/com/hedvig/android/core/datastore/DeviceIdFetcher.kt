@file:OptIn(ExperimentalObjCName::class)

package com.hedvig.android.core.datastore

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Fetch the unique ID which identifies this device across logins but not across new app installations.
 * Wraps [DeviceIdDataStore] for Android/Jvm but delegates to an iOS implementation for the native target.
 *
 * Kotlin calls [fetch] from any thread, usually a background one. An iOS implementation must complete it exactly once:
 * Kotlin cannot cancel it, so one that never completes holds its caller until that caller's own time limit.
 */
@ObjCName(name = "HedvigSharedDeviceIdFetcher", swiftName = "DeviceIdFetcher", exact = true)
interface DeviceIdFetcher {
  suspend fun fetch(): String?
}
