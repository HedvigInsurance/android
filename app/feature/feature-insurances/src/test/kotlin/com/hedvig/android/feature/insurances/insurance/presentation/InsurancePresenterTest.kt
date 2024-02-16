package com.hedvig.android.feature.insurances.insurance.presentation

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.fx.coroutines.raceN
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsSubList
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.notification.badge.data.crosssell.card.FakeCrossSellCardNotificationBadgeService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.CrossSellsQuery
import octopus.type.CrossSellType
import org.junit.Rule
import org.junit.Test

internal class InsurancePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val validContracts: List<InsuranceContract> = listOf(
    InsuranceContract(
      "contractId#1",
      "displayName#1",
      exposureDisplayName = "Test exposure",
      inceptionDate = LocalDate.fromEpochDays(200),
      terminationDate = LocalDate.fromEpochDays(400),
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
      id = "contractId#2",
      displayName = "displayName#2",
      exposureDisplayName = "Test exposure",
      inceptionDate = LocalDate.fromEpochDays(200),
      terminationDate = LocalDate.fromEpochDays(400),
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
  private val terminatedContracts: List<InsuranceContract> = listOf(
    InsuranceContract(
      id = "contractId#3",
      displayName = "displayName#3",
      exposureDisplayName = "Test exposure",
      inceptionDate = LocalDate.fromEpochDays(200),
      terminationDate = LocalDate.fromEpochDays(400),
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
      id = "contractId#4",
      displayName = "displayName#4",
      exposureDisplayName = "Test exposure",
      inceptionDate = LocalDate.fromEpochDays(200),
      terminationDate = LocalDate.fromEpochDays(400),
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
  private val validCrossSells: List<CrossSellsQuery.Data.CurrentMember.CrossSell> = listOf(
    CrossSellsQuery.Data.CurrentMember.CrossSell(
      id = "crossSellId",
      title = "crossSellTitle",
      description = "crossSellDescription",
      storeUrl = "",
      type = CrossSellType.HOME,
    ),
  )

  @Test
  fun `on launch loading state is true, followed by the loaded state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { FakeCrossSellCardNotificationBadgeService() },
    )
    presenter.test(InsuranceUiState.initialState) {
      awaitItem().also { uiState ->
        assertThat(uiState).isEqualTo(InsuranceUiState.initialState)
        assertThat(uiState.isLoading).isTrue()
        assertThat(uiState.isRetrying).isFalse()
      }

      getInsuranceContractsUseCase.contracts.add(validContracts)
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      awaitItem().also { uiState ->
        assertAll {
          assertThat(uiState.hasError).isFalse()
          assertThat(uiState.isLoading).isFalse()
          assertThat(uiState.isRetrying).isFalse()
          assertThat(uiState.quantityOfCancelledInsurances)
            .isEqualTo(validContracts.count(InsuranceContract::isTerminated))
          assertThat(uiState.contracts.map { it.id }).containsSubList(validContracts.map { it.id })
          assertThat(uiState.crossSells.map { it.id }).containsSubList(validCrossSells.map { it.id })
          assertThat(uiState.showNotificationBadge)
        }
      }
    }
  }

  @Test
  fun `when cross sells fail to load, we get an error state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { FakeCrossSellCardNotificationBadgeService() },
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.contracts.add(validContracts)
      getCrossSellsUseCase.errorMessages.add(ErrorMessage())
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
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { FakeCrossSellCardNotificationBadgeService() },
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.errorMessages.add(ErrorMessage())
      expectNoEvents() // No events while we're still waiting for all backend calls to finish
      getCrossSellsUseCase.errorMessages.add(ErrorMessage())
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
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { FakeCrossSellCardNotificationBadgeService() },
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.errorMessages.add(ErrorMessage())
      getCrossSellsUseCase.crossSells.add(validCrossSells)
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
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isRetrying).isFalse()
        assertThat(uiState.quantityOfCancelledInsurances)
          .isEqualTo(validContracts.count(InsuranceContract::isTerminated))
      }
    }
  }

  @Test
  fun `getting some terminated contracts should not be part of the cards, but show the quantity of them`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { FakeCrossSellCardNotificationBadgeService() },
    )
    val allContracts = validContracts + terminatedContracts
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.contracts.add(allContracts)
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      awaitItem().also { uiState ->
        assertAll {
          assertThat(uiState.contracts.map(InsuranceContract::id))
            .containsSubList(allContracts.filterNot(InsuranceContract::isTerminated).map(InsuranceContract::id))
          assertThat(uiState.quantityOfCancelledInsurances)
            .isEqualTo(allContracts.count(InsuranceContract::isTerminated))
        }
      }
    }
  }

  @Test
  fun `marking cross sells as seen correctly updates storage and the UI state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val crossSellCardNotificationBadgeService = FakeCrossSellCardNotificationBadgeService()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { crossSellCardNotificationBadgeService },
    )
    presenter.test(InsuranceUiState.initialState) {
      assertThat(awaitItem().showNotificationBadge).isEqualTo(InsuranceUiState.initialState.showNotificationBadge)

      getInsuranceContractsUseCase.contracts.add(validContracts)
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      assertThat(awaitItem().showNotificationBadge).isEqualTo(InsuranceUiState.initialState.showNotificationBadge)

      crossSellCardNotificationBadgeService.showNotification.add(true)
      assertThat(awaitItem().showNotificationBadge).isEqualTo(true)

      sendEvent(InsuranceScreenEvent.MarkCardCrossSellsAsSeen)
      assertThat(awaitItem().showNotificationBadge).isEqualTo(false)
    }
  }

  @Test
  fun `when starting presentation with an already loaded initial state, don't briefly show loading`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val crossSellCardNotificationBadgeService = FakeCrossSellCardNotificationBadgeService()
    val presenter = InsurancePresenter(
      { getInsuranceContractsUseCase },
      { getCrossSellsUseCase },
      { crossSellCardNotificationBadgeService },
    )
    val initialState = InsuranceUiState(
      contracts = persistentListOf(),
      crossSells = persistentListOf(),
      showNotificationBadge = false,
      quantityOfCancelledInsurances = 0,
      hasError = false,
      isLoading = false,
      isRetrying = false,
    )
    presenter.test(initialState) {
      awaitItem().also { uiState ->
        assertThat(uiState).isEqualTo(initialState)
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isRetrying).isFalse()
      }

      getInsuranceContractsUseCase.contracts.add(validContracts)
      expectNoEvents()
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      assertThat(awaitItem().isLoading).isEqualTo(false)
    }
  }

  private class FakeGetInsuranceContractsUseCase : GetInsuranceContractsUseCase {
    val errorMessages = Turbine<ErrorMessage>()
    val contracts = Turbine<List<InsuranceContract>>()

    override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, List<InsuranceContract>>> {
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
}
