package com.hedvig.android.apollo.test

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.MapTestNetworkTransport
import com.apollographql.apollo.testing.QueueTestNetworkTransport
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
}
