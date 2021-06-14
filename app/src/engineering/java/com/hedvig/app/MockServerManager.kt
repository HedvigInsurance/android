package com.hedvig.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.mockwebserver.MockWebServer

class MockServerManager {
    private var mockServer: MockWebServer? = null

    fun setupNewServerWithMocks(vararg mocks: Pair<String, ApolloResultProvider>) {
        mockServer?.close()
        mockServer = apolloMockServer(*mocks)
        CoroutineScope(Dispatchers.IO).launch {
            mockServer?.start(8080)
        }
    }
}
