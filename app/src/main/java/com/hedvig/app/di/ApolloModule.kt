package com.hedvig.app.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCache
import com.apollographql.apollo.subscription.SubscriptionConnectionParams
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import com.hedvig.app.CUSTOM_TYPE_ADAPTERS
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.isDebug
import com.hedvig.app.util.apollo.ApolloTimberLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ApolloModule {

    @Provides
    fun provideApolloClient(
        okHttpClient: OkHttpClient,
        normalizedCacFactory: NormalizedCacheFactory<LruNormalizedCache>,
        authenticationTokenService: AuthenticationTokenService,
        @ApplicationContext context: Context
    ): ApolloClient {
        val builder = ApolloClient
            .builder()
            .serverUrl("https://graphql.dev.hedvigit.com/graphql")
            .okHttpClient(okHttpClient)
            .subscriptionConnectionParams {
                SubscriptionConnectionParams(mapOf("Authorization" to authenticationTokenService.authenticationToken))
            }
            .subscriptionTransportFactory(
                WebSocketSubscriptionTransport.Factory(
                    context.getString(R.string.WS_GRAPHQL_URL),
                    okHttpClient
                )
            )
            .normalizedCache(normalizedCacFactory)

        CUSTOM_TYPE_ADAPTERS.customAdapters.forEach { (t, a) -> builder.addCustomTypeAdapter(t, a) }

        if (isDebug()) {
            builder.logger(ApolloTimberLogger())
        }

        return builder.build()
    }
}
