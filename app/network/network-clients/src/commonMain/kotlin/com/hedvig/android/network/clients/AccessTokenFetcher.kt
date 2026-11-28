@file:OptIn(ExperimentalObjCName::class)

package com.hedvig.android.network.clients

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * The member's access token, or null when there is none. Called before every request that sends one, from any thread,
 * usually a background one.
 *
 * An iOS implementation returns null when no token is stored. The request then goes out without a token and a member
 * read fails as unauthenticated. When a token exists but cannot be produced right now, as when refreshing it failed,
 * it throws instead: the request is not sent and the read fails as a network failure. Kotlin cannot cancel a suspend
 * function implemented in Swift, so an iOS implementation must always complete, or the request waits forever.
 */
@ObjCName(name = "HedvigSharedAccessTokenFetcher", swiftName = "AccessTokenFetcher", exact = true)
interface AccessTokenFetcher {
  suspend fun fetch(): String?
}
