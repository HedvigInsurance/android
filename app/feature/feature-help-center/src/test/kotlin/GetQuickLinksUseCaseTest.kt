import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.right
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.feature.help.center.data.GetMemberActionsUseCase
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.feature.help.center.data.MemberAction
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.model.QuickAction.MultiSelectExpandedLink
import com.hedvig.android.featureflags.test.FakeFeatureManager
import com.hedvig.android.logger.TestLogcatLoggingRule
import hedvig.resources.R
import kotlinx.coroutines.test.runTest
import octopus.AvailableSelfServiceOnContractsQuery
import octopus.type.buildAgreement
import octopus.type.buildContract
import octopus.type.buildMember
import octopus.type.buildProductVariant
import org.junit.Rule
import org.junit.Test

class GetQuickLinksUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = AvailableSelfServiceOnContractsQuery(),
        data = AvailableSelfServiceOnContractsQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            activeContracts = listOf(
              buildContract {
                id = "id"
                exposureDisplayName = "displaySubtitle"
                currentAgreement = buildAgreement {
                  productVariant = buildProductVariant {
                    displayName = "Variant"
                  }
                }
                supportsCoInsured = true
                coInsured = listOf()
              },
            )
          }
        },
      )
    }

  @Test
  fun `when response is fine return ChangeTier quickAction`() = runTest {
    val featureManager = FakeFeatureManager(fixedReturnForAll = true)
    val getMemberActionsUseCase = FakeGetMemberActionsUseCase()
    getMemberActionsUseCase.turbine.add(fakeMemberActionWithTier.right())
    val useCase = GetQuickLinksUseCase(
      apolloClient = apolloClientWithGoodResponse,
      featureManager = featureManager,
      getMemberActionsUseCase = getMemberActionsUseCase,
    )
    val result = useCase.invoke()
    val listNotEmpty = result.getOrNull()?.isNotEmpty() ?: false
    assertk.assertThat(listNotEmpty).isTrue()
    assertk
      .assertThat(result)
      .isRight()
      .transform { list ->
        list.filterIsInstance<MultiSelectExpandedLink>()
          .any {
            it.links.contains(
              QuickAction.StandaloneQuickLink(
                quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkChangeTier,
                titleRes = R.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_TITLE,
                hintTextRes = R.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE,
              ),
            )
          }
      }
      .isTrue()
  }

  @Test
  fun `when response says cannot change tier should not contain ChangeTier quickAction`() = runTest {
    val featureManager = FakeFeatureManager(fixedReturnForAll = true)
    val getMemberActionsUseCase = FakeGetMemberActionsUseCase()
    getMemberActionsUseCase.turbine.add(fakeMemberActionWithoutTier.right())
    val useCase = GetQuickLinksUseCase(
      apolloClient = apolloClientWithGoodResponse,
      featureManager = featureManager,
      getMemberActionsUseCase = getMemberActionsUseCase,
    )
    val result = useCase.invoke()
    val listNotEmpty = result.getOrNull()?.isNotEmpty() ?: false
    assertk.assertThat(listNotEmpty).isTrue()
    assertk
      .assertThat(result)
      .isRight()
      .transform { list ->
        list.filterIsInstance<MultiSelectExpandedLink>()
          .none {
            it.links.contains(
              QuickAction.StandaloneQuickLink(
                quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkChangeTier,
                titleRes = R.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_TITLE,
                hintTextRes = R.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE,
              ),
            )
          }
      }
      .isTrue()
  }
}

private class FakeGetMemberActionsUseCase : GetMemberActionsUseCase {
  val turbine = Turbine<Either<ErrorMessage, MemberAction>>()

  override suspend fun invoke(): Either<ErrorMessage, MemberAction> {
    return turbine.awaitItem()
  }
}

private val fakeMemberActionWithTier = MemberAction(
  isTierChangeEnabled = true,
  firstVetAction = null,
  isCancelInsuranceEnabled = true,
  isMovingEnabled = true,
  isEditCoInsuredEnabled = true,
  isConnectPaymentEnabled = true,
  isTravelCertificateEnabled = true,
  sickAbroadAction = null,
)
private val fakeMemberActionWithoutTier = MemberAction(
  isTierChangeEnabled = false,
  firstVetAction = null,
  isCancelInsuranceEnabled = true,
  isMovingEnabled = true,
  isEditCoInsuredEnabled = true,
  isConnectPaymentEnabled = true,
  isTravelCertificateEnabled = true,
  sickAbroadAction = null,
)
