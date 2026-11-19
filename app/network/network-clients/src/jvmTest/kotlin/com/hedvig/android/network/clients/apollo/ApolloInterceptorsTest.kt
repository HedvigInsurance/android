package com.hedvig.android.network.clients.apollo

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.single
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.json.JsonWriter
import com.apollographql.apollo.testing.QueueTestNetworkTransport
import com.apollographql.apollo.testing.enqueueTestResponse
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.test.TestAuthTokenService
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Runs queued GraphQL responses through an [ApolloClient] carrying [addHedvigInterceptors], the chain the provided
 * Apollo client is built with.
 */
@OptIn(ApolloExperimental::class)
internal class ApolloInterceptorsTest {
  private val rumLogger = RecordingRumLogger()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule(testRumLogger = rumLogger)

  private val authTokenService = TestAuthTokenService()

  @Test
  fun `the interceptors are logout on unauthenticated, then logging`() {
    val apolloClient = apolloClient(isDemoMode = false)

    assertThat(apolloClient.interceptors.map { it::class }).containsExactly(
      LogoutOnUnauthenticatedInterceptor::class,
      LoggingInterceptor::class,
    )
  }

  @Test
  fun `an UNAUTHENTICATED error logs the member out and is not reported as an error`() = runTest {
    val apolloClient = apolloClient(isDemoMode = false)
    apolloClient.enqueueTestResponse(TestQuery, errors = listOf(unauthenticatedError))

    apolloClient.query(TestQuery).execute()

    assertThat(authTokenService.authStatus.value).isEqualTo(AuthStatus.LoggedOut)
    assertThat(rumLogger.errors).isEmpty()
  }

  @Test
  fun `an UNAUTHENTICATED error in demo mode does not log out`() = runTest {
    val apolloClient = apolloClient(isDemoMode = true)
    apolloClient.enqueueTestResponse(TestQuery, errors = listOf(unauthenticatedError))

    apolloClient.query(TestQuery).execute()

    assertThat(authTokenService.authStatus.value).isNull()
  }

  @Test
  fun `another error does not log out and is reported as a network error`() = runTest {
    val apolloClient = apolloClient(isDemoMode = false)
    apolloClient.enqueueTestResponse(TestQuery, errors = listOf(Error.Builder("Something failed").build()))

    apolloClient.query(TestQuery).execute()

    assertThat(authTokenService.authStatus.value).isNull()
    assertThat(rumLogger.errors).single().isEqualTo(
      ErrorSource.NETWORK to "GraphQL errors for Test: [Something failed]",
    )
  }

  private fun apolloClient(isDemoMode: Boolean): ApolloClient {
    return ApolloClient.Builder()
      .networkTransport(QueueTestNetworkTransport())
      .addHedvigInterceptors(authTokenService = authTokenService, demoManager = FakeDemoManager(isDemoMode))
      .build()
  }

  private val unauthenticatedError = Error.Builder(
    "Unauthenticated",
  ).putExtension("errorType", "UNAUTHENTICATED").build()
}

private class FakeDemoManager(isDemoMode: Boolean) : DemoManager {
  private val demoMode = MutableStateFlow(isDemoMode)

  override fun isDemoMode(): Flow<Boolean> = demoMode

  override suspend fun setDemoMode(demoMode: Boolean) {
    this.demoMode.value = demoMode
  }
}

private class RecordingRumLogger : RumLogger {
  val errors = mutableListOf<Pair<ErrorSource, String>>()

  override fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {}

  override fun logError(
    message: String,
    source: ErrorSource,
    attributes: Map<String, Any?>,
    throwable: Throwable?,
    stacktrace: String?,
  ) {
    errors += source to message
  }
}

/** A query whose responses are queued on the test transport, so nothing ever parses or caches it. */
private object TestQuery : Query<TestQuery.Data> {
  object Data : Query.Data

  override fun document(): String = "query Test { __typename }"

  override fun name(): String = "Test"

  override fun id(): String = "Test"

  override fun adapter(): Adapter<Data> = error("Queued responses are never parsed")

  override fun serializeVariables(
    writer: JsonWriter,
    customScalarAdapters: CustomScalarAdapters,
    withDefaultValues: Boolean,
  ) {}

  override fun rootField(): CompiledField = error("The client has no cache")
}
