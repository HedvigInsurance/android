package com.hedvig.android.feature

import com.hedvig.android.apollo.NetworkCacheManager

val NoopNetworkCacheManager = object : NetworkCacheManager {
  override fun clearCache() {}
}
