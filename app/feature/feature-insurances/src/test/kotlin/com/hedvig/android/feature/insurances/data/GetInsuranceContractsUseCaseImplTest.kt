package com.hedvig.android.feature.insurances.data

import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.InsuranceContractsQuery
import octopus.type.AgreementCreationCause.RENEWAL
import octopus.type.buildAgreement
import octopus.type.buildContract
import octopus.type.buildMember
import octopus.type.buildProductVariant
import org.junit.Rule
import org.junit.Test

class GetInsuranceContractsUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponseThatSupportsTier: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = InsuranceContractsQuery(false),
        data = InsuranceContractsQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            firstName = "test"
            lastName = "test"
            ssn = "test"
            terminatedContracts = listOf()
            activeContracts = buildList {
              add(
                buildContract {
                  masterInceptionDate = LocalDate(2021, 3, 9)
                  terminationDate = null
                  supportsMoving = true
                  supportsCoInsured = true
                  supportsChangeTier = true
                  coInsured = listOf()
                  upcomingChangedAgreement = null
                  id = "id"
                  exposureDisplayName = "displaySubtitle"
                  currentAgreement = buildAgreement {
                    activeFrom = LocalDate(2024, 3, 9)
                    activeTo = LocalDate(2025, 3, 9)
                    certificateUrl = null
                    creationCause = RENEWAL
                    displayItems = listOf()
                    productVariant = buildProductVariant {
                      displayName = "Variant"
                      displayNameTier = "Standard"
                      tierDescription = "kjkjkjkjhkj"
                      typeOfContract = "Swedish home"
                      partner = null
                      perils = listOf()
                      insurableLimits = listOf()
                      documents = listOf()
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
  private val apolloClientWithGoodResponseWithoutTier: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = InsuranceContractsQuery(false),
        data = InsuranceContractsQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            firstName = "test"
            lastName = "test"
            ssn = "test"
            terminatedContracts = listOf()
            activeContracts = buildList {
              add(
                buildContract {
                  masterInceptionDate = LocalDate(2021, 3, 9)
                  terminationDate = null
                  supportsMoving = true
                  supportsCoInsured = true
                  supportsChangeTier = false
                  coInsured = listOf()
                  upcomingChangedAgreement = null
                  id = "id"
                  exposureDisplayName = "displaySubtitle"
                  currentAgreement = buildAgreement {
                    activeFrom = LocalDate(2024, 3, 9)
                    activeTo = LocalDate(2025, 3, 9)
                    certificateUrl = null
                    creationCause = RENEWAL
                    displayItems = listOf()
                    productVariant = buildProductVariant {
                      displayName = "Variant"
                      displayNameTier = "Standard"
                      tierDescription = "kjkjkjkjhkj"
                      typeOfContract = "Swedish home"
                      partner = null
                      perils = listOf()
                      insurableLimits = listOf()
                      documents = listOf()
                    }
                  }
                },
              )
            }
          }
        },
      )
    }

  @Test
  fun `when the contract response has isChangeTierEnabled as true and FF is on InsuranceContract should have supportsTierChange as true`() =
    runTest {
      val featureManager = FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.TIER to true,
          Feature.MOVING_FLOW to true,
          Feature.EDIT_COINSURED to true,
          Feature.PAYMENT_SCREEN to true,
          Feature.TRAVEL_ADDON to false
        ),
      )
      val subjectUseCase = GetInsuranceContractsUseCaseImpl(
        apolloClient = apolloClientWithGoodResponseThatSupportsTier,
        featureManager = featureManager,
      )
      val result = subjectUseCase.invoke(true).first()
      assertk.assertThat(result).isRight().transform {
        it.first().supportsTierChange
      }.isTrue()
    }

  @Test
  fun `when the contract response has isChangeTierEnabled as true but FF is off InsuranceContract should have supportsTierChange as false`() =
    runTest {
      val featureManager = FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.TIER to false,
          Feature.MOVING_FLOW to true,
          Feature.EDIT_COINSURED to true,
          Feature.PAYMENT_SCREEN to true,
          Feature.TRAVEL_ADDON to false
        ),
      )
      val subjectUseCase = GetInsuranceContractsUseCaseImpl(
        apolloClient = apolloClientWithGoodResponseThatSupportsTier,
        featureManager = featureManager,
      )
      val result = subjectUseCase.invoke(true).first()
      assertk.assertThat(result).isRight().transform {
        it.first().supportsTierChange
      }.isFalse()
    }

  @Test
  fun `when FF is on  but the contract response has isChangeTierEnabled as false InsuranceContract should have supportsTierChange as false`() =
    runTest {
      val featureManager = FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.TIER to true,
          Feature.MOVING_FLOW to true,
          Feature.EDIT_COINSURED to true,
          Feature.PAYMENT_SCREEN to true,
          Feature.TRAVEL_ADDON to false
        ),
      )
      val subjectUseCase = GetInsuranceContractsUseCaseImpl(
        apolloClient = apolloClientWithGoodResponseWithoutTier,
        featureManager = featureManager,
      )
      val result = subjectUseCase.invoke(true).first()
      assertk.assertThat(result).isRight().transform {
        it.first().supportsTierChange
      }.isFalse()
    }

  @Test
  fun `when FF is off and the contract response has isChangeTierEnabled as false InsuranceContract should have supportsTierChange as false`() =
    runTest {
      val featureManager = FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.TIER to false,
          Feature.MOVING_FLOW to true,
          Feature.EDIT_COINSURED to true,
          Feature.PAYMENT_SCREEN to true,
          Feature.TRAVEL_ADDON to false
        ),
      )
      val subjectUseCase = GetInsuranceContractsUseCaseImpl(
        apolloClient = apolloClientWithGoodResponseWithoutTier,
        featureManager = featureManager,
      )
      val result = subjectUseCase.invoke(true).first()
      assertk.assertThat(result).isRight().transform {
        it.first().supportsTierChange
      }.isFalse()
    }
}
