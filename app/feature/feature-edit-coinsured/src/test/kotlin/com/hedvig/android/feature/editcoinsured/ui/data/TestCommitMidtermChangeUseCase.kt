package com.hedvig.android.feature.editcoinsured.ui.data

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.fx.coroutines.raceN
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeSuccess
import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeUseCase

internal class TestCommitMidtermChangeUseCase : CommitMidtermChangeUseCase {
  val errorMessages = Turbine<ErrorMessage>()
  val commitMidtermChangeResult = Turbine<CommitMidtermChangeSuccess>()

  override suspend fun invoke(intentId: String): Either<ErrorMessage, CommitMidtermChangeSuccess> {
    return raceN(
      { errorMessages.awaitItem() },
      { commitMidtermChangeResult.awaitItem() },
    )
  }
}
