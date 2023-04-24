package com.hedvig.android.odyssey.search

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.odyssey.model.SearchableClaim

internal interface GetClaimEntryPointsUseCase {
  suspend operator fun invoke(): Either<ErrorMessage, CommonClaimsResult>
}

data class CommonClaimsResult(val searchableClaims: List<SearchableClaim>)
