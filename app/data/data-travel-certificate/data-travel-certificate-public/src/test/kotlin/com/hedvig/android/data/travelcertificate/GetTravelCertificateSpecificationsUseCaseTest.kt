package com.hedvig.android.data.travelcertificate

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.testing.enqueueTestNetworkError
import com.apollographql.apollo3.testing.enqueueTestResponse
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.TravelCertificateSpecificationsQuery
import octopus.type.buildContract
import octopus.type.buildMember
import octopus.type.buildTravelCertificateContractSpecification
import octopus.type.buildTravelCertificateSpecification
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ApolloExperimental::class)
@RunWith(TestParameterInjector::class)
internal class GetTravelCertificateSpecificationsUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `when the feature flag is on and the network request fails, we get not Error response`() = runTest {
    val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
      apolloClient,
    )

    apolloClient.enqueueTestNetworkError()
    val result = travelCertificateUseCase.invoke(null)

    assertThat(result).isLeft().isInstanceOf<TravelCertificateError.Error>()
  }

  @Test
  fun `when the feature flag is on and the network response contains no travel certificate, we get not eligible`() =
    runTest {
      val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
        apolloClient,
      )

      apolloClient.enqueueTestResponse(
        TravelCertificateSpecificationsQuery(),
        TravelCertificateSpecificationsQuery.Data(OctopusFakeResolver, {}),
      )
      val result = travelCertificateUseCase.invoke(null)

      assertThat(result).isLeft().isInstanceOf<TravelCertificateError.NotEligible>()
    }

  @Test
  fun `when the passed contractId is wrong, we get not eligible`() = runTest {
    val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
      apolloClient,
    )

    apolloClient.enqueueTestResponse(
      TravelCertificateSpecificationsQuery(),
      TravelCertificateSpecificationsQuery.Data(OctopusFakeResolver, {}),
    )
    val result = travelCertificateUseCase.invoke("wrong contractId")

    assertThat(result).isLeft().isInstanceOf<TravelCertificateError.NotEligible>()
  }

  @Test
  fun `when the passed contractId is right, we get TravelCertificateData`() = runTest {
    val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
      apolloClient,
    )

    apolloClient.enqueueTestResponse(
      TravelCertificateSpecificationsQuery(),
      TravelCertificateSpecificationsQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          travelCertificateSpecifications = buildTravelCertificateSpecification {
            contractSpecifications = listOf(
              buildTravelCertificateContractSpecification {
                contractId = "id"
                email = "email"
                minStartDate = LocalDate.parse("2023-02-02")
                maxStartDate = LocalDate.parse("2023-03-02")
                maxDurationDays = 1
                numberOfCoInsured = 2
              },
            )
          }
        }
      },
    )
    val result = travelCertificateUseCase.invoke("id")

    assertThat(result).isRight().isEqualTo(
      TravelCertificateData(
        TravelCertificateData.TravelCertificateSpecification(
          contractId = "id",
          email = "email",
          maxDurationDays = 1,
          dateRange = LocalDate.parse("2023-02-02")..LocalDate.parse("2023-03-02"),
          numberOfCoInsured = 2,
        ),
      ),
    )
  }

  @Test
  fun `when the passed contractId is correct, but the contract is not eligible, we get not eligible`() = runTest {
    val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
      apolloClient,
    )

    apolloClient.enqueueTestResponse(
      TravelCertificateSpecificationsQuery(),
      TravelCertificateSpecificationsQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          travelCertificateSpecifications = buildTravelCertificateSpecification {
            contractSpecifications = listOf(
              buildTravelCertificateContractSpecification {
                contractId = "id"
                email = "email"
                minStartDate = LocalDate.parse("2023-02-02")
                maxStartDate = LocalDate.parse("2023-03-02")
                maxDurationDays = 1
                numberOfCoInsured = 2
              },
            )
          }
        }
      },
    )
    val result = travelCertificateUseCase.invoke("id not eligible")

    assertThat(result).isLeft().isInstanceOf<TravelCertificateError.NotEligible>()
  }

  @Test
  fun `when the feature flag is on and the network request succeeds, the response depends on the active contract travel certificate eligibility`(
    @TestParameter contractSupportsTravelCertificate: Boolean,
  ) = runTest {
    val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
      apolloClient,
    )

    apolloClient.enqueueTestResponse(
      TravelCertificateSpecificationsQuery(),
      TravelCertificateSpecificationsQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          travelCertificateSpecifications = buildTravelCertificateSpecification {
            activeContracts = listOf(
              buildContract {
                id = "contractId"
                supportsTravelCertificate = contractSupportsTravelCertificate
              },
            )
            contractSpecifications = listOf(
              buildTravelCertificateContractSpecification {
                contractId = "contractId"
                email = "email"
                minStartDate = LocalDate.parse("2023-02-02")
                maxStartDate = LocalDate.parse("2023-03-02")
                maxDurationDays = 1
                numberOfCoInsured = 2
              },
            )
          }
        }
      },
    )
    val result = travelCertificateUseCase.invoke(null)

    if (contractSupportsTravelCertificate) {
      assertThat(result).isRight().isEqualTo(
        TravelCertificateData(
          TravelCertificateData.TravelCertificateSpecification(
            contractId = "contractId",
            email = "email",
            maxDurationDays = 1,
            dateRange = LocalDate.parse("2023-02-02")..LocalDate.parse("2023-03-02"),
            numberOfCoInsured = 2,
          ),
        ),
      )
    } else {
      assertThat(result).isLeft().isInstanceOf<TravelCertificateError.NotEligible>()
    }
  }
}
