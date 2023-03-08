package com.hedvig.android.odyssey.search

import com.hedvig.android.odyssey.model.SearchableClaim

internal class GetLocalEntryPointsUseCase : GetClaimEntryPointsUseCase {

  override suspend fun invoke(): CommonClaimsResult {
    return CommonClaimsResult.Success(
      searchableClaims = listOf(
        SearchableClaim("6", displayName = "Broken phone"),
        SearchableClaim("23", displayName = "Broken computer"),
      ),
    )
  }
}
