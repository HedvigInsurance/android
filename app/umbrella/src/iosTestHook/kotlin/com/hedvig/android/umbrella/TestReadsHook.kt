package com.hedvig.android.umbrella

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.iosdata.bridge.IosGraphHolder
import com.hedvig.android.iosdata.bridge.SessionReads
import com.hedvig.android.iosdata.bridge.component
import com.hedvig.android.iosdata.bridge.generalReadFailure
import com.hedvig.android.iosdata.bridge.iosGraphHolder
import com.hedvig.android.language.IosLanguageSource
import com.hedvig.android.language.Language
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.NetworkLogPolicy
import com.rickclephas.kmp.nativecoroutines.NativeSuspend
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.concurrent.AtomicInt
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import octopus.CbmNumberOfChatMessagesQuery
import platform.Foundation.NSLock

/*
 * Test hook for the iOS app's tests: the real Kotlin graph, network clients and read helper, with a canned HTTP
 * engine. Tests get real failures by driving the backend: a missing answer fails as GENERAL, a held answer and a
 * session signal end as SESSION_ENDED, and cancelling the awaiting task is a cancel.
 */

/**
 * The backend as tests describe it: an answer per GraphQL operation name, or an answer held back until released. An
 * operation without an answer fails with HTTP 500.
 */
class CannedBackend {
  private val lock = NSLock()
  private val answers = mutableMapOf<String, String>()
  private val held = mutableMapOf<String, CompletableDeferred<Unit>>()
  private val sent = mutableListOf<String>()
  private val authorizations = mutableListOf<String>()

  /** Answers [operation] with [json], a whole GraphQL response body such as `{"data": {...}}`. */
  fun answer(operation: String, json: String) {
    locked { answers[operation] = json }
  }

  /** Answers [operation] with HTTP 500. */
  fun fail(operation: String) {
    locked { answers.remove(operation) }
  }

  /** Holds [operation]'s answer until [release]. */
  fun hold(operation: String) {
    locked { held[operation] = CompletableDeferred() }
  }

  fun release(operation: String) {
    locked { held.remove(operation) }?.complete(Unit)
  }

  /** The operation names sent so far, in order. */
  fun sentOperations(): List<String> = locked { sent.toList() }

  /** The `Authorization` header of each request sent so far, in order, "" where there was none. */
  fun sentAuthorizations(): List<String> = locked { authorizations.toList() }

  internal val engine = MockEngine { request ->
    val body = Json.parseToJsonElement(request.body.toByteArray().decodeToString()).jsonObject
    val operation = body["operationName"]!!.jsonPrimitive.content
    val gate = locked {
      sent += operation
      authorizations += request.headers[HttpHeaders.Authorization].orEmpty()
      held[operation]
    }
    gate?.await()
    val json = locked { answers[operation] }
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

/**
 * An example of a feature's reads, written the way a feature writes them, for tests only: one member read and one
 * logged-out read of data-conversations' operation, which needs no feature module. It reaches Swift through
 * [ExampleReadsComponent] and the [exampleReads] extension, with no change to the holder or the graph.
 */
@Inject
class ExampleReads internal constructor(private val reads: SessionReads) {
  fun memberConversations(): NativeSuspend<ExampleConversations> = reads.read(
    "example.memberConversations",
    operation = OPERATION,
  ) {
    conversations(apolloClient)
  }

  fun publicConversations(): NativeSuspend<ExampleConversations> = reads.publicRead(
    "example.publicConversations",
    operation = OPERATION,
  ) {
    conversations(apolloClient)
  }

  private suspend fun conversations(client: ApolloClient): ExampleConversations {
    val response = client.query(CbmNumberOfChatMessagesQuery()).execute()
    val data = response.data ?: throw generalReadFailure(ErrorMessage(throwable = response.exception))
    return ExampleConversations(openCount = data.currentMember.conversations.count { it.isOpen })
  }

  private companion object {
    const val OPERATION = "CbmNumberOfChatMessages"
  }
}

/** What an example read returns: a plain class with a non-null field, as a feature's model would be. */
class ExampleConversations(val openCount: Int)

/** The example's accessor, contributed to every graph that links it. */
@ContributesTo(AppScope::class)
interface ExampleReadsComponent {
  val exampleReads: ExampleReads
}

/** The example reads, as Swift reaches a feature's reads: a method on the holder. */
fun IosGraphHolder.exampleReads(): ExampleReads = component<ExampleReadsComponent>().exampleReads

/** A graph over a [CannedBackend] and the holder the iOS app would have over it, already in use. */
class TestGraph internal constructor(
  internal val graph: MockedNetworkIosGraph,
  private val tokenFetches: AtomicInt,
) {
  val holder: IosGraphHolder = iosGraphHolder { graph }.also { it.graph }

  /** How often the graph asked for a token. */
  fun tokenFetchCount(): Int = tokenFetches.value
}

object TestGraphs {
  /**
   * A graph over the real bindings with [backend] as the HTTP engine and staging inputs: a fixed token and device id,
   * English, no body logging. With [tokenFetchNeverAnswers] the token fetch suspends forever, as a Swift fetcher that
   * never calls back does.
   */
  fun create(backend: CannedBackend, tokenFetchNeverAnswers: Boolean): TestGraph {
    val tokenFetches = AtomicInt(0)
    val graph = createGraphFactory<MockedNetworkIosGraph.Factory>().create(
      appBuildConfig = IosAppBuildConfig(Flavor.Staging),
      accessTokenFetcher = object : AccessTokenFetcher {
        override suspend fun fetch(): String? {
          tokenFetches.incrementAndGet()
          if (tokenFetchNeverAnswers) suspendCoroutine<Nothing> { }
          return "test-access-token"
        }
      },
      deviceIdFetcher = object : DeviceIdFetcher {
        override suspend fun fetch(): String? = "test-device-id"
      },
      languageSource = object : IosLanguageSource {
        override fun currentLanguage(): Language = Language.EN_SE
      },
      networkLogPolicy = NetworkLogPolicy(logsHttpBodies = false, logsGraphqlData = false, logsGraphqlErrors = true),
      engine = backend.engine,
    )
    return TestGraph(graph, tokenFetches)
  }
}
