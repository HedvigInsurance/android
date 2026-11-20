@file:OptIn(ExperimentalObjCName::class)

package com.hedvig.android.network.clients

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * The member's access token, or null when there is none. Called before every request that sends one, from any thread,
 * usually a background one; when it throws, the request fails without being sent. Kotlin cannot cancel a suspend
 * function implemented in Swift, so an iOS implementation must always complete, or the request waits forever.
 */
@ObjCName(name = "HedvigSharedAccessTokenFetcher", swiftName = "AccessTokenFetcher", exact = true)
interface AccessTokenFetcher {
  suspend fun fetch(): String?
}
