package com.hedvig.android.feature.editcoinsured.ui.data

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.fx.coroutines.raceN
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeResult
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import kotlinx.datetime.LocalDate

internal class TestCreateMidTermChangeUseCase : CreateMidtermChangeUseCase {
  val errorMessages = Turbine<ErrorMessage>()
  private val createMidtermChangeResult = Turbine<CreateMidtermChangeResult>()

  // Needed because the coInsured parameter to the use case is passed as output
  fun addCreateMidtermChangeResult(
    id: String,
    currentPremium: UiMoney,
    newPremium: UiMoney,
    activatedDate: LocalDate,
  ) {
    createMidtermChangeResult.add(
      CreateMidtermChangeResult(
        id = id,
        currentCost = currentPremium,
        newCost = newPremium,
        activatedDate = activatedDate,
        coInsured = listOf(),
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
