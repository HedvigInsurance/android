package com.hedvig.android.feature.insurances.insurancedetail

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
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class ContractDetailPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `if termination flow enabled and no termination date show cancel insurance button`() = runTest {
    val getContractForContractIdUseCase = FakeGetContractForContractIdUseCase()
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TERMINATION_FLOW to true))
    val presenter = ContractDetailPresenter(
      contractId = getContractForContractIdUseCase.getValIdWithoutTerminationDate(),
      featureManager = featureManager,
      getContractForContractIdUseCase = getContractForContractIdUseCase,
    )
    presenter.test(ContractDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContractDetailsUiState.Loading)
      getContractForContractIdUseCase.addInsuranceWithNoTerminationDateToResponseTurbine()
      assertThat(awaitItem()).isEqualTo(
        ContractDetailsUiState.Success(
          allowTerminatingInsurance = true,
          insuranceContract = getContractForContractIdUseCase.getInsuranceWithOutTerminationDate(),
        ),
      )
    }
  }

  @Test
  fun `with an initial success, if there is an error, can retry and get back in success state again through loading`() =
    runTest {
      val getContractForContractIdUseCase = FakeGetContractForContractIdUseCase()
      val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TERMINATION_FLOW to true))
      val presenter = ContractDetailPresenter(
        contractId = getContractForContractIdUseCase.getValIdWithoutTerminationDate(),
        featureManager = featureManager,
        getContractForContractIdUseCase = getContractForContractIdUseCase,
      )
      presenter.test(
        ContractDetailsUiState.Success(
          getContractForContractIdUseCase.getInsuranceWithOutTerminationDate(),
          true,
        ),
      ) {
        assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Success>()
        getContractForContractIdUseCase.addErrorToResponse()
        assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Error>()
        sendEvent(ContractDetailsEvent.RetryLoadingContract)
        assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Loading>()
        getContractForContractIdUseCase.addInsuranceWithNoTerminationDateToResponseTurbine()
        assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Success>()
      }
    }

  @Test
  fun `with an initial error state, can retry and with a good response get success state through loading`() = runTest {
    val getContractForContractIdUseCase = FakeGetContractForContractIdUseCase()
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TERMINATION_FLOW to true))
    val presenter = ContractDetailPresenter(
      contractId = getContractForContractIdUseCase.getValIdWithoutTerminationDate(),
      featureManager = featureManager,
      getContractForContractIdUseCase = getContractForContractIdUseCase,
    )
    presenter.test(
      ContractDetailsUiState.Error,
    ) {
      assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Error>()
      sendEvent(ContractDetailsEvent.RetryLoadingContract)
      assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Loading>()
      getContractForContractIdUseCase.addInsuranceWithNoTerminationDateToResponseTurbine()
      assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Success>()
    }
  }

  @Test
  fun `with an initial error state, if a good response comes with the flow, show success state`() = runTest {
    val getContractForContractIdUseCase = FakeGetContractForContractIdUseCase()
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TERMINATION_FLOW to true))
    val presenter = ContractDetailPresenter(
      contractId = getContractForContractIdUseCase.getValIdWithoutTerminationDate(),
      featureManager = featureManager,
      getContractForContractIdUseCase = getContractForContractIdUseCase,
    )
    presenter.test(
      ContractDetailsUiState.Error,
    ) {
      assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Error>()
      assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Loading>()
      getContractForContractIdUseCase.addInsuranceWithNoTerminationDateToResponseTurbine()
      assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Success>()
    }
  }

  @Test
  fun `with an initial success state do not show loading if get the same response`() = runTest {
    val getContractForContractIdUseCase = FakeGetContractForContractIdUseCase()
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TERMINATION_FLOW to true))
    val presenter = ContractDetailPresenter(
      contractId = getContractForContractIdUseCase.getValIdWithoutTerminationDate(),
      featureManager = featureManager,
      getContractForContractIdUseCase = getContractForContractIdUseCase,
    )
    val successState = ContractDetailsUiState.Success(
      getContractForContractIdUseCase.getInsuranceWithOutTerminationDate(),
      true,
    )
    presenter.test(
      successState,
    ) {
      assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Success>()
      sendEvent(ContractDetailsEvent.RetryLoadingContract)
      getContractForContractIdUseCase.addInsuranceWithNoTerminationDateToResponseTurbine()
      awaitUnchanged()
    }
  }

  @Test
  fun `with an initial success state do not show loading if get the different successful response`() = runTest {
    val getContractForContractIdUseCase = FakeGetContractForContractIdUseCase()
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TERMINATION_FLOW to true))
    val presenter = ContractDetailPresenter(
      contractId = getContractForContractIdUseCase.getValIdWithoutTerminationDate(),
      featureManager = featureManager,
      getContractForContractIdUseCase = getContractForContractIdUseCase,
    )
    val successStateFirst = ContractDetailsUiState.Success(
      getContractForContractIdUseCase.getInsuranceWithOutTerminationDate(),
      true,
    )
    val successStateSecond = ContractDetailsUiState.Success(
      getContractForContractIdUseCase.getInsuranceWithTerminationDate(),
      false,
    )
    presenter.test(
      successStateFirst,
    ) {
      assertThat(awaitItem()).isEqualTo(successStateFirst)
      getContractForContractIdUseCase.addInsuranceWithTerminationDateToResponseTurbine()
      assertThat(awaitItem()).isEqualTo(successStateSecond)
    }
  }

  @Test
  fun `if termination flow disabled not show cancel insurance button`() = runTest {
    val getContractForContractIdUseCase = FakeGetContractForContractIdUseCase()
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TERMINATION_FLOW to false))
    val presenter = ContractDetailPresenter(
      contractId = getContractForContractIdUseCase.getValIdWithoutTerminationDate(),
      featureManager = featureManager,
      getContractForContractIdUseCase = getContractForContractIdUseCase,
    )
    presenter.test(ContractDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContractDetailsUiState.Loading)
      getContractForContractIdUseCase.addInsuranceWithNoTerminationDateToResponseTurbine()
      assertThat(awaitItem()).isEqualTo(
        ContractDetailsUiState.Success(
          allowTerminatingInsurance = false,
          insuranceContract = getContractForContractIdUseCase.getInsuranceWithOutTerminationDate(),
        ),
      )
    }
  }

  @Test
  fun `if contractId is wrong show no contract found state`() = runTest {
    val getContractForContractIdUseCase = FakeGetContractForContractIdUseCase()
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TERMINATION_FLOW to false))
    val presenter = ContractDetailPresenter(
      contractId = getContractForContractIdUseCase.getInvalidId(),
      featureManager = featureManager,
      getContractForContractIdUseCase = getContractForContractIdUseCase,
    )
    presenter.test(ContractDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContractDetailsUiState.Loading)
      getContractForContractIdUseCase.addContractNotFoundToResponseTurbine()
      assertThat(awaitItem()).isEqualTo(
        ContractDetailsUiState.NoContractFound,
      )
    }
  }

  @Test
  fun `if contractId is okay show success state`() = runTest {
    val getContractForContractIdUseCase = FakeGetContractForContractIdUseCase()
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TERMINATION_FLOW to false))
    val presenter = ContractDetailPresenter(
      contractId = getContractForContractIdUseCase.getValIdWithoutTerminationDate(),
      featureManager = featureManager,
      getContractForContractIdUseCase = getContractForContractIdUseCase,
    )
    presenter.test(ContractDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContractDetailsUiState.Loading)
      getContractForContractIdUseCase.addInsuranceWithNoTerminationDateToResponseTurbine()
      assertThat(awaitItem()).isInstanceOf<ContractDetailsUiState.Success>()
    }
  }

  @Test
  fun `if termination is enabled but contract has termination date not show cancel insurance button`() = runTest {
    val getContractForContractIdUseCase = FakeGetContractForContractIdUseCase()
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TERMINATION_FLOW to true))
    val presenter = ContractDetailPresenter(
      contractId = getContractForContractIdUseCase.getValIdWithTerminationDate(),
      featureManager = featureManager,
      getContractForContractIdUseCase = getContractForContractIdUseCase,
    )
    presenter.test(ContractDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContractDetailsUiState.Loading)
      getContractForContractIdUseCase.addInsuranceWithTerminationDateToResponseTurbine()
      assertThat(awaitItem()).isEqualTo(
        ContractDetailsUiState.Success(
          insuranceContract = getContractForContractIdUseCase.getInsuranceWithTerminationDate(),
          allowTerminatingInsurance = false,
        ),
      )
    }
  }

  internal class FakeGetContractForContractIdUseCase : GetContractForContractIdUseCase {
    private val insuranceWithNoTerminationDate = InsuranceContract(
      "contractId1",
      "displayName#1",
      exposureDisplayName = "Test exposure",
      inceptionDate = LocalDate.fromEpochDays(200),
      terminationDate = null,
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
    )

    private val insuranceWithTerminationDate = InsuranceContract(
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
    )

    private val responseTurbine = Turbine<Either<GetContractForContractIdError, InsuranceContract>>()

    override fun invoke(contractId: String): Flow<Either<GetContractForContractIdError, InsuranceContract>> {
      return responseTurbine.asChannel().receiveAsFlow()
    }

    fun getValIdWithTerminationDate() = insuranceWithTerminationDate.id

    fun getInsuranceWithTerminationDate() = insuranceWithTerminationDate

    fun getValIdWithoutTerminationDate() = insuranceWithNoTerminationDate.id

    fun getInsuranceWithOutTerminationDate() = insuranceWithNoTerminationDate

    fun getInvalidId() = "invalid_id"

    fun addInsuranceWithTerminationDateToResponseTurbine() {
      responseTurbine.add(insuranceWithTerminationDate.right())
    }

    fun addInsuranceWithNoTerminationDateToResponseTurbine() {
      responseTurbine.add(insuranceWithNoTerminationDate.right())
    }

    fun addContractNotFoundToResponseTurbine() {
      responseTurbine.add(GetContractForContractIdError.NoContractFound(ErrorMessage()).left())
    }

    fun addErrorToResponse() {
      responseTurbine.add(GetContractForContractIdError.GenericError(ErrorMessage()).left())
    }
  }
}
