package com.hedvig.android.feature.editcoinsured.ui.data

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.fx.coroutines.raceN
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeResult
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.MonthlyCost
import kotlinx.datetime.LocalDate

internal class TestCreateMidTermChangeUseCase : CreateMidtermChangeUseCase {
  val errorMessages = Turbine<ErrorMessage>()
  private val createMidtermChangeResult = Turbine<CreateMidtermChangeResult>()

  // Needed because the coInsured parameter to the use case is passed as output
  fun addCreateMidtermChangeResult(
    id: String,
    currentCost: MonthlyCost,
    newCost: MonthlyCost,
    activatedDate: LocalDate,
  ) {
    createMidtermChangeResult.add(
      CreateMidtermChangeResult(
        id = id,
        currentCost = currentCost,
        newCost = newCost,
        activatedDate = activatedDate,
        coInsured = listOf(),
        newCostBreakDown = listOf(),
      ),
    )
  }

  override suspend fun invoke(
    contractId: String,
    coInsured: List<CoInsured>,
  ): Either<ErrorMessage, CreateMidtermChangeResult> {
    return raceN(
      { errorMessages.awaitItem() },
      { createMidtermChangeResult.awaitItem().copy(coInsured = coInsured) },
    )
  }
}
