package com.hedvig.android.core.datastore

/**
 * Fetch the unique ID which identifies this device across logins but not across new app installations.
 * Wraps [DeviceIdDataStore] for Android/Jvm but delegates to an iOS implementation for the native target
 */
interface DeviceIdFetcher {
  suspend fun fetch(): String?
}
