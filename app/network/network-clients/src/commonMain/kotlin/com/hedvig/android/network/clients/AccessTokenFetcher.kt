@file:OptIn(ExperimentalObjCName::class)

package com.hedvig.android.network.clients

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * The member's access token, or null when there is none. Called before every request that sends one, from any thread,
 * usually a background one; when it throws, the request fails without being sent. An iOS implementation must complete
 * it exactly once: Kotlin cannot cancel it, so one that never completes holds its caller until that caller's own time
 * limit.
 */
@ObjCName(name = "HedvigSharedAccessTokenFetcher", swiftName = "AccessTokenFetcher", exact = true)
interface AccessTokenFetcher {
  suspend fun fetch(): String?
}
