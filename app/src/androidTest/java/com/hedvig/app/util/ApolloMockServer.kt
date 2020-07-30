package com.hedvig.app.util

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.toJson
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.TestApplication
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.junit.rules.ExternalResource

fun apolloMockServer(vararg mocks: Pair<String, () -> ApolloMockServerResult>) =
    MockWebServer().apply {
        dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val body = request.body.peek().readUtf8()
                val bodyAsJson = JSONObject(body)
                val query = bodyAsJson.getString("query")

                val dataProvider =
                    mocks.firstOrNull { it.first == query }?.second
                        ?: return super.peek()

                return when (val result = dataProvider()) {
                    ApolloMockServerResult.InternalServerError -> MockResponse().setResponseCode(500)
                    is ApolloMockServerResult.GraphQLError -> MockResponse().setBody(jsonObjectOf("errors" to result.errors).toString())
                    is ApolloMockServerResult.GraphQLResponse -> MockResponse().setBody(result.body)
                }
            }
        }
    }

inline fun apolloResponse(crossinline build: ApolloMockServerResponseBuilder.() -> ApolloMockServerResult): () -> ApolloMockServerResult =
    { build(ApolloMockServerResponseBuilder) }

sealed class ApolloMockServerResult {
    object InternalServerError : ApolloMockServerResult()

    data class GraphQLError(
        val errors: List<Error>
    ) : ApolloMockServerResult()

    data class GraphQLResponse(
        val body: String
    ) : ApolloMockServerResult()
}

object ApolloMockServerResponseBuilder {
    fun internalServerError() = ApolloMockServerResult.InternalServerError
    fun graphQLError(vararg errors: Error) =
        ApolloMockServerResult.GraphQLError(errors.toList())

    fun success(data: Operation.Data) =
        ApolloMockServerResult.GraphQLResponse(data.toJson(scalarTypeAdapters = ApolloClientWrapper.CUSTOM_TYPE_ADAPTERS))

    fun success(data: JSONObject) =
        ApolloMockServerResult.GraphQLResponse(jsonObjectOf("data" to data).toString())
}

class ApolloMockServerRule(
    vararg mocks: Pair<String, () -> ApolloMockServerResult>
) : ExternalResource() {
    val webServer = apolloMockServer(*mocks)

    override fun before() {
        webServer.start(TestApplication.PORT)
    }

    override fun after() {
        webServer.close()
    }
}
