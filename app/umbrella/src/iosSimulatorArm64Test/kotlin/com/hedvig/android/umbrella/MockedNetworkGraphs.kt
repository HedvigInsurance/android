package com.hedvig.android.umbrella

import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.Foundation.NSLock

/** The real bindings over [backend] instead of the network. */
internal fun FakeInputs.mockedGraph(backend: RecordingBackend): MockedNetworkIosGraph =
  createGraphFactory<MockedNetworkIosGraph.Factory>().create(
    appBuildConfig = IosAppBuildConfig(appFlavor),
    accessTokenFetcher = accessTokenFetcher,
    deviceIdFetcher = deviceIdFetcher,
    languageSource = languageSource,
    networkLogPolicy = networkLogPolicy.allowedFor(appFlavor),
    engine = backend.engine,
  )

/** One request as the HTTP engine received it. */
internal class SentRequest(val operation: String, val headers: Headers, val body: String)

/**
 * The backend at the HTTP engine: GraphQL answers by operation name; an operation without one is answered with HTTP
 * 500. Records every request.
 */
internal class RecordingBackend(initialAnswers: Map<String, String> = emptyMap()) {
  private val lock = NSLock()
  private val answers = initialAnswers.toMutableMap()
  private val sent = mutableListOf<SentRequest>()

  fun sent(): List<SentRequest> = locked { sent.toList() }

  fun answer(operation: String, json: String) = locked { answers[operation] = json }

  val engine = MockEngine { request ->
    val body = request.body.toByteArray().decodeToString()
    val operation = Json.parseToJsonElement(body).jsonObject["operationName"]!!.jsonPrimitive.content
    val json = locked {
      sent += SentRequest(operation, request.headers, body)
      answers[operation]
    }
    if (json == null) {
      respond("", HttpStatusCode.InternalServerError)
    } else {
      respond(json, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
    }
  }

  private fun <T> locked(block: () -> T): T {
    lock.lock()
    try {
      return block()
    } finally {
      lock.unlock()
    }
  }
}
