package com.hedvig.android.feature.insurances.insurance.presentation

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.fx.coroutines.raceN
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsSubList
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.TravelAddonBannerInfo
import com.hedvig.android.data.addons.data.TravelAddonBannerSource
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.data.InsuranceContract.EstablishedInsuranceContract
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.CrossSellsQuery
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class InsurancePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val validContracts: List<EstablishedInsuranceContract> = listOf(
    EstablishedInsuranceContract(
      "contractId#1",
      "displayName#1",
      exposureDisplayName = "Test exposure",
      inceptionDate = LocalDate.fromEpochDays(200),
      terminationDate = LocalDate.fromEpochDays(400),
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
          tierDescription = "Our standard coverage",
          termsVersion = "SE_DOG_STANDARD-20230330-HEDVIG-null",
        ),
        certificateUrl = null,
        coInsured = listOf(),
        creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        addons = null,
      ),
      upcomingInsuranceAgreement = null,
      renewalDate = LocalDate.fromEpochDays(500),
      supportsAddressChange = false,
      supportsEditCoInsured = true,
      isTerminated = false,
      contractHolderSSN = "",
      contractHolderDisplayName = "",
      supportsTierChange = true,
      tierName = "STANDARD",
    ),
    EstablishedInsuranceContract(
      id = "contractId#2",
      displayName = "displayName#2",
      exposureDisplayName = "Test exposure",
      inceptionDate = LocalDate.fromEpochDays(200),
      terminationDate = LocalDate.fromEpochDays(400),
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
          tierDescription = "Our standard coverage",
          termsVersion = "SE_DOG_STANDARD-20230330-HEDVIG-null",
        ),
        certificateUrl = null,
        coInsured = listOf(),
        creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        addons = null,
      ),
      upcomingInsuranceAgreement = null,
      renewalDate = LocalDate.fromEpochDays(500),
      supportsAddressChange = false,
      supportsEditCoInsured = true,
      isTerminated = false,
      contractHolderSSN = "",
      contractHolderDisplayName = "",
      supportsTierChange = true,
      tierName = "STANDARD",
    ),
  )
  private val terminatedContracts: List<EstablishedInsuranceContract> = listOf(
    EstablishedInsuranceContract(
      id = "contractId#3",
      displayName = "displayName#3",
      exposureDisplayName = "Test exposure",
      inceptionDate = LocalDate.fromEpochDays(200),
      terminationDate = LocalDate.fromEpochDays(400),
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
          tierDescription = "Our standard coverage",
          termsVersion = "SE_DOG_STANDARD-20230330-HEDVIG-null",
        ),
        certificateUrl = null,
        coInsured = listOf(),
        creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        addons = null,
      ),
      upcomingInsuranceAgreement = null,
      renewalDate = LocalDate.fromEpochDays(500),
      supportsAddressChange = false,
      supportsEditCoInsured = true,
      isTerminated = true,
      contractHolderSSN = "",
      contractHolderDisplayName = "",
      supportsTierChange = true,
      tierName = "STANDARD",
    ),
    EstablishedInsuranceContract(
      id = "contractId#4",
      displayName = "displayName#4",
      exposureDisplayName = "Test exposure",
      inceptionDate = LocalDate.fromEpochDays(200),
      terminationDate = LocalDate.fromEpochDays(400),
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
          tierDescription = "Our standard coverage",
          termsVersion = "SE_DOG_STANDARD-20230330-HEDVIG-null",
        ),
        certificateUrl = null,
        coInsured = listOf(),
        creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
        addons = null,
      ),
      upcomingInsuranceAgreement = null,
      renewalDate = LocalDate.fromEpochDays(500),
      supportsAddressChange = false,
      supportsEditCoInsured = true,
      isTerminated = true,
      contractHolderSSN = "",
      contractHolderDisplayName = "",
      supportsTierChange = true,
      tierName = "STANDARD",
    ),
  )
  private val validCrossSells: List<CrossSellsQuery.Data.CurrentMember.CrossSell> = listOf(
    CrossSellsQuery.Data.CurrentMember.CrossSell(
      id = "crossSellId",
      title = "crossSellTitle",
      description = "crossSellDescription",
      storeUrl = "",
      pillowImageLarge = CrossSellsQuery.Data.CurrentMember.CrossSell.PillowImageLarge("", "", ""),
    ),
  )

  @Test
  fun `on launch loading state is true, followed by the loaded state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val getTravelAddonBannerInfoUseCase = FakeGetTravelAddonBannerInfoUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { getTravelAddonBannerInfoUseCase },
    )
    presenter.test(InsuranceUiState.initialState) {
      awaitItem().also { uiState ->
        assertThat(uiState).isEqualTo(InsuranceUiState.initialState)
        assertThat(uiState.isLoading).isTrue()
        assertThat(uiState.isRetrying).isFalse()
      }

      getInsuranceContractsUseCase.contracts.add(validContracts)
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      getTravelAddonBannerInfoUseCase.turbine.add(either { fakeTravelAddon })
      awaitItem().also { uiState ->
        assertAll {
          assertThat(uiState.hasError).isFalse()
          assertThat(uiState.isLoading).isFalse()
          assertThat(uiState.isRetrying).isFalse()
          assertThat(uiState.quantityOfCancelledInsurances)
            .isEqualTo(validContracts.count(EstablishedInsuranceContract::isTerminated))
          assertThat(uiState.contracts.map { it.id }).containsSubList(validContracts.map { it.id })
          assertThat(uiState.crossSells.map { it.id }).containsSubList(validCrossSells.map { it.id })
        }
      }
    }
  }

  @Test
  fun `when cross sells fail to load, we get an error state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val getTravelAddonBannerInfoUseCase = FakeGetTravelAddonBannerInfoUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { getTravelAddonBannerInfoUseCase },
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.contracts.add(validContracts)
      getCrossSellsUseCase.errorMessages.add(ErrorMessage())
      getTravelAddonBannerInfoUseCase.turbine.add(either { fakeTravelAddon })
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isTrue()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isRetrying).isFalse()
        assertThat(uiState.quantityOfCancelledInsurances).isEqualTo(0)
      }
    }
  }

  @Test
  fun `partial backend responses keep the state as loading until everything is done`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val getTravelAddonBannerInfoUseCase = FakeGetTravelAddonBannerInfoUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { getTravelAddonBannerInfoUseCase },
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.errorMessages.add(ErrorMessage())
      expectNoEvents() // No events while we're still waiting for all backend calls to finish
      getCrossSellsUseCase.errorMessages.add(ErrorMessage())
      getTravelAddonBannerInfoUseCase.turbine.add(either { fakeTravelAddon })
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isTrue()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isRetrying).isFalse()
      }
    }
  }

  @Test
  fun `after a fail, retrying and getting good data back results in proper state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val getTravelAddonBannerInfoUseCase = FakeGetTravelAddonBannerInfoUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { getTravelAddonBannerInfoUseCase },
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.errorMessages.add(ErrorMessage())
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      getTravelAddonBannerInfoUseCase.turbine.add(either { fakeTravelAddon })
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isTrue()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isRetrying).isFalse()
        assertThat(uiState.quantityOfCancelledInsurances).isEqualTo(0)
      }

      sendEvent(InsuranceScreenEvent.RetryLoading)
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isRetrying).isTrue()
        assertThat(uiState.quantityOfCancelledInsurances).isEqualTo(0)
      }

      getCrossSellsUseCase.crossSells.add(validCrossSells)
      getInsuranceContractsUseCase.contracts.add(validContracts)
      getTravelAddonBannerInfoUseCase.turbine.add(either { fakeTravelAddon })
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isRetrying).isFalse()
        assertThat(uiState.quantityOfCancelledInsurances)
          .isEqualTo(validContracts.count(EstablishedInsuranceContract::isTerminated))
      }
    }
  }

  @Test
  fun `getting some terminated contracts should not be part of the cards, but show the quantity of them`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val getTravelAddonBannerInfoUseCase = FakeGetTravelAddonBannerInfoUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { getTravelAddonBannerInfoUseCase },
    )
    val allContracts = validContracts + terminatedContracts
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.contracts.add(allContracts)
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      getTravelAddonBannerInfoUseCase.turbine.add(either { fakeTravelAddon })
      awaitItem().also { uiState ->
        assertAll {
          assertThat(uiState.contracts.map(EstablishedInsuranceContract::id))
            .containsSubList(
              allContracts.filterNot(EstablishedInsuranceContract::isTerminated).map(EstablishedInsuranceContract::id),
            )
          assertThat(uiState.quantityOfCancelledInsurances)
            .isEqualTo(allContracts.count(EstablishedInsuranceContract::isTerminated))
        }
      }
    }
  }

  @Test
  fun `The existence of movable contracts determines whether we show the moving flow section or not`(
    @TestParameter supportsAddressChange: Boolean,
  ) = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val getTravelAddonBannerInfoUseCase = FakeGetTravelAddonBannerInfoUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { getTravelAddonBannerInfoUseCase },
    )
    val contracts = validContracts.map { it.copy(supportsAddressChange = supportsAddressChange) }
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.contracts.add(contracts)
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      getTravelAddonBannerInfoUseCase.turbine.add(either { fakeTravelAddon })
      assertThat(awaitItem().shouldSuggestMovingFlow).isEqualTo(supportsAddressChange)
    }
  }

  @Test
  fun `if GetTravelAddonBannerInfoUseCase returns null, don't show addon banner`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val getTravelAddonBannerInfoUseCase = FakeGetTravelAddonBannerInfoUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { getTravelAddonBannerInfoUseCase },
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)
      getInsuranceContractsUseCase.contracts.add(validContracts)
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      getTravelAddonBannerInfoUseCase.turbine.add(either { null })
      assertThat(awaitItem().travelAddonBannerInfo).isEqualTo(null)
    }
  }

  @Test
  fun `if GetTravelAddonBannerInfoUseCase returns not null, show addon banner`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val getTravelAddonBannerInfoUseCase = FakeGetTravelAddonBannerInfoUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { getTravelAddonBannerInfoUseCase },
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)
      getInsuranceContractsUseCase.contracts.add(validContracts)
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      getTravelAddonBannerInfoUseCase.turbine.add(either { fakeTravelAddon })
      assertThat(awaitItem().travelAddonBannerInfo).isEqualTo(fakeTravelAddon)
    }
  }

  @Test
  fun `when starting presentation with an already loaded initial state, don't briefly show loading`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val getTravelAddonBannerInfoUseCase = FakeGetTravelAddonBannerInfoUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { getTravelAddonBannerInfoUseCase },
    )
    val initialState = InsuranceUiState(
      contracts = listOf(),
      crossSells = listOf(),
      quantityOfCancelledInsurances = 0,
      shouldSuggestMovingFlow = false,
      hasError = false,
      isLoading = false,
      isRetrying = false,
      travelAddonBannerInfo = null,
      pendingContracts = listOf(),
    )
    presenter.test(initialState) {
      awaitItem().also { uiState ->
        assertThat(uiState).isEqualTo(initialState)
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isRetrying).isFalse()
      }

      getInsuranceContractsUseCase.contracts.add(validContracts)
      getTravelAddonBannerInfoUseCase.turbine.add(either { fakeTravelAddon })
      expectNoEvents()
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      assertThat(awaitItem().isLoading).isEqualTo(false)
    }
  }

  private class FakeGetInsuranceContractsUseCase : GetInsuranceContractsUseCase {
    val errorMessages = Turbine<ErrorMessage>()
    val contracts = Turbine<List<EstablishedInsuranceContract>>()

    override fun invoke(): Flow<Either<ErrorMessage, List<InsuranceContract>>> {
      return flow {
        emit(
          raceN(
            { errorMessages.awaitItem() },
            { contracts.awaitItem() },
          ),
        )
      }
    }
  }

  private class FakeGetCrossSellsUseCase : GetCrossSellsUseCase {
    val errorMessages = Turbine<ErrorMessage>()
    val crossSells = Turbine<List<CrossSellsQuery.Data.CurrentMember.CrossSell>>()

    override suspend fun invoke(): Either<ErrorMessage, List<CrossSellsQuery.Data.CurrentMember.CrossSell>> {
      return raceN(
        { errorMessages.awaitItem() },
        { crossSells.awaitItem() },
      )
    }
  }

  private class FakeGetTravelAddonBannerInfoUseCase : GetTravelAddonBannerInfoUseCase {
    val turbine = Turbine<Either<ErrorMessage, TravelAddonBannerInfo?>>()

    override fun invoke(source: TravelAddonBannerSource): Flow<Either<ErrorMessage, TravelAddonBannerInfo?>> {
      return turbine.asChannel().receiveAsFlow()
    }
  }

  private val fakeTravelAddon = TravelAddonBannerInfo(
    "Travel",
    "desc",
    listOf(),
    nonEmptyListOf("id"),
  )
}
