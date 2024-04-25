package com.hedvig.android.feature.insurances.terminatedcontracts

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class TerminatedContractsPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `if there are no terminated insurances we get no terminated insurances state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val presenter = TerminatedContractsPresenter { getInsuranceContractsUseCase }
    presenter.test(TerminatedContractsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TerminatedContractsUiState.Loading)
      getInsuranceContractsUseCase.addOnlyActiveInsurancesToResponse()
      assertThat(awaitItem()).isEqualTo(
        TerminatedContractsUiState.NoTerminatedInsurances,
      )
    }
  }

  @Test
  fun `if there are terminated insurances they are all passed to success state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val presenter = TerminatedContractsPresenter { getInsuranceContractsUseCase }
    presenter.test(TerminatedContractsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TerminatedContractsUiState.Loading)
      getInsuranceContractsUseCase.addTerminatedInsurancesToResponse()
      assertThat(awaitItem()).isEqualTo(
        TerminatedContractsUiState.Success(
          insuranceContracts = getInsuranceContractsUseCase.getTerminatedInsurances(),
        ),
      )
    }
  }

  @Test
  fun `if there are terminated and active insurances success state has no active insurances`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val presenter = TerminatedContractsPresenter { getInsuranceContractsUseCase }
    presenter.test(TerminatedContractsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TerminatedContractsUiState.Loading)
      getInsuranceContractsUseCase.addTerminatedAndActiveInsurancesToResponse()
      assertThat(awaitItem()).isEqualTo(
        TerminatedContractsUiState.Success(
          insuranceContracts = getInsuranceContractsUseCase.getTerminatedInsurances(),
        ),
      )
    }
  }

  @Test
  fun `if receive error show error uiState`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val presenter = TerminatedContractsPresenter { getInsuranceContractsUseCase }
    presenter.test(TerminatedContractsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TerminatedContractsUiState.Loading)
      getInsuranceContractsUseCase.addErrorToResponse()
      assertThat(awaitItem()).isEqualTo(
        TerminatedContractsUiState.Error,
      )
    }
  }

  internal class FakeGetInsuranceContractsUseCase() : GetInsuranceContractsUseCase {
    private val responseTurbine = Turbine<Either<ErrorMessage, List<InsuranceContract>>>()

    override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, List<InsuranceContract>>> {
      return responseTurbine.asChannel().receiveAsFlow()
    }

    fun addTerminatedInsurancesToResponse() {
      responseTurbine.add(terminatedInsurances.right())
    }

    fun addTerminatedAndActiveInsurancesToResponse() {
      responseTurbine.add((activeInsurances + terminatedInsurances).right())
    }

    fun addOnlyActiveInsurancesToResponse() {
      responseTurbine.add(activeInsurances.right())
    }

    fun addErrorToResponse() {
      responseTurbine.add(ErrorMessage().left())
    }

    fun getTerminatedInsurances() = terminatedInsurances.toPersistentList()

    private val terminatedInsurances = listOf(
      InsuranceContract(
        "contractId1",
        "displayName#1",
        exposureDisplayName = "Test exposure",
        inceptionDate = LocalDate.fromEpochDays(200),
        terminationDate = LocalDate(2024, 5, 6),
        currentInsuranceAgreement = InsuranceAgreement(
          activeFrom = LocalDate.fromEpochDays(240),
          activeTo = LocalDate.fromEpochDays(340),
          displayItems = persistentListOf(),
          productVariant = ProductVariant(
            displayName = "Variant",
            contractGroup = ContractGroup.RENTAL,
            contractType = ContractType.SE_APARTMENT_RENT,
            partner = null,
            perils = persistentListOf(),
            insurableLimits = persistentListOf(),
            documents = persistentListOf(),
          ),
          certificateUrl = null,
          coInsured = persistentListOf(),
          creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        ),
        upcomingInsuranceAgreement = null,
        renewalDate = LocalDate.fromEpochDays(500),
        supportsAddressChange = false,
        supportsEditCoInsured = true,
        isTerminated = true,
        contractHolderSSN = "",
        contractHolderDisplayName = "",
      ),
      InsuranceContract(
        "contractId2",
        "displayName#2",
        exposureDisplayName = "Test exposure",
        inceptionDate = LocalDate.fromEpochDays(200),
        terminationDate = LocalDate(2024, 5, 6),
        currentInsuranceAgreement = InsuranceAgreement(
          activeFrom = LocalDate.fromEpochDays(240),
          activeTo = LocalDate.fromEpochDays(340),
          displayItems = persistentListOf(),
          productVariant = ProductVariant(
            displayName = "Variant",
            contractGroup = ContractGroup.RENTAL,
            contractType = ContractType.SE_APARTMENT_RENT,
            partner = null,
            perils = persistentListOf(),
            insurableLimits = persistentListOf(),
            documents = persistentListOf(),
          ),
          certificateUrl = null,
          coInsured = persistentListOf(),
          creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        ),
        upcomingInsuranceAgreement = null,
        renewalDate = LocalDate.fromEpochDays(500),
        supportsAddressChange = false,
        supportsEditCoInsured = true,
        isTerminated = true,
        contractHolderSSN = "",
        contractHolderDisplayName = "",
      ),
    )
    private val activeInsurances = listOf(
      InsuranceContract(
        "contractId4",
        "displayName#1",
        exposureDisplayName = "Test exposure",
        inceptionDate = LocalDate.fromEpochDays(200),
        terminationDate = LocalDate(2024, 5, 6),
        currentInsuranceAgreement = InsuranceAgreement(
          activeFrom = LocalDate.fromEpochDays(240),
          activeTo = LocalDate.fromEpochDays(340),
          displayItems = persistentListOf(),
          productVariant = ProductVariant(
            displayName = "Variant",
            contractGroup = ContractGroup.RENTAL,
            contractType = ContractType.SE_APARTMENT_RENT,
            partner = null,
            perils = persistentListOf(),
            insurableLimits = persistentListOf(),
            documents = persistentListOf(),
          ),
          certificateUrl = null,
          coInsured = persistentListOf(),
          creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        ),
        upcomingInsuranceAgreement = null,
        renewalDate = LocalDate.fromEpochDays(500),
        supportsAddressChange = false,
        supportsEditCoInsured = true,
        isTerminated = false,
        contractHolderSSN = "",
        contractHolderDisplayName = "",
      ),
      InsuranceContract(
        "contractId4",
        "displayName#2",
        exposureDisplayName = "Test exposure",
        inceptionDate = LocalDate.fromEpochDays(200),
        terminationDate = LocalDate(2024, 5, 6),
        currentInsuranceAgreement = InsuranceAgreement(
          activeFrom = LocalDate.fromEpochDays(240),
          activeTo = LocalDate.fromEpochDays(340),
          displayItems = persistentListOf(),
          productVariant = ProductVariant(
            displayName = "Variant",
            contractGroup = ContractGroup.RENTAL,
            contractType = ContractType.SE_APARTMENT_RENT,
            partner = null,
            perils = persistentListOf(),
            insurableLimits = persistentListOf(),
            documents = persistentListOf(),
          ),
          certificateUrl = null,
          coInsured = persistentListOf(),
          creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        ),
        upcomingInsuranceAgreement = null,
        renewalDate = LocalDate.fromEpochDays(500),
        supportsAddressChange = false,
        supportsEditCoInsured = true,
        isTerminated = false,
        contractHolderSSN = "",
        contractHolderDisplayName = "",
      ),
    )
  }
}
