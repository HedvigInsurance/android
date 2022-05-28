package com.hedvig.app.util

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import org.junit.rules.ExternalResource
import org.koin.test.KoinTest
import org.koin.test.get

class ApolloCacheClearRule : ExternalResource(), KoinTest {
    override fun before() {
        val apolloClient: ApolloClient = get()
        apolloClient.apolloStore.clearAll()
    }
}
