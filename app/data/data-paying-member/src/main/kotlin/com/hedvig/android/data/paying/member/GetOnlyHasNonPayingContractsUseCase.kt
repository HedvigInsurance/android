package com.hedvig.android.data.paying.member

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage

/**
 * Returns true when all the member's active contracts are non-paying contracts.
 */
interface GetOnlyHasNonPayingContractsUseCase {
  suspend fun invoke(): Either<ErrorMessage, Boolean>
}

