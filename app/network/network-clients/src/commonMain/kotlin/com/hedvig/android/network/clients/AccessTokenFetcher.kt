@file:OptIn(ExperimentalObjCName::class)

package com.hedvig.android.network.clients

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * The member's access token, or null when there is none. Called before every request that sends one, from any thread,
 * usually a background one. Kotlin cannot cancel a suspend function implemented in Swift, so an iOS implementation must
 * always complete, or the request waits forever.
 *
 * An iOS implementation returns null when it has no usable token, including when refreshing one failed, and never
 * throws. The request then goes out without a token, the backend answers unauthenticated, and the read fails as
 * unauthenticated, which is what leads the app to log the member out. A throw instead fails the request without
 * sending it, and the read reports it as a network failure.
 */
@ObjCName(name = "HedvigSharedAccessTokenFetcher", swiftName = "AccessTokenFetcher", exact = true)
interface AccessTokenFetcher {
  suspend fun fetch(): String?
}
