package com.hedvig.android.feature.insurances.insurancedetail

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.testing.enqueueTestResponse
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameter.TestParameterValuesProvider
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.giraffe.test.GiraffeFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.feature.insurances.insurancedetail.coverage.ContractCoverage
import com.hedvig.android.feature.insurances.insurancedetail.coverage.GetContractCoverageUseCase
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.android.language.test.FakeLanguageService
import giraffe.InsuranceQuery
import giraffe.fragment.ContractStatusFragment
import giraffe.type.Locale
import giraffe.type.buildActiveInFutureAndTerminatedInFutureStatus
import giraffe.type.buildActiveInFutureStatus
import giraffe.type.buildActiveStatus
import giraffe.type.buildContract
import giraffe.type.buildDeletedStatus
import giraffe.type.buildPendingStatus
import giraffe.type.buildTerminatedInFutureStatus
import giraffe.type.buildTerminatedStatus
import giraffe.type.buildTerminatedTodayStatus
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ApolloExperimental::class)
@RunWith(TestParameterInjector::class)
class GetContractDetailsUseCaseTest {

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `the contract status should determine if terminating the contract is allowed or not`(
    @TestParameter(valuesProvider = ContractStatusToTerminationAvailabilityProvider::class)
    contractStatusToTerminationAvailability: Pair<ContractStatusFragment, IsAllowedToTerminateContract>,
  ) = runTest {
    val (contractStatusFragment, isTerminationAllowed) = contractStatusToTerminationAvailability
    val getContractCoverageUseCase = object : GetContractCoverageUseCase {
      override suspend fun invoke(contractId: String): Either<ErrorMessage, ContractCoverage> {
        return ContractCoverage(persistentListOf(), persistentListOf()).right()
      }
    }
    val getContractDetailsUseCase = GetContractDetailsUseCase(
      apolloClient,
      getContractCoverageUseCase,
      FakeLanguageService(),
      FakeFeatureManager({ mapOf(Feature.TERMINATION_FLOW to true) }),
    )
    apolloClient.enqueueTestResponse(
      InsuranceQuery(Locale.UNKNOWN__),
      InsuranceQuery.Data(GiraffeFakeResolver) {
        contracts = listOf(
          buildContract {
            id = "contractId"
            status = when {
              contractStatusFragment.asPendingStatus != null -> buildPendingStatus {}
              contractStatusFragment.asActiveInFutureStatus != null -> buildActiveInFutureStatus {}
              contractStatusFragment.asActiveStatus != null -> buildActiveStatus {}
              contractStatusFragment.asActiveInFutureAndTerminatedInFutureStatus != null -> {
                buildActiveInFutureAndTerminatedInFutureStatus {}
              }
              contractStatusFragment.asTerminatedInFutureStatus != null -> buildTerminatedInFutureStatus {}
              contractStatusFragment.asTerminatedTodayStatus != null -> buildTerminatedTodayStatus {}
              contractStatusFragment.asTerminatedStatus != null -> buildTerminatedStatus {}
              contractStatusFragment.asDeletedStatus != null -> buildDeletedStatus {}
              else -> error("test error")
            }
          },
        )
      },
    )

    val result: Either<ContractDetailError, ContractDetails> = getContractDetailsUseCase.invoke("contractId")

    assertThat(result)
      .isRight()
      .prop(ContractDetails::cancelInsuranceData).apply {
        when (isTerminationAllowed) {
          IsAllowedToTerminateContract.ALLOWED -> isNotNull()
          IsAllowedToTerminateContract.DISALLOWED -> isNull()
        }
      }
  }

  private object ContractStatusToTerminationAvailabilityProvider : TestParameterValuesProvider {
    private val nullContractStatusFragment =
      ContractStatusFragment("", null, null, null, null, null, null, null, null)

    override fun provideValues(): List<Pair<ContractStatusFragment, IsAllowedToTerminateContract>> {
      return listOf(
        nullContractStatusFragment.copy(
          asPendingStatus = ContractStatusFragment.AsPendingStatus("", null),
        ) to IsAllowedToTerminateContract.ALLOWED,
        nullContractStatusFragment.copy(
          asActiveInFutureStatus = ContractStatusFragment.AsActiveInFutureStatus(
            "",
            null,
          ),
        ) to IsAllowedToTerminateContract.ALLOWED,
        nullContractStatusFragment.copy(
          asActiveStatus = ContractStatusFragment.AsActiveStatus("", null, null),
        ) to IsAllowedToTerminateContract.ALLOWED,
        nullContractStatusFragment.copy(
          asActiveInFutureAndTerminatedInFutureStatus = ContractStatusFragment.AsActiveInFutureAndTerminatedInFutureStatus(
            "",
            null,
            null,
          ),
        ) to IsAllowedToTerminateContract.DISALLOWED,
        nullContractStatusFragment.copy(
          asTerminatedInFutureStatus = ContractStatusFragment.AsTerminatedInFutureStatus(
            "",
            null,
          ),
        ) to IsAllowedToTerminateContract.DISALLOWED,
        nullContractStatusFragment.copy(
          asTerminatedTodayStatus = ContractStatusFragment.AsTerminatedTodayStatus(
            "",
            null,
          ),
        ) to IsAllowedToTerminateContract.DISALLOWED,
        nullContractStatusFragment.copy(
          asTerminatedStatus = ContractStatusFragment.AsTerminatedStatus(
            "",
            null,
          ),
        ) to IsAllowedToTerminateContract.DISALLOWED,
        nullContractStatusFragment.copy(
          asDeletedStatus = ContractStatusFragment.AsDeletedStatus("", null),
        ) to IsAllowedToTerminateContract.DISALLOWED,
      )
    }
  }
}

enum class IsAllowedToTerminateContract {
  ALLOWED,
  DISALLOWED,
}
