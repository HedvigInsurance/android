package data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.data.SessionAndIntent
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.ApartmentPriceIntentCreateMutation
import octopus.ApartmentShopSessionCreateMutation
import octopus.type.CountryCode
import octopus.type.buildPriceIntent
import octopus.type.buildShopSession
import org.junit.Rule
import org.junit.Test

class CreateSessionAndPriceIntentUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  @Test
  fun `successful session and price intent creation returns both ids`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ApartmentShopSessionCreateMutation(CountryCode.SE),
        data = ApartmentShopSessionCreateMutation.Data(OctopusFakeResolver) {
          shopSessionCreate = buildShopSession {
            id = "session-123"
          }
        },
      )
      registerTestResponse(
        operation = ApartmentPriceIntentCreateMutation(
          shopSessionId = "session-123",
          productName = "SE_APARTMENT_RENT",
        ),
        data = ApartmentPriceIntentCreateMutation.Data(OctopusFakeResolver) {
          priceIntentCreate = buildPriceIntent {
            id = "intent-456"
          }
        },
      )
    }

    val sut = CreateSessionAndPriceIntentUseCaseImpl(apolloClient)
    val result = sut.invoke("SE_APARTMENT_RENT")
    assertThat(result).isRight().isEqualTo(SessionAndIntent("session-123", "intent-456"))
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `network error on session creation returns ErrorMessage`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ApartmentShopSessionCreateMutation(CountryCode.SE),
        data = null,
        errors = listOf(Error.Builder(message = "Network error").build()),
      )
    }

    val sut = CreateSessionAndPriceIntentUseCaseImpl(apolloClient)
    val result = sut.invoke("SE_APARTMENT_RENT")
    assertThat(result).isLeft().prop(ErrorMessage::message).isNull()
  }
}
