package com.hedvig.android.shareddi

/**
 * Fetch the unique ID which identifies this device across logins but not across new app installations
 */
interface DeviceIdFetcher {
  suspend fun fetch(): String?
}
