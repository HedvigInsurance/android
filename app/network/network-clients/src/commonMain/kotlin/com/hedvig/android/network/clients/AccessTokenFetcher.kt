package com.hedvig.android.network.clients

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines

interface AccessTokenFetcher {
  @NativeCoroutines
  suspend fun fetch(): String?
}
