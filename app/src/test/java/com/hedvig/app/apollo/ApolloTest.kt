package com.hedvig.app.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.mockserver.MockServer
import com.hedvig.app.util.apollo.adapter.CUSTOM_SCALAR_ADAPTERS
import kotlinx.coroutines.test.TestScope

object ApolloTest {
    fun runTest(block: suspend TestScope.(MockServer, ApolloClient) -> Unit) {
        return kotlinx.coroutines.test.runTest {
            val mockServer = MockServer()
            val apolloClient = ApolloClient.Builder()
                .customScalarAdapters(CUSTOM_SCALAR_ADAPTERS)
                .serverUrl(mockServer.url())
                .build()
            block(mockServer, apolloClient)
            apolloClient.close()
            mockServer.stop()
        }
    }
}
