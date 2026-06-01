package com.hedvig.android.data.paying.member

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import dev.zacsweers.metro.Inject

@Inject
class GetOnlyHasNonPayingContractsUseCaseDemo : GetOnlyHasNonPayingContractsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Boolean> {
    return false.right()
  }
}
