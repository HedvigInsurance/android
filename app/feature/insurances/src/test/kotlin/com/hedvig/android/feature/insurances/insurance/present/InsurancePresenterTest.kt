package com.hedvig.android.feature.insurances.insurance.present

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.Turbine
import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.fx.coroutines.raceN
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.notification.badge.data.crosssell.card.FakeCrossSellCardNotificationBadgeService
import giraffe.type.TypeOfContract
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import octopus.CrossSalesQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

internal class InsurancePresenterTest {
  private val seed: InsuranceUiState = InsuranceUiState.InitialState

  private val validContracts: List<InsuranceContract> = listOf(
    InsuranceContract(
      id = "contractId",
      displayName = "displayName",
      statusPills = listOf("statuspill#1"),
      detailPills = listOf("detailpill#1"),
      isTerminated = false,
      typeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    ),
  )
  private val validCrossSell = CrossSalesQuery.Data.CurrentMember.CrossSell(
    id = "crossSellId",
    title = "crossSellTitle",
    description = "crossSellDescription",
    storeUrl = "",
  )

  @Test
  fun `on launch, loading state is true followed by the loaded state`() = runBlocking {
    val getInsuranceContractsUseCase = FakeGetInsuranceContractsUseCase()
    val getCrossSellsUseCase = FakeGetCrossSellsUseCase()
    val presenter = InsurancePresenter(
      getInsuranceContractsUseCase,
      getCrossSellsUseCase,
      FakeCrossSellCardNotificationBadgeService(),
    )
    moleculeFlow(mode = RecompositionMode.Immediate) {
      presenter.present(seed, emptyFlow())
    }.distinctUntilChanged().test {
      assertEquals(seed, awaitItem())

      getInsuranceContractsUseCase.contracts.add(validContracts)
      getCrossSellsUseCase.crossSells.add(listOf(validCrossSell))
      awaitItem().also { uiState ->
        assertFalse(uiState.hasError)
        assertFalse(uiState.loading)
        assertEquals(
          validContracts.count(InsuranceContract::isTerminated),
          uiState.quantityOfCancelledInsurances,
        )
      }
    }
  }

  private class FakeGetInsuranceContractsUseCase : GetInsuranceContractsUseCase {
    val errorMessages = Turbine<ErrorMessage>()
    val contracts = Turbine<List<InsuranceContract>>()

    override suspend fun invoke(): Either<ErrorMessage, List<InsuranceContract>> {
      return raceN(
        { errorMessages.awaitItem() },
        { contracts.awaitItem() },
      ).fold(
        ifLeft = { it.left() },
        ifRight = { it.right() },
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
      ).fold(
        ifLeft = { it.left() },
        ifRight = { it.right() },
      )
    }
  }
}
