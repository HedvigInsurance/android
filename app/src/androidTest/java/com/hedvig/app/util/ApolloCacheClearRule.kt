package com.hedvig.app.util

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import org.junit.rules.ExternalResource
import org.koin.test.KoinTest
import org.koin.test.inject

class ApolloCacheClearRule : ExternalResource(), KoinTest {
    private val apolloClient: ApolloClient by inject()
    override fun before() {
        apolloClient.apolloStore.clearAll()
    }
}
