package com.hedvig.android.apollo.test

import app.cash.turbine.Turbine
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.network.NetworkTransport
import com.apollographql.apollo.testing.MapTestNetworkTransport
import com.apollographql.apollo.testing.QueueTestNetworkTransport
import com.benasher44.uuid.uuid4
import kotlin.concurrent.withLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import org.junit.rules.ExternalResource

/**
 * A test rule which sets up the [ApolloClient] ready to test by using the [QueueTestNetworkTransport].
 * Use by calling [ApolloClient.enqueueTestResponse] and [ApolloClient.enqueueTestNetworkError] on the [apolloClient]
 * exposed from this rule, and by passing it to the classes than need an [ApolloClient].
 *
 * Example usage:
 * ```
 * @get:Rule
 * val testApolloClientRule = TestApolloClientRule()
 * val apolloClient: ApolloClient
 *   get() = testApolloClientRule.apolloClient
 *
 * @Test fun test() = runTest {
 *   apolloClient.enqueueTestResponse()
 * }
 * ```
 */
class TestApolloClientRule(
  private val testNetworkTransportType: TestNetworkTransportType = TestNetworkTransportType.QUEUE,
) : ExternalResource() {
  lateinit var apolloClient: ApolloClient
    private set

  @OptIn(ApolloExperimental::class)
  override fun before() {
    apolloClient = ApolloClient.Builder()
      .networkTransport(
        when (testNetworkTransportType) {
          TestNetworkTransportType.QUEUE -> QueueTestNetworkTransport()
          TestNetworkTransportType.MAP -> MapTestNetworkTransport()
          TestNetworkTransportType.TURBINE_MAP -> TurbineMapTestNetworkTransport()
        },
      )
      .build()
  }

  override fun after() {
    apolloClient.close()
  }
}

enum class TestNetworkTransportType {
  QUEUE,
  MAP,
  TURBINE_MAP,
}

private sealed interface TestResponse {
  object NetworkError : TestResponse

  class Response(val response: ApolloResponse<out Operation.Data>) : TestResponse
}

@ApolloExperimental
private class TurbineMapTestNetworkTransport : NetworkTransport {
  private val lock = reentrantLock()
  private val operationIdsToTurbineResponses =
    mutableMapOf<Operation<out Operation.Data>, Turbine<TestResponse>>()

  fun <D : Operation.Data> MutableMap<Operation<out Operation.Data>, Turbine<TestResponse>>.getOrPutTurbineForOperation(
    operation: Operation<D>,
  ): Turbine<TestResponse> {
    return lock.withLock {
      getOrPut(operation) {
        Turbine<TestResponse>(name = "Turbine for operation ${operation.name()}")
      }
    }
  }

  override fun <D : Operation.Data> execute(request: ApolloRequest<D>): Flow<ApolloResponse<D>> {
    return flow {
      // "Emulate" a network call
      yield()

      val response = operationIdsToTurbineResponses.getOrPutTurbineForOperation(request.operation).awaitItem()

      val apolloResponse = when (response) {
        is TestResponse.NetworkError -> {
          ApolloResponse.Builder(operation = request.operation, requestUuid = request.requestUuid)
            .exception(exception = ApolloNetworkException("Network error registered in MapTestNetworkTransport"))
            .build()
        }

        is TestResponse.Response -> {
          @Suppress("UNCHECKED_CAST")
          response.response as ApolloResponse<D>
        }
      }

      emit(apolloResponse.newBuilder().isLast(true).build())
    }
  }

  fun <D : Operation.Data> register(operation: Operation<D>, response: ApolloResponse<D>) {
    operationIdsToTurbineResponses.getOrPutTurbineForOperation(operation).add(TestResponse.Response(response))
  }

  fun <D : Operation.Data> registerNetworkError(operation: Operation<D>) {
    operationIdsToTurbineResponses.getOrPutTurbineForOperation(operation).add(TestResponse.NetworkError)
  }

  override fun dispose() {}
}

@ApolloExperimental
fun <D : Operation.Data> ApolloClient.registerSuspendingTestResponse(
  operation: Operation<D>,
  response: ApolloResponse<D>,
): Unit = (networkTransport as? TurbineMapTestNetworkTransport)?.register(operation, response)
  ?: error("Apollo: ApolloClient.registerSuspendingTestResponse() can be used only with TurbineMapTestNetworkTransport")

@ApolloExperimental
fun <D : Operation.Data> ApolloClient.registerSuspendingTestResponse(
  operation: Operation<D>,
  data: D? = null,
  errors: List<Error>? = null,
) = registerSuspendingTestResponse(
  operation,
  ApolloResponse.Builder(
    operation = operation,
    requestUuid = uuid4(),
  )
    .data(data)
    .errors(errors)
    .build(),
)

@ApolloExperimental
fun <D : Operation.Data> ApolloClient.registerSuspendingTestNetworkError(operation: Operation<D>): Unit =
  (networkTransport as? TurbineMapTestNetworkTransport)?.registerNetworkError(operation)
    ?: error(
      "Apollo: ApolloClient.registerSuspendingTestNetworkError() can be used only with TurbineMapTestNetworkTransport",
    )
