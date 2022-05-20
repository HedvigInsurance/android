package com.hedvig.app.util

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.json.buildJsonString
import com.apollographql.apollo3.api.toJson
import com.hedvig.app.CUSTOM_SCALAR_ADAPTERS
import com.hedvig.app.TestApplication
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.junit.rules.ExternalResource

fun interface ApolloResultProvider {
    fun provideResult(withVariables: JSONObject): ApolloMockServerResult
}

data class QueryToResultProvider(
    val queryDocument: String,
    val apolloResultProvider: ApolloResultProvider,
) {
    companion object {
        fun fromPair(pair: Pair<String, ApolloResultProvider>): QueryToResultProvider {
            return QueryToResultProvider(pair.first, pair.second)
        }
    }
}

fun apolloMockServer(vararg mocks: Pair<String, ApolloResultProvider>): MockWebServer {
    @Suppress("NAME_SHADOWING")
    val mocks: List<QueryToResultProvider> = mocks.map(QueryToResultProvider::fromPair)
    return MockWebServer().apply {
        dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.headers.find { it.first == "Upgrade" && it.second == "websocket" } != null) {
                    return handleWebSocket()
                }
                return handleHttp(request)
            }

            private fun handleHttp(request: RecordedRequest): MockResponse {
                val body = request.body.peek().readUtf8()
                val bodyAsJson = JSONObject(body)
                val query = bodyAsJson.getString("query")
                val variables = if (bodyAsJson.has("variables")) {
                    bodyAsJson.getJSONObject("variables")
                } else {
                    JSONObject()
                }

                val dataProvider =
                    mocks.firstOrNull { it.queryDocument == query }?.apolloResultProvider
                        ?: return super.peek()

                return when (val result = dataProvider.provideResult(withVariables = variables)) {
                    ApolloMockServerResult.InternalServerError -> MockResponse().setResponseCode(500)
                    is ApolloMockServerResult.GraphQLError -> MockResponse().setBody(
                        jsonObjectOf(
                            "errors" to result.errors.toJsonArray()
                        ).toString()
                    )
                    is ApolloMockServerResult.GraphQLResponse -> MockResponse().setBody(result.body)
                }
            }

            private fun handleWebSocket() = MockResponse()
                .withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        val message = JSONObject(text)
                        if (message.getString("type") == "connection_init") {
                            webSocket.send(
                                jsonObjectOf(
                                    "type" to "connection_ack"
                                ).toString()
                            )
                        }
                    }
                })
        }
    }
}

inline fun apolloResponse(
    crossinline build: ApolloMockServerResponseBuilder.() -> ApolloMockServerResult,
): ApolloResultProvider = ApolloResultProvider { vars ->
    build(ApolloMockServerResponseBuilder(vars))
}

sealed class ApolloMockServerResult {
    object InternalServerError : ApolloMockServerResult()

    data class GraphQLError(
        val errors: List<JSONObject>,
    ) : ApolloMockServerResult()

    data class GraphQLResponse(
        val body: String,
    ) : ApolloMockServerResult()
}

class ApolloMockServerResponseBuilder(
    val variables: JSONObject,
) {
    fun internalServerError() = ApolloMockServerResult.InternalServerError
    fun graphQLError(vararg errors: JSONObject) =
        ApolloMockServerResult.GraphQLError(errors.toList())

    fun success(operationData: Operation.Data): ApolloMockServerResult.GraphQLResponse {
        return ApolloMockServerResult.GraphQLResponse(
            operationData.toJsonString(
                customScalarAdapters = CUSTOM_SCALAR_ADAPTERS,
            )
        )
    }

    fun success(data: JSONObject) =
        ApolloMockServerResult.GraphQLResponse(jsonObjectOf("data" to data).toString())
}

class ApolloMockServerRule(
    vararg mocks: Pair<String, ApolloResultProvider>,
) : ExternalResource() {
    val webServer = apolloMockServer(*mocks)

    override fun before() {
        webServer.start(TestApplication.PORT)
    }

    override fun after() {
        webServer.close()
    }
}

// todo replace with com.apollographql.apollo3.api.toJsonString when it's stable
fun Operation.Data.toJsonString(customScalarAdapters: CustomScalarAdapters = CustomScalarAdapters.Empty): String {
    return buildJsonString {
        this@toJsonString.toJson(jsonWriter = this@buildJsonString, customScalarAdapters = customScalarAdapters)
    }
}
