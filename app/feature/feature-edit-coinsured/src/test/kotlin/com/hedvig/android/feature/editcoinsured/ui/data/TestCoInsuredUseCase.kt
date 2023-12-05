package com.hedvig.android.feature.editcoinsured.ui.data

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.fx.coroutines.raceN
import com.hedvig.android.feature.editcoinsured.data.CoInsuredError
import com.hedvig.android.feature.editcoinsured.data.CoInsuredResult
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase

internal class TestGetCoInsuredUseCase : GetCoInsuredUseCase {
  val errorMessages = Turbine<CoInsuredError>()
  val coInsured = Turbine<CoInsuredResult>()

  override suspend fun invoke(contractId: String): Either<CoInsuredError, CoInsuredResult> {
    return raceN(
      { errorMessages.awaitItem() },
      { coInsured.awaitItem() },
    )
  }
}
