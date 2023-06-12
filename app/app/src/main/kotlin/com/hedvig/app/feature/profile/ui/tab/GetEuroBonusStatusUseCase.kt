package com.hedvig.app.feature.profile.ui.tab

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

internal interface GetEuroBonusStatusUseCase {
  suspend fun invoke(): Either<GetEuroBonusError, EuroBonus>
}

// todo replace with real backend call when we can fetch this data
internal class TemporaryGetEuroBonusStatusUseCase() : GetEuroBonusStatusUseCase {
  var last = true
  override suspend fun invoke(): Either<GetEuroBonusError, EuroBonus> {
    delay(3.seconds) // todo()
    return if (last) {
      GetEuroBonusError.EuroBonusNotApplicable.left()
    } else {
      EuroBonus("1234").right()
    }.also {
      last = !last
    }
  }
}

internal sealed interface GetEuroBonusError {
  object EuroBonusNotApplicable : GetEuroBonusError
  data class Error(
    val errorMessage: ErrorMessage,
  ) : GetEuroBonusError, ErrorMessage by errorMessage
}

internal data class EuroBonus(
  val code: String?,
)
