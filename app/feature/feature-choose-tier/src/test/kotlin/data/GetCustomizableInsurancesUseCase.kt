package data

import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.feature.change.tier.data.GetCustomizableInsurancesUseCaseImpl
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.ContractsEligibleForTierChangeQuery
import octopus.type.buildAgreement
import octopus.type.buildContract
import octopus.type.buildMember
import octopus.type.buildProductVariant
import org.junit.Rule
import org.junit.Test

class GetCustomizableInsurancesUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithBadResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ContractsEligibleForTierChangeQuery(),
        errors = listOf(com.apollographql.apollo.api.Error.Builder(message = "Bad message").build()),
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponseButNotEligible: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ContractsEligibleForTierChangeQuery(),
        data = ContractsEligibleForTierChangeQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            activeContracts = listOf(
              buildContract {
                id = "000"
                supportsChangeTier = false
                exposureDisplayName = "Rurururugatan 134"
                currentAgreement = buildAgreement {
                  productVariant = buildProductVariant {
                    displayName = "Variant"
                    typeOfContract = "SE_APARTMENT_RENT"
                    partner = null
                    perils = listOf()
                    insurableLimits = listOf()
                    documents = listOf()
                    displayNameTier = "Standard"
                    tierDescription = "Our standard coverage"
                  }
                }
              },
            )
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ContractsEligibleForTierChangeQuery(),
        data = ContractsEligibleForTierChangeQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            activeContracts = listOf(
              buildContract {
                id = "000"
                supportsChangeTier = true
                exposureDisplayName = "Rurururugatan 134"
                currentAgreement = buildAgreement {
                  productVariant = buildProductVariant {
                    displayName = "Variant"
                    typeOfContract = "SE_APARTMENT_RENT"
                    partner = null
                    perils = listOf()
                    insurableLimits = listOf()
                    documents = listOf()
                    displayNameTier = "Standard"
                    tierDescription = "Our standard coverage"
                  }
                }
              },
            )
          }
        },
      )
    }

  @Test
  fun `when response is fine and tier feature flag is on the result is good`() = runTest {
    val useCase = GetCustomizableInsurancesUseCaseImpl(apolloClient = apolloClientWithGoodResponse)
    useCase.invoke().collectLatest { result ->
      assertk.assertThat(result.getOrNull()?.first()?.displayName)
        .isNotNull().isEqualTo("Variant")
    }
  }

  @Test
  fun `when response is bad the result is ErrorMessage`() = runTest {
    val useCase = GetCustomizableInsurancesUseCaseImpl(apolloClient = apolloClientWithBadResponse)
    useCase.invoke().collectLatest { result ->
      assertk.assertThat(result).isLeft()
    }
  }

  @Test
  fun `when response is otherwise good but there is no customizable insurance in it return null`() = runTest {
    val useCase = GetCustomizableInsurancesUseCaseImpl(apolloClient = apolloClientWithGoodResponseButNotEligible)
    val result = useCase.invoke().first()
    assertk.assertThat(result).isRight().isNull()
  }
}
