package com.hedvig.android.app.apollo

import assertk.assertThat
import assertk.assertions.any
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isNotEmpty
import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.json.JsonWriter
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class LoggingInterceptorTest {
  private val logLines = mutableListOf<String>()
  private val rumErrors = mutableListOf<String>()

  @Before
  fun installLoggers() {
    LogcatLogger.install(
      object : LogcatLogger {
        override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
          logLines.add(message())
        }
      },
    )
    RumLogger.install(
      object : RumLogger {
        override fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {}

        override fun logError(
          message: String,
          source: ErrorSource,
          attributes: Map<String, Any?>,
          throwable: Throwable?,
          stacktrace: String?,
        ) {
          rumErrors.add("$message $attributes")
        }
      },
    )
  }

  @After
  fun uninstallLoggers() {
    LogcatLogger.uninstall()
    RumLogger.uninstall()
  }

  @Test
  fun `outside debug builds, response data is never logged`() = runTest {
    intercept(logResponseData = false, withErrors = false)
    intercept(logResponseData = false, withErrors = true)

    assertThat(logLines).any { it.contains(OPERATION_NAME) }
    assertThat(logLines).any { it.contains(ERROR_MESSAGE) }
    assertThat(rumErrors).isNotEmpty()
    assertThat(rumErrors).any { it.contains(ERROR_MESSAGE) }
    for (line in logLines + rumErrors) {
      assertThat(line).doesNotContain(DATA_MARKER)
    }
  }

  @Test
  fun `in debug builds, response data is logged`() = runTest {
    intercept(logResponseData = true, withErrors = false)

    assertThat(logLines).any { it.contains(DATA_MARKER) }
  }

  private suspend fun intercept(logResponseData: Boolean, withErrors: Boolean) {
    val request = ApolloRequest.Builder(FakeQuery).build()
    val response = ApolloResponse.Builder(FakeQuery, request.requestUuid)
      .data(FakeQuery.FakeData(DATA_MARKER))
      .errors(if (withErrors) listOf(Error.Builder(ERROR_MESSAGE).build()) else null)
      .build()
    val chain = object : ApolloInterceptorChain {
      @Suppress("UNCHECKED_CAST")
      override fun <D : Operation.Data> proceed(request: ApolloRequest<D>): Flow<ApolloResponse<D>> {
        return flowOf(response as ApolloResponse<D>)
      }
    }
    LoggingInterceptor(logResponseData).intercept(request, chain).toList()
  }

  private object FakeQuery : Operation<FakeQuery.FakeData> {
    data class FakeData(val memberName: String) : Operation.Data

    override fun document(): String = "query $OPERATION_NAME { memberName }"

    override fun name(): String = OPERATION_NAME

    override fun id(): String = OPERATION_NAME

    override fun adapter(): Adapter<FakeData> = error("Not needed")

    override fun serializeVariables(
      writer: JsonWriter,
      customScalarAdapters: CustomScalarAdapters,
      withDefaultValues: Boolean,
    ) {}

    override fun rootField(): CompiledField = error("Not needed")
  }

  private companion object {
    const val OPERATION_NAME = "FakeMemberQuery"
    const val ERROR_MESSAGE = "Something went wrong"
    const val DATA_MARKER = "SensitiveMemberName"
  }
}
