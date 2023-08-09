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
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.molecule.test.test
import com.hedvig.android.notification.badge.data.crosssell.card.FakeCrossSellCardNotificationBadgeService
import giraffe.type.TypeOfContract
import kotlinx.coroutines.test.runTest
import octopus.CrossSalesQuery
import octopus.type.CrossSellType
import org.junit.Assert
import org.junit.Test

internal class InsurancePresenterTest {

  private val validContracts: List<InsuranceContract> = listOf(
    InsuranceContract(
      id = "contractId#1",
      displayName = "displayName#1",
      statusPills = listOf("statuspill#1"),
      detailPills = listOf("detailpill#1"),
      isTerminated = false,
      typeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    ),
    InsuranceContract(
      id = "contractId#2",
      displayName = "displayName#2",
      statusPills = listOf("statuspill#1"),
      detailPills = listOf("detailpill#1"),
      isTerminated = false,
      typeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    ),
  )
  private val terminatedContracts: List<InsuranceContract> = listOf(
    InsuranceContract(
      id = "contractId#3",
      displayName = "displayName#3",
      statusPills = listOf("statuspill#1"),
      detailPills = listOf("detailpill#1"),
      isTerminated = true,
      typeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    ),
    InsuranceContract(
      id = "contractId#4",
      displayName = "displayName#4",
      statusPills = listOf("statuspill#1"),
      detailPills = listOf("detailpill#1"),
      isTerminated = true,
      typeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    ),
  )
  private val validCrossSells: List<CrossSalesQuery.Data.CurrentMember.CrossSell> = listOf(
    CrossSalesQuery.Data.CurrentMember.CrossSell(
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
      getInsuranceContractsUseCase,
      getCrossSellsUseCase,
      FakeCrossSellCardNotificationBadgeService(),
    )
    presenter.test(InsuranceUiState.initialState) {
      awaitItem().also { uiState ->
        assertThat(uiState).isEqualTo(InsuranceUiState.initialState)
        assertThat(uiState.loading).isTrue()
      }

      getInsuranceContractsUseCase.contracts.add(validContracts)
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      awaitItem().also { uiState ->
        assertAll {
          assertThat(uiState.hasError).isFalse()
          assertThat(uiState.loading).isFalse()
          assertThat(uiState.quantityOfCancelledInsurances)
            .isEqualTo(validContracts.count(InsuranceContract::isTerminated))
          assertThat(uiState.insuranceCards.map { it.contractId }).containsSubList(validContracts.map { it.id })
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
      getInsuranceContractsUseCase,
      getCrossSellsUseCase,
      FakeCrossSellCardNotificationBadgeService(),
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.contracts.add(validContracts)
      getCrossSellsUseCase.errorMessages.add(ErrorMessage())
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isTrue()
        assertThat(uiState.loading).isFalse()
        assertThat(uiState.quantityOfCancelledInsurances).isEqualTo(0)
      }
    }
  }

  @Test
  fun `partial backend responses keep the state as loading until everything is done`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val presenter = InsurancePresenter(
      getInsuranceContractsUseCase,
      getCrossSellsUseCase,
      FakeCrossSellCardNotificationBadgeService(),
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.errorMessages.add(ErrorMessage())
      expectNoEvents() // No events while we're still waiting for all backend calls to finish
      getCrossSellsUseCase.errorMessages.add(ErrorMessage())
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isTrue()
        assertThat(uiState.loading).isFalse()
      }
    }
  }

  @Test
  fun `after a fail, retrying and getting good data back results in proper state`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val presenter = InsurancePresenter(
      getInsuranceContractsUseCase,
      getCrossSellsUseCase,
      FakeCrossSellCardNotificationBadgeService(),
    )
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.errorMessages.add(ErrorMessage())
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isTrue()
        assertThat(uiState.loading).isFalse()
        assertThat(uiState.quantityOfCancelledInsurances).isEqualTo(0)
      }

      sendEvent(InsuranceScreenEvent.RetryLoading)
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isFalse()
        assertThat(uiState.loading).isTrue()
        assertThat(uiState.quantityOfCancelledInsurances).isEqualTo(0)
      }

      getCrossSellsUseCase.crossSells.add(validCrossSells)
      getInsuranceContractsUseCase.contracts.add(validContracts)
      awaitItem().also { uiState ->
        assertThat(uiState.hasError).isFalse()
        assertThat(uiState.loading).isFalse()
        assertThat(uiState.quantityOfCancelledInsurances)
          .isEqualTo(validContracts.count(InsuranceContract::isTerminated))
      }
    }
  }

  @Test
  fun `getting some terminated contracts should no be part of the cards, but show the quantity of them`() = runTest {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val presenter = InsurancePresenter(
      getInsuranceContractsUseCase,
      getCrossSellsUseCase,
      FakeCrossSellCardNotificationBadgeService(),
    )
    val allContracts = validContracts + terminatedContracts
    presenter.test(InsuranceUiState.initialState) {
      skipItems(1)

      getInsuranceContractsUseCase.contracts.add(allContracts)
      getCrossSellsUseCase.crossSells.add(validCrossSells)
      awaitItem().also { uiState ->
        Assert.assertArrayEquals(
          "message, then expected, then actual",
          allContracts.filterNot(InsuranceContract::isTerminated).map(InsuranceContract::id).toTypedArray(),
          uiState.insuranceCards.map(InsuranceUiState.InsuranceCard::contractId).toTypedArray(),
        )
        assertAll {
          assertThat(uiState.insuranceCards.map(InsuranceUiState.InsuranceCard::contractId))
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
      getInsuranceContractsUseCase,
      getCrossSellsUseCase,
      crossSellCardNotificationBadgeService,
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

  private class FakeGetInsuranceContractsUseCase : GetInsuranceContractsUseCase {
    val errorMessages = Turbine<ErrorMessage>()
    val contracts = Turbine<List<InsuranceContract>>()

    override suspend fun invoke(): Either<ErrorMessage, List<InsuranceContract>> {
      return raceN(
        { errorMessages.awaitItem() },
        { contracts.awaitItem() },
      )
    }
  }

  private class FakeGetCrossSellsUseCase : GetCrossSellsUseCase {
    val errorMessages = Turbine<ErrorMessage>()
    val crossSells = Turbine<List<CrossSalesQuery.Data.CurrentMember.CrossSell>>()

    override suspend fun invoke(): Either<ErrorMessage, List<CrossSalesQuery.Data.CurrentMember.CrossSell>> {
      return raceN(
        { errorMessages.awaitItem() },
        { crossSells.awaitItem() },
      )
    }
  }
}
