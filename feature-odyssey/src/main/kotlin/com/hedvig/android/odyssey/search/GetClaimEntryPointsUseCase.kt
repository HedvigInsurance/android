package com.hedvig.android.odyssey.search

import com.hedvig.android.odyssey.model.SearchableClaim

interface GetClaimEntryPointsUseCase {
  suspend operator fun invoke(): CommonClaimsResult
}

sealed interface CommonClaimsResult {
  data class Error(val message: String) : CommonClaimsResult
  data class Success(val searchableClaims: List<SearchableClaim>) : CommonClaimsResult
}
