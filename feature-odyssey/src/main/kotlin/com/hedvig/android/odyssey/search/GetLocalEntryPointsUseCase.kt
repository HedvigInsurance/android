package com.hedvig.android.odyssey.search

import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeArrayRestCall
import com.hedvig.android.odyssey.model.ItemProblem
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class GetLocalEntryPointsUseCase : GetClaimEntryPoints {

  override suspend operator fun invoke(): CommonClaimsResult {
    return CommonClaimsResult.Success(
      searchableClaims = listOf(
        SearchableClaim("6", displayName = "Broken phone"),
        SearchableClaim("23", displayName = "Broken computer"),
      ),
    )
  }
}
