package com.hedvig.testutil

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.subscription.SubscriptionConnectionParams
import com.example.network.ApolloTimberLogger
import com.example.network.CUSTOM_TYPE_ADAPTERS
import org.koin.dsl.module

val apolloTestModule = module(override = true) {
    single {
        val builder = ApolloClient
            .builder()
            .serverUrl("http://localhost:8080/")
            .okHttpClient(get())
            .subscriptionConnectionParams(SubscriptionConnectionParams(mapOf("Authorization" to "testtoken")))
            .normalizedCache(get())

        CUSTOM_TYPE_ADAPTERS.customAdapters.forEach { (t, a) -> builder.addCustomTypeAdapter(t, a) }

        builder.logger(ApolloTimberLogger())
        builder.build()
    }
}

