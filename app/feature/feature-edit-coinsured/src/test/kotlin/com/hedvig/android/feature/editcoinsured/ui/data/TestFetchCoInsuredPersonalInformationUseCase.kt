package com.hedvig.android.feature.editcoinsured.ui.data

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.fx.coroutines.raceN
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.editcoinsured.data.CoInsuredPersonalInformation
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase

internal class TestFetchCoInsuredPersonalInformationUseCase : FetchCoInsuredPersonalInformationUseCase {
  val errorMessages = Turbine<ErrorMessage>()
  val coInsuredPersonalInformation = Turbine<CoInsuredPersonalInformation>()

  override suspend fun invoke(ssn: String): Either<ErrorMessage, CoInsuredPersonalInformation> {
    return raceN(
      { errorMessages.awaitItem() },
      { coInsuredPersonalInformation.awaitItem() },
    )
  }
}
