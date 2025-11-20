package com.hedvig.android.shareddi

interface AccessTokenFetcher {
  suspend fun fetch(): String?
}
