package com.hedvig.android.feature.insurances.terminatedcontracts

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
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
  fun `with an initial success state, if there is an error, we are able to retry and get back in success state again`() =
    runTest {
      val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
      val presenter = TerminatedContractsPresenter { getInsuranceContractsUseCase }
      presenter.test(TerminatedContractsUiState.Success(getInsuranceContractsUseCase.getTerminatedInsurances())) {
        assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Success>()
        getInsuranceContractsUseCase.addErrorToResponse()
        assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Error>()
        sendEvent(TerminatedContractsEvent.Retry)
        assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Loading>()
        getInsuranceContractsUseCase.addTerminatedInsurancesToResponse()
        assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Success>()
      }
    }

  @Test
  fun `with an initial error state, we are able to retry and if response is successful show success state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val presenter = TerminatedContractsPresenter { getInsuranceContractsUseCase }
    presenter.test(TerminatedContractsUiState.Error) {
      assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Error>()
      sendEvent(TerminatedContractsEvent.Retry)
      assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Loading>()
      getInsuranceContractsUseCase.addTerminatedInsurancesToResponse()
      assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Success>()
    }
  }

  @Test
  fun `with an initial error state, if there comes a successful response in flow, show success state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val presenter = TerminatedContractsPresenter { getInsuranceContractsUseCase }
    presenter.test(TerminatedContractsUiState.Error) {
      assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Error>()
      assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Loading>()
      getInsuranceContractsUseCase.addTerminatedInsurancesToResponse()
      assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Success>()
    }
  }

  @Test
  fun `with an initial success state, if there comes the same response, do not show loading`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val presenter = TerminatedContractsPresenter { getInsuranceContractsUseCase }
    val successState = TerminatedContractsUiState.Success(getInsuranceContractsUseCase.getTerminatedInsurances())
    presenter.test(successState) {
      assertThat(awaitItem()).isInstanceOf<TerminatedContractsUiState.Success>()
      sendEvent(TerminatedContractsEvent.Retry)
      getInsuranceContractsUseCase.addTerminatedInsurancesToResponse()
      awaitUnchanged()
    }
  }

  @Test
  fun `with an initial success state, if there comes a different successful response, do not show loading`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val presenter = TerminatedContractsPresenter { getInsuranceContractsUseCase }
    val successStateFirst = TerminatedContractsUiState.Success(getInsuranceContractsUseCase.getTerminatedInsurances())
    val successStateSecond = TerminatedContractsUiState.Success(
      getInsuranceContractsUseCase.getAnotherSetOfTerminatedInsurances(),
    )
    presenter.test(successStateFirst) {
      assertThat(awaitItem()).isEqualTo(successStateFirst)
      getInsuranceContractsUseCase.addAnotherSetOfTerminatedInsurances()
      assertThat(awaitItem()).isEqualTo(successStateSecond)
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

    fun getTerminatedInsurances() = terminatedInsurances

    fun addAnotherSetOfTerminatedInsurances() {
      responseTurbine.add((terminatedInsurances + listOf(extraTerminatedInsurance)).right())
    }

    fun getAnotherSetOfTerminatedInsurances() = (terminatedInsurances + listOf(extraTerminatedInsurance))

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
          displayItems = listOf(),
          productVariant = ProductVariant(
            displayName = "Variant",
            contractGroup = ContractGroup.RENTAL,
            contractType = ContractType.SE_APARTMENT_RENT,
            partner = null,
            perils = listOf(),
            insurableLimits = listOf(),
            documents = listOf(),
            displayTierName = "Standard",
            tierDescription = "Our standard coverage"
          ),
          certificateUrl = null,
          coInsured = listOf(),
          creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        ),
        upcomingInsuranceAgreement = null,
        renewalDate = LocalDate.fromEpochDays(500),
        supportsAddressChange = false,
        supportsEditCoInsured = true,
        isTerminated = true,
        contractHolderSSN = "",
        contractHolderDisplayName = "",
        supportsTierChange = true,
        tierName = "STANDARD"
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
          displayItems = listOf(),
          productVariant = ProductVariant(
            displayName = "Variant",
            contractGroup = ContractGroup.RENTAL,
            contractType = ContractType.SE_APARTMENT_RENT,
            partner = null,
            perils = listOf(),
            insurableLimits = listOf(),
            documents = listOf(),
            displayTierName = "Standard",
            tierDescription = "Our standard coverage"
          ),
          certificateUrl = null,
          coInsured = listOf(),
          creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        ),
        upcomingInsuranceAgreement = null,
        renewalDate = LocalDate.fromEpochDays(500),
        supportsAddressChange = false,
        supportsEditCoInsured = true,
        isTerminated = true,
        contractHolderSSN = "",
        contractHolderDisplayName = "",
        supportsTierChange = true,
        tierName = "STANDARD"
      ),
    )

    private val extraTerminatedInsurance = InsuranceContract(
      "contractId3",
      "displayName#3",
      exposureDisplayName = "Test exposure 3",
      inceptionDate = LocalDate.fromEpochDays(200),
      terminationDate = LocalDate(2024, 5, 6),
      currentInsuranceAgreement = InsuranceAgreement(
        activeFrom = LocalDate.fromEpochDays(240),
        activeTo = LocalDate.fromEpochDays(340),
        displayItems = listOf(),
        productVariant = ProductVariant(
          displayName = "Variant",
          contractGroup = ContractGroup.RENTAL,
          contractType = ContractType.SE_APARTMENT_RENT,
          partner = null,
          perils = listOf(),
          insurableLimits = listOf(),
          documents = listOf(),
          displayTierName = "Standard",
          tierDescription = "Our standard coverage"
        ),
        certificateUrl = null,
        coInsured = listOf(),
        creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
      ),
      upcomingInsuranceAgreement = null,
      renewalDate = LocalDate.fromEpochDays(500),
      supportsAddressChange = false,
      supportsEditCoInsured = true,
      isTerminated = true,
      contractHolderSSN = "",
      contractHolderDisplayName = "",
      supportsTierChange = true,
      tierName = "STANDARD"
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
          displayItems = listOf(),
          productVariant = ProductVariant(
            displayName = "Variant",
            contractGroup = ContractGroup.RENTAL,
            contractType = ContractType.SE_APARTMENT_RENT,
            partner = null,
            perils = listOf(),
            insurableLimits = listOf(),
            documents = listOf(),
            displayTierName = "Standard",
            tierDescription = "Our standard coverage"
          ),
          certificateUrl = null,
          coInsured = listOf(),
          creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        ),
        upcomingInsuranceAgreement = null,
        renewalDate = LocalDate.fromEpochDays(500),
        supportsAddressChange = false,
        supportsEditCoInsured = true,
        isTerminated = false,
        contractHolderSSN = "",
        contractHolderDisplayName = "",
        supportsTierChange = true,
        tierName = "STANDARD"
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
          displayItems = listOf(),
          productVariant = ProductVariant(
            displayName = "Variant",
            contractGroup = ContractGroup.RENTAL,
            contractType = ContractType.SE_APARTMENT_RENT,
            partner = null,
            perils = listOf(),
            insurableLimits = listOf(),
            documents = listOf(),
            displayTierName = "Standard",
            tierDescription = "Our standard coverage"
          ),
          certificateUrl = null,
          coInsured = listOf(),
          creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        ),
        upcomingInsuranceAgreement = null,
        renewalDate = LocalDate.fromEpochDays(500),
        supportsAddressChange = false,
        supportsEditCoInsured = true,
        isTerminated = false,
        contractHolderSSN = "",
        contractHolderDisplayName = "",
        supportsTierChange = true,
        tierName = "STANDARD"
      ),
    )
  }
}
