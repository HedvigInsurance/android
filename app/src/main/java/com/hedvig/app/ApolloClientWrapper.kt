package com.hedvig.app

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCache
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import com.hedvig.android.owldroid.type.CustomType
import com.hedvig.app.util.apollo.ApolloTimberLogger
import com.hedvig.app.util.apollo.PromiscuousLocalDateAdapter
import com.hedvig.app.util.extensions.getAuthenticationToken
import okhttp3.OkHttpClient

class ApolloClientWrapper(val okHttpClient: OkHttpClient,
                          val context: Context,
                          val normalizedCacheFactory: NormalizedCacheFactory<LruNormalizedCache>) {

    val apolloClient: ApolloClient
        get() {
            return nullableApolloClient
                ?: createNewApolloClientInstance(okHttpClient, context.getAuthenticationToken(), normalizedCacheFactory)
        }


    private var nullableApolloClient: ApolloClient? = null

    fun invalidateApolloClient() {
        nullableApolloClient?.disableSubscriptions()
        nullableApolloClient = null
    }

    private fun createNewApolloClientInstance(okHttpClient: OkHttpClient,
                                              authToken: String?,
                                              normalizedCacheFactory: NormalizedCacheFactory<LruNormalizedCache>): ApolloClient {
        val builder = ApolloClient
            .builder()
            .serverUrl(BuildConfig.GRAPHQL_URL)
            .okHttpClient(okHttpClient)
            .addCustomTypeAdapter(CustomType.LOCALDATE, PromiscuousLocalDateAdapter())
            .subscriptionConnectionParams(mapOf("Authorization" to authToken))
            .subscriptionTransportFactory(
                WebSocketSubscriptionTransport.Factory(
                    BuildConfig.WS_GRAPHQL_URL,
                    okHttpClient
                )
            )
            .normalizedCache(normalizedCacheFactory)

        if (isDebug()) {
            builder.logger(ApolloTimberLogger())
        }

        val newApolloClient = builder.build()
        nullableApolloClient = newApolloClient
        return newApolloClient
    }
}
