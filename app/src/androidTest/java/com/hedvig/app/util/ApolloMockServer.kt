package com.hedvig.app.util

import androidx.test.espresso.IdlingRegistry
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.android.ApolloIdlingResource
import com.apollographql.apollo3.android.idlingResource
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.json.buildJsonString
import com.apollographql.apollo3.api.toJson
import com.hedvig.app.TestApplication
import com.hedvig.app.apolloClientModule
import com.hedvig.app.util.apollo.adapter.CUSTOM_SCALAR_ADAPTERS
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.junit.rules.ExternalResource
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest

interface ApolloResultProvider {
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

            private fun handleWebSocket(): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
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
}

inline fun apolloResponse(
    crossinline build: ApolloMockServerResponseBuilder.() -> ApolloMockServerResult,
): ApolloResultProvider = object : ApolloResultProvider {
    override fun provideResult(withVariables: JSONObject): ApolloMockServerResult {
        return ApolloMockServerResponseBuilder(withVariables).build()
    }
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

    fun graphQLError(vararg errors: JSONObject): ApolloMockServerResult.GraphQLError {
        return ApolloMockServerResult.GraphQLError(errors.toList())
    }

    fun success(operationData: Operation.Data): ApolloMockServerResult.GraphQLResponse {
        val dataJsonString = operationData.toJsonStringWithData()
        return ApolloMockServerResult.GraphQLResponse(dataJsonString)
    }

    fun success(data: JSONObject): ApolloMockServerResult.GraphQLResponse {
        return ApolloMockServerResult.GraphQLResponse(jsonObjectOf("data" to data).toString())
    }
}

class ApolloMockServerRule(
    vararg mocks: Pair<String, ApolloResultProvider>,
) : ExternalResource(), KoinTest {
    private val mockWebServer = apolloMockServer(*mocks)
    private val idlingResource = ApolloIdlingResource("ApolloIdlingResource")

    private val originalApolloClientModule = apolloClientModule

    private var testApolloModule: Module = constructTestApolloModule(idlingResource)

    override fun before() {
        mockWebServer.start(TestApplication.PORT)
        IdlingRegistry.getInstance().register(idlingResource)
        unloadKoinModules(originalApolloClientModule)
        loadKoinModules(testApolloModule)
    }

    override fun after() {
        mockWebServer.close()
        IdlingRegistry.getInstance().unregister(idlingResource)
        unloadKoinModules(testApolloModule)
        loadKoinModules(originalApolloClientModule)
    }
}

@Suppress("RemoveExplicitTypeArguments")
private fun constructTestApolloModule(
    idlingResource: ApolloIdlingResource,
): Module {
    return module {
        single<ApolloClient> {
            // Copy builder to not accumulate many idlingResource calls which crashes the tests.
            val builder: ApolloClient.Builder = get<ApolloClient.Builder>().copy()
            builder.idlingResource(idlingResource)
            builder.build()
        }
    }
}

private fun Operation.Data.toJsonStringWithData(
    customScalarAdapters: CustomScalarAdapters = CUSTOM_SCALAR_ADAPTERS,
): String {
    return buildJsonString {
        beginObject()
        name("data")
        this@toJsonStringWithData.toJson(jsonWriter = this@buildJsonString, customScalarAdapters)
        endObject()
    }
}
