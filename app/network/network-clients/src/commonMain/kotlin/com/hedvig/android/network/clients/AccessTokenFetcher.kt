package com.hedvig.android.network.clients

interface AccessTokenFetcher {
  suspend fun fetch(): String?
}
