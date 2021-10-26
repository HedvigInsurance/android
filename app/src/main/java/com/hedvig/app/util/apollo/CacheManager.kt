package com.hedvig.app.util.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.ApolloStoreOperation
import timber.log.Timber

class CacheManager(
    private val apolloClient: ApolloClient
) {
    fun clearCache() {
        apolloClient.clearHttpCache()
        apolloClient.clearNormalizedCache(object : ApolloStoreOperation.Callback<Boolean> {
            override fun onFailure(t: Throwable) {
                Timber.e(t)
            }

            override fun onSuccess(result: Boolean) {
                Timber.d("Clear cache result: $result")
            }
        })
    }
}
