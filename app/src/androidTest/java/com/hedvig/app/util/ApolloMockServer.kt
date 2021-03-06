package com.hedvig.app.util

import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.ScalarTypeAdapters
import com.hedvig.app.CUSTOM_TYPE_ADAPTERS
import com.hedvig.app.TestApplication
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.IOException
import org.json.JSONObject
import org.junit.rules.ExternalResource

typealias ApolloResultProvider = (JSONObject) -> ApolloMockServerResult

fun apolloMockServer(vararg mocks: Pair<String, ApolloResultProvider>) =
    MockWebServer().apply {
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
                    mocks.firstOrNull { it.first == query }?.second
                        ?: return super.peek()

                return when (val result = dataProvider(variables)) {
                    ApolloMockServerResult.InternalServerError -> MockResponse().setResponseCode(500)
                    is ApolloMockServerResult.GraphQLError -> MockResponse().setBody(
                        jsonObjectOf(
                            "errors" to result.errors.map {
                                jsonObjectOf(
                                    "message" to it
                                )
                            }.toJsonArray()
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

inline fun apolloResponse(
    crossinline build: ApolloMockServerResponseBuilder.() -> ApolloMockServerResult,
): (JSONObject) -> ApolloMockServerResult =
    { vars -> build(ApolloMockServerResponseBuilder(vars)) }

sealed class ApolloMockServerResult {
    object InternalServerError : ApolloMockServerResult()

    data class GraphQLError(
        val errors: List<String>,
    ) : ApolloMockServerResult()

    data class GraphQLResponse(
        val body: String,
    ) : ApolloMockServerResult()
}

class ApolloMockServerResponseBuilder(
    val variables: JSONObject,
) {
    fun internalServerError() = ApolloMockServerResult.InternalServerError
    fun graphQLError(vararg errors: String) =
        ApolloMockServerResult.GraphQLError(errors.toList())

    fun success(data: Operation.Data) =
        ApolloMockServerResult.GraphQLResponse(
            data.toJson(
                scalarTypeAdapters = CUSTOM_TYPE_ADAPTERS
            )
        )

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

fun Operation.Data.toJson(
    indent: String = "",
    scalarTypeAdapters: ScalarTypeAdapters = ScalarTypeAdapters.DEFAULT,
): String {
    return try {
        MergingResponseWriter(scalarTypeAdapters).let { writer ->
            marshaller().marshal(writer)
            writer.toJson(indent)
        }
    } catch (e: IOException) {
        throw IllegalStateException(e)
    }
}
