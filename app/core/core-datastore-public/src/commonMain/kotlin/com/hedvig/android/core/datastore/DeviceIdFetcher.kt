@file:OptIn(ExperimentalObjCName::class)

package com.hedvig.android.core.datastore

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Fetch the unique ID which identifies this device across logins but not across new app installations.
 * Wraps [DeviceIdDataStore] for Android/Jvm but delegates to an iOS implementation for the native target.
 *
 * Kotlin calls [fetch] from any thread, usually a background one. Kotlin cannot cancel a suspend function implemented
 * in Swift, so an iOS implementation must always complete, or the request waits forever.
 */
@ObjCName(name = "HedvigSharedDeviceIdFetcher", swiftName = "DeviceIdFetcher", exact = true)
interface DeviceIdFetcher {
  suspend fun fetch(): String?
}
