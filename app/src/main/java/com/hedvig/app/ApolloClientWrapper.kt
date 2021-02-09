package com.hedvig.app

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ScalarTypeAdapters
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCache
import com.apollographql.apollo.subscription.SubscriptionConnectionParams
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import com.hedvig.android.owldroid.type.CustomType
import com.hedvig.app.util.apollo.ApolloTimberLogger
import com.hedvig.app.util.apollo.JSONStringAdapter
import com.hedvig.app.util.apollo.PaymentMethodsApiResponseAdapter
import com.hedvig.app.util.apollo.PromiscuousLocalDateAdapter
import com.hedvig.app.util.extensions.getAuthenticationToken
import okhttp3.OkHttpClient

class ApolloClientWrapper(
    private val okHttpClient: OkHttpClient,
    private val context: Context,
    private val application: HedvigApplication,
    private val normalizedCacheFactory: NormalizedCacheFactory<LruNormalizedCache>,
) {

    val apolloClient: ApolloClient
        get() {
            return nullableApolloClient
                ?: createNewApolloClientInstance(
                    okHttpClient,
                    context.getAuthenticationToken(),
                    normalizedCacheFactory
                )
        }

    private var nullableApolloClient: ApolloClient? = null

    fun invalidateApolloClient() {
        nullableApolloClient?.disableSubscriptions()
        nullableApolloClient = null
    }

    private fun createNewApolloClientInstance(
        okHttpClient: OkHttpClient,
        authToken: String?,
        normalizedCacheFactory: NormalizedCacheFactory<LruNormalizedCache>,
    ): ApolloClient {
        val builder = ApolloClient
            .builder()
            .serverUrl(application.graphqlUrl)
            .okHttpClient(okHttpClient)
            .subscriptionConnectionParams(SubscriptionConnectionParams(mapOf("Authorization" to authToken)))
            .subscriptionTransportFactory(
                WebSocketSubscriptionTransport.Factory(
                    BuildConfig.WS_GRAPHQL_URL,
                    okHttpClient
                )
            )
            .normalizedCache(normalizedCacheFactory)

        CUSTOM_TYPE_ADAPTERS.customAdapters.forEach { (t, a) -> builder.addCustomTypeAdapter(t, a) }

        if (isDebug()) {
            builder.logger(ApolloTimberLogger())
        }

        val newApolloClient = builder.build()
        nullableApolloClient = newApolloClient
        return newApolloClient
    }

    companion object {
        val CUSTOM_TYPE_ADAPTERS = ScalarTypeAdapters(
            mapOf(
                CustomType.LOCALDATE to PromiscuousLocalDateAdapter(),
                CustomType.PAYMENTMETHODSRESPONSE to PaymentMethodsApiResponseAdapter(),
                CustomType.JSONSTRING to JSONStringAdapter(),
            )
        )
    }
}
