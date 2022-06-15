package com.hedvig.app.util.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.http.httpCache
import com.apollographql.apollo3.cache.normalized.apolloStore
import d
import e

class CacheManager(
    private val apolloClient: ApolloClient
) {
    fun clearCache() {
        apolloClient.httpCache.clearAll()
        val didClearAllRecords = apolloClient.apolloStore.clearAll()
        if (didClearAllRecords) {
            d { "Did clear entire apolloStore cache" }
        } else {
            e { "Failed to clear apolloStore cache" }
        }
    }
}
