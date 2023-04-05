package com.hedvig.android.odyssey.search

import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeArrayRestCall
import com.hedvig.android.odyssey.model.ItemProblem
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

internal class GetNetworkClaimEntryPointsUseCase(
  private val okhttpClient: OkHttpClient,
  private val odysseyUrl: String,
) : GetClaimEntryPointsUseCase {

  override suspend operator fun invoke(): CommonClaimsResult {
    val url = HttpUrl.Builder()
      .scheme("https")
      .host(odysseyUrl.substringAfter("//"))
      .addPathSegment("api")
      .addPathSegment("entrypoints")
      .addQueryParameter("limit", NR_OF_ENTRYPOINTS)
      .build()

    val request = Request.Builder()
      .url(url)
      .header("Content-Type", "application/json")
      .header("Odyssey-Platform", "android")
      .get()
      .build()

    return when (val result = okhttpClient.newCall(request).safeArrayRestCall()) {
      is OperationResult.Error -> CommonClaimsResult.Error(result.message ?: "Unknown error")
      is OperationResult.Success -> {
        val claimEntryPointDTO = Json.decodeFromString<List<ClaimEntryPointDTO>>(result.data.toString())
        val searchableClaims = claimEntryPointDTO.toSearchableClaims()
        CommonClaimsResult.Success(searchableClaims)
      }
    }
  }
}

private fun List<ClaimEntryPointDTO>.toSearchableClaims() = map {
  SearchableClaim(
    entryPointId = it.id,
    displayName = it.displayName,
    icon = null,
    itemType = ItemType(""),
    itemProblem = ItemProblem(""),
  )
}

private const val NR_OF_ENTRYPOINTS = "20"

@Serializable
internal data class ClaimEntryPointDTO(
  val id: String,
  val displayName: String,
)
