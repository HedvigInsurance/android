package com.hedvig.android.data.importantmessages

import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.ktor.ktorClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import octopus.cache.Cache

/** Answers each request with the JSON [nextResponse] returns, or with HTTP 500 when it returns null. */
@OptIn(ExperimentalAtomicApi::class)
internal class TestApolloServer(private val nextResponse: suspend () -> String?) {
  private val requestCount = AtomicInt(0)

  val requests: Int
    get() = requestCount.load()

  /** Has a normalized cache, as the app's public client does, so a cache read would show as a missing request. */
  fun apolloClient(): ApolloClient = with(Cache) {
    ApolloClient.Builder()
      .serverUrl("https://example.test/graphql")
      .ktorClient(
        HttpClient(
          MockEngine {
            requestCount.incrementAndFetch()
            val json = nextResponse()
            respond(
              content = json ?: "",
              status = if (json == null) HttpStatusCode.InternalServerError else HttpStatusCode.OK,
              headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
          },
        ),
      )
      .cache(MemoryCacheFactory())
      .build()
  }
}

internal fun messagesJson(vararg messages: String): String =
  """{ "data": { "publicImportantMessages": [ ${messages.joinToString()} ] } }"""

internal fun messageJson(
  id: String,
  message: String = "Message $id",
  severity: String = "INFO",
  linkUrl: String? = null,
  linkButtonText: String = "Open",
): String {
  val linkInfo = if (linkUrl == null) {
    "null"
  } else {
    """{ "__typename": "LinkInfo", "url": "$linkUrl", "buttonText": "$linkButtonText" }"""
  }
  return """
    {
      "__typename": "PublicImportantMessage",
      "id": "$id",
      "message": "$message",
      "severity": "$severity",
      "linkInfo": $linkInfo
    }
    """.trimIndent()
}
