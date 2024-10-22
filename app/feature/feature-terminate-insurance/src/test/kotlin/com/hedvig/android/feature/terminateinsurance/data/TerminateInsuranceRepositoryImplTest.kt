package com.hedvig.android.feature.terminateinsurance.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.logger.TestLogcatLoggingRule
import octopus.FlowTerminationStartMutation
import octopus.type.FlowTerminationStartInput
import org.junit.Rule

class TerminateInsuranceRepositoryImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  private val testId = "testId"

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithBadResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = FlowTerminationStartMutation(FlowTerminationStartInput(testId)),
        errors = listOf(com.apollographql.apollo.api.Error.Builder(message = "Bad message").build()),
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = FlowTerminationStartMutation(FlowTerminationStartInput(testId)),
        errors = listOf(com.apollographql.apollo.api.Error.Builder(message = "Bad message").build()),
      )
    }
}
