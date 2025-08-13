package data

import assertk.assertThat
import assertk.assertions.isEqualTo
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
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCaseImpl
import com.hedvig.android.feature.addon.purchase.data.InsuranceForAddon
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.InsurancesForTravelAddonQuery
import octopus.type.buildAgreement
import octopus.type.buildContract
import octopus.type.buildMember
import octopus.type.buildProductVariant
import org.junit.Rule
import org.junit.Test

class GetInsuranceForTravelAddonUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  val testIds = listOf("testId1")

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = InsurancesForTravelAddonQuery(),
        data = InsurancesForTravelAddonQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            activeContracts = buildList {
              add(
                buildContract {
                  id = testIds[0]
                  exposureDisplayName = "exposureDisplayName"
                  currentAgreement = buildAgreement {
                    productVariant = buildProductVariant {
                      displayName = "displayName"
                      typeOfContract = "SE_HOUSE"
                    }
                  }
                },
              )
              add(
                buildContract {
                  id = "anotherId"
                  exposureDisplayName = "exposureDisplayName"
                  currentAgreement = buildAgreement {
                    productVariant = buildProductVariant {
                      displayName = "displayName"
                      typeOfContract = "SE_HOUSE"
                    }
                  }
                },
              )
            }
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodButEmptyResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = InsurancesForTravelAddonQuery(),
        data = InsurancesForTravelAddonQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            activeContracts = listOf()
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithError: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = InsurancesForTravelAddonQuery(),
        errors = listOf(Error.Builder(message = "Bad message").build()),
      )
    }

  @Test
  fun `if FF for addons is off return ErrorMessage`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to false))
    val sut = GetInsuranceForTravelAddonUseCaseImpl(apolloClientWithGoodResponse, featureManager)
    val result = sut.invoke(testIds).first()
    assertThat(result)
      .isLeft()
  }

  @Test
  fun `if quotes list is empty return ErrorMessage with null message`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetInsuranceForTravelAddonUseCaseImpl(apolloClientWithGoodButEmptyResponse, featureManager)
    val result = sut.invoke(testIds).first()
    assertThat(result)
      .isLeft()
      .prop(ErrorMessage::message)
  }

  @Test
  fun `if BE gives error return ErrorMessage`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetInsuranceForTravelAddonUseCaseImpl(apolloClientWithError, featureManager)
    val result = sut.invoke(testIds).first()
    assertThat(result)
      .isLeft()
  }

  @Test
  fun `if BE gives correct response but the required ids are not there return ErrorMessage`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetInsuranceForTravelAddonUseCaseImpl(apolloClientWithGoodResponse, featureManager)
    val result = sut.invoke(listOf("someotherid")).first()
    assertThat(result)
      .isLeft()
  }

  @Test
  fun `if BE gives correct response and required ids are not there return correctly mapped list`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetInsuranceForTravelAddonUseCaseImpl(apolloClientWithGoodResponse, featureManager)
    val result = sut.invoke(testIds).first()
    assertThat(result)
      .isRight().isEqualTo(
        listOf(
          InsuranceForAddon(
            id = testIds[0],
            displayName = "displayName",
            contractExposure = "exposureDisplayName",
            contractGroup = ContractGroup.HOUSE,
          ),
        ),
      )
  }
}
