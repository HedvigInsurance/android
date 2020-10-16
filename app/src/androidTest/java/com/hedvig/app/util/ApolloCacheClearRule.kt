package com.hedvig.app.util

import com.hedvig.app.ApolloClientWrapper
import org.junit.rules.ExternalResource
import org.koin.test.KoinTest
import org.koin.test.inject

class ApolloCacheClearRule : ExternalResource(), KoinTest {
    private val apolloClientWrapper: ApolloClientWrapper by inject()
    override fun before() {
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }
}
