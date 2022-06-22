package com.hedvig.app.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.json.buildJsonString
import com.apollographql.apollo3.api.toJson
import com.apollographql.apollo3.api.toJsonString
import com.apollographql.apollo3.mockserver.MockServer
import com.hedvig.app.util.apollo.adapter.CUSTOM_SCALAR_ADAPTERS
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

fun runApolloTest(
    extraApolloClientConfiguration: ApolloClient.Builder.() -> ApolloClient.Builder = { this },
    block: suspend TestScope.(MockServer, ApolloClient) -> Unit,
) {
    return kotlinx.coroutines.test.runTest {
        val mockServer = MockServer()
        val apolloClient = ApolloClient.Builder()
            .requestedDispatcher(StandardTestDispatcher(testScheduler))
            .customScalarAdapters(CUSTOM_SCALAR_ADAPTERS)
            .serverUrl(mockServer.url())
            .extraApolloClientConfiguration()
            .build()
        block(mockServer, apolloClient)
        apolloClient.close()
        mockServer.stop()
    }
}

fun Operation.Data.toJsonStringWithData(
    customScalarAdapters: CustomScalarAdapters = CUSTOM_SCALAR_ADAPTERS,
): String {
    return buildJsonString {
        beginObject()
        name("data")
        this@toJsonStringWithData.toJson(jsonWriter = this@buildJsonString, customScalarAdapters)
        endObject()
    }
}

// toJsonString gives a json representation, meaning there's extra '"' surrounding the json. This removes it since the
//  test builders require the json representation without those extra quotation marks.
fun <T> Adapter<T>.toJsonStringForTestBuilder(
    data: T,
    customScalarAdapters: CustomScalarAdapters = CUSTOM_SCALAR_ADAPTERS,
): String {
    val jsonString = toJsonString(data, customScalarAdapters)
    return if ((jsonString.first() == '"') && (jsonString.last() == '"')) {
        jsonString.drop(1).dropLast(1)
    } else {
        jsonString
    }
}
