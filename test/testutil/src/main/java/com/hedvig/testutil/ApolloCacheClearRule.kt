package com.hedvig.testutil

import com.apollographql.apollo.ApolloClient
import org.junit.rules.ExternalResource
import org.koin.test.KoinTest
import org.koin.test.inject

class ApolloCacheClearRule : ExternalResource(), KoinTest {
    private val apolloClient: ApolloClient by inject()
    override fun before() {
        apolloClient
            .clearNormalizedCache()
    }
}
