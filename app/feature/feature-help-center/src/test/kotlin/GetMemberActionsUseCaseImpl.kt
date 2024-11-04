import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.feature.help.center.data.GetMemberActionsUseCaseImpl
import com.hedvig.android.feature.help.center.data.MemberAction
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import octopus.MemberActionsQuery
import octopus.type.buildMember
import octopus.type.buildMemberActions
import org.junit.Rule
import org.junit.Test

class GetMemberActionsUseCaseImplTest {
  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponseTierChangeTrue: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = MemberActionsQuery(),
        data = MemberActionsQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            memberActions = buildMemberActions {
              firstVetAction = null
              sickAbroadAction = null
              isCancelInsuranceEnabled = true
              isConnectPaymentEnabled = true
              isEditCoInsuredEnabled = true
              isMovingEnabled = true
              isTravelCertificateEnabled = true
              isChangeTierEnabled = true
              memberId = "memberid"
            }
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponseTierChangeFalse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = MemberActionsQuery(),
        data = MemberActionsQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            memberActions = buildMemberActions {
              firstVetAction = null
              sickAbroadAction = null
              isCancelInsuranceEnabled = true
              isConnectPaymentEnabled = true
              isEditCoInsuredEnabled = true
              isMovingEnabled = true
              isTravelCertificateEnabled = true
              isChangeTierEnabled = false
              memberId = "memberid"
            }
          }
        },
      )
    }

  @Test
  fun `when response has isChangeTierEnabled as true but FF is off MemberAction should have isTierChangeEnabled as false`() =
    runTest {
      val featureManager = FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.TIER to false,
          Feature.MOVING_FLOW to true,
          Feature.EDIT_COINSURED to true,
          Feature.PAYMENT_SCREEN to true,
        ),
      )
      val marketManager = FakeMarketManager()
      val subjectUseCase = GetMemberActionsUseCaseImpl(
        apolloClient = apolloClientWithGoodResponseTierChangeTrue,
        featureManager = featureManager,
        marketManager = marketManager,
      )
      val result = subjectUseCase.invoke()
      assertk.assertThat(result)
        .isRight()
        .prop(MemberAction::isTierChangeEnabled)
        .isFalse()
    }

  @Test
  fun `when response has isChangeTierEnabled as true and FF is on MemberAction should have isTierChangeEnabled as true`() =
    runTest {
      val featureManager = FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.TIER to true,
          Feature.MOVING_FLOW to true,
          Feature.EDIT_COINSURED to true,
          Feature.PAYMENT_SCREEN to true,
        ),
      )
      val marketManager = FakeMarketManager()
      val subjectUseCase = GetMemberActionsUseCaseImpl(
        apolloClient = apolloClientWithGoodResponseTierChangeTrue,
        featureManager = featureManager,
        marketManager = marketManager,
      )
      val result = subjectUseCase.invoke()
      assertk.assertThat(result)
        .isRight()
        .prop(MemberAction::isTierChangeEnabled)
        .isTrue()
    }

  @Test
  fun `when FF is on but response has isChangeTierEnabled as false MemberAction should have isTierChangeEnabled as false`() =
    runTest {
      val featureManager = FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.TIER to true,
          Feature.MOVING_FLOW to true,
          Feature.EDIT_COINSURED to true,
          Feature.PAYMENT_SCREEN to true,
        ),
      )
      val marketManager = FakeMarketManager()
      val subjectUseCase = GetMemberActionsUseCaseImpl(
        apolloClient = apolloClientWithGoodResponseTierChangeFalse,
        featureManager = featureManager,
        marketManager = marketManager,
      )
      val result = subjectUseCase.invoke()
      assertk.assertThat(result)
        .isRight()
        .prop(MemberAction::isTierChangeEnabled)
        .isFalse()
    }
}

private class FakeMarketManager : MarketManager {
  override val market: StateFlow<Market>
    get() = MutableStateFlow(Market.SE).asStateFlow()

  override fun selectedMarket(): Flow<Market?> {
    return flow { emit(Market.SE) }
  }
}
