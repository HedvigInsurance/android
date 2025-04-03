package data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepositoryImpl
import com.hedvig.android.feature.addon.purchase.data.SubmitAddonPurchaseUseCaseImpl
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.UpsellTravelAddonActivateMutation
import octopus.type.buildUpsellTravelAddonActivationOutput
import octopus.type.buildUserError
import org.junit.Rule
import org.junit.Test

class SubmitAddonPurchaseUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  val testId = "jhjhjh"

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponseNullError: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = UpsellTravelAddonActivateMutation(testId, testId),
        data = UpsellTravelAddonActivateMutation.Data(OctopusFakeResolver) {
          upsellTravelAddonActivate = buildUpsellTravelAddonActivationOutput {
            userError = null
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithUserError: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = UpsellTravelAddonActivateMutation(testId, testId),
        data = UpsellTravelAddonActivateMutation.Data(OctopusFakeResolver) {
          upsellTravelAddonActivate = buildUpsellTravelAddonActivationOutput {
            userError = buildUserError {
              message = "Bad message"
            }
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithBadResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = UpsellTravelAddonActivateMutation(testId, testId),
        data = null,
        errors = listOf(Error.Builder(message = "Bad message").build()),
      )
    }

  @Test
  fun `if BE response is good return Unit and mark the flow as complete`() = runTest {
    val crossSellAfterFlowRepository = CrossSellAfterFlowRepositoryImpl()
    val sut = SubmitAddonPurchaseUseCaseImpl(apolloClientWithGoodResponseNullError, crossSellAfterFlowRepository)
    assertThat(crossSellAfterFlowRepository.shouldShowCrossSellSheet().first()).isFalse()
    val result = sut.invoke(testId, testId)
    assertThat(result).isRight().isEqualTo(Unit)
    assertThat(crossSellAfterFlowRepository.shouldShowCrossSellSheet().first()).isTrue()
  }

  @Test
  fun `if BE response is UserError return ErrorMessage with the msg from BE`() = runTest {
    val sut = SubmitAddonPurchaseUseCaseImpl(apolloClientWithUserError, CrossSellAfterFlowRepositoryImpl())
    val result = sut.invoke(testId, testId)
    assertThat(result)
      .isLeft().prop(ErrorMessage::message).isEqualTo("Bad message")
  }

  @Test
  fun `if BE response is error return ErrorMessage with null message`() = runTest {
    val sut = SubmitAddonPurchaseUseCaseImpl(apolloClientWithBadResponse, CrossSellAfterFlowRepositoryImpl())
    val result = sut.invoke(testId, testId)
    assertThat(result)
      .isLeft().prop(ErrorMessage::message).isEqualTo(null)
  }
}
