package com.hedvig.android.network.clients.apollo

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.QueueTestNetworkTransport
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.benasher44.uuid.uuid4
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.network.clients.CapturedLog
import com.hedvig.android.network.clients.CapturedRumError
import com.hedvig.android.network.clients.CapturingLogcatLogger
import com.hedvig.android.network.clients.CapturingRumLogger
import com.hedvig.android.network.clients.FakeHedvigBuildConstants
import com.hedvig.android.network.clients.SentinelQuery
import com.hedvig.android.network.clients.di.ApolloClientMetroProviders
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

/**
 * Pins the exact lines [LoggingInterceptor] writes on Android and desktop, with the policy those platforms provide for
 * a non-production and a production build.
 */
@OptIn(ApolloExperimental::class)
class LoggingInterceptorOutputTest {
  @Test
  fun `a success response logs its data at DEBUG`() {
    val runs = runForEachProvidedPolicy { apolloClient ->
      apolloClient.enqueueTestResponse(SentinelQuery, SentinelQuery.Data("data-sentinel"))
      apolloClient.query(SentinelQuery).execute()
    }

    for (run in runs) {
      assertThat(run.logs).containsExactly(
        CapturedLog(LogPriority.DEBUG, null, "GraphQL request for Sentinel START."),
        CapturedLog(
          LogPriority.DEBUG,
          null,
          "GraphQL request for Sentinel EMISSION. Response data: Data(value=data-sentinel)",
        ),
        CapturedLog(LogPriority.DEBUG, null, "GraphQL request for Sentinel END."),
      )
      assertThat(run.rumErrors).isEmpty()
    }
  }

  @Test
  fun `a GraphQL error logs data, errors and extensions at ERROR and to RUM`() {
    val runs = runForEachProvidedPolicy { apolloClient ->
      apolloClient.enqueueTestResponse(
        ApolloResponse.Builder(SentinelQuery, uuid4())
          .data(SentinelQuery.Data("data-sentinel"))
          .errors(listOf(Error.Builder("Something failed").putExtension("detail", "error-extension-sentinel").build()))
          .extensions(mapOf("trace" to "response-extension-sentinel"))
          .build(),
      )
      apolloClient.query(SentinelQuery).execute()
    }

    val errors = "[LoggableGraphqlError(message=Something failed, locations=null, paths=null, " +
      "extensions=Extensions(errorType=null, uncategorizedExtensions={detail=error-extension-sentinel}))]"
    val attributes =
      "{data=Data(value=data-sentinel), errors=$errors, extensions=[(trace, response-extension-sentinel)]}"
    for (run in runs) {
      assertThat(run.logs).containsExactly(
        CapturedLog(LogPriority.DEBUG, null, "GraphQL request for Sentinel START."),
        CapturedLog(
          LogPriority.DEBUG,
          null,
          "GraphQL request for Sentinel EMISSION. Response data: Data(value=data-sentinel)",
        ),
        CapturedLog(
          LogPriority.ERROR,
          null,
          "GraphQL errors for Sentinel: [Something failed] attributes: $attributes" +
            " There was data and errors at the same time! Data:Data(value=data-sentinel)",
        ),
        CapturedLog(LogPriority.DEBUG, null, "GraphQL request for Sentinel END."),
      )
      assertThat(run.rumErrors.map { it.copy(attributes = mapOf("rendered" to it.attributes.toString())) })
        .containsExactly(
          CapturedRumError(
            "GraphQL errors for Sentinel: [Something failed]",
            ErrorSource.NETWORK,
            mapOf("rendered" to attributes),
          ),
        )
    }
  }

  @Test
  fun `an exception logs at WARN`() {
    val runs = runForEachProvidedPolicy { apolloClient ->
      apolloClient.enqueueTestNetworkError()
      apolloClient.query(SentinelQuery).execute()
    }

    for (run in runs) {
      assertThat(run.logs).containsExactly(
        CapturedLog(LogPriority.DEBUG, null, "GraphQL request for Sentinel START."),
        CapturedLog(LogPriority.DEBUG, null, "GraphQL request for Sentinel EMISSION. Response data: null"),
        CapturedLog(
          LogPriority.WARN,
          null,
          "GraphQL exception for Sentinel: " +
            "com.apollographql.apollo.exception.ApolloNetworkException: Network error queued in QueueTestNetworkTransport",
        ),
        CapturedLog(LogPriority.DEBUG, null, "GraphQL request for Sentinel END."),
      )
      assertThat(run.rumErrors).isEmpty()
    }
  }
}

private class Run(val logs: List<CapturedLog>, val rumErrors: List<CapturedRumError>)

/** Runs [block] against a fresh client once per provided policy, for a non-production and a production build. */
private fun runForEachProvidedPolicy(block: suspend (ApolloClient) -> Unit): List<Run> {
  return listOf(false, true).map { isProduction ->
    val logs = CapturingLogcatLogger()
    val rum = CapturingRumLogger()
    LogcatLogger.install(logs)
    RumLogger.install(rum)
    try {
      val networkLogPolicy = object : ApolloClientMetroProviders {}
        .provideNetworkLogPolicy(FakeHedvigBuildConstants(isProduction))
      val apolloClient = ApolloClient.Builder()
        .networkTransport(QueueTestNetworkTransport())
        .addHedvigInterceptors(emptyList(), networkLogPolicy)
        .build()
      runTest { block(apolloClient) }
      Run(logs.entries(), rum.errors())
    } finally {
      LogcatLogger.uninstall()
      RumLogger.uninstall()
    }
  }
}
