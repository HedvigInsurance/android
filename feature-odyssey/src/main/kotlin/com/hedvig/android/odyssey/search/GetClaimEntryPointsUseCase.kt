package com.hedvig.android.odyssey.search

import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeGraphqlCall
import com.hedvig.android.language.LanguageService
import com.hedvig.android.odyssey.model.ItemProblem
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class GetClaimEntryPointsUseCase(
  private val okhttpClient: OkHttpClient,
  private val languageService: LanguageService,
) : GetClaimEntryPoints {

  override suspend operator fun invoke(): CommonClaimsResult {
    val url = HttpUrl.Builder()
      .scheme("https")
      .host("claims-service")
      .addPathSegment("api")
      .addPathSegment("automation-claims")
      .addPathSegment("entrypoints")
      .addQueryParameter("count", NR_OF_ENTRYPOINTS)
      .build()

    val request = Request.Builder()
      .url(url)
      .header("Content-Type", "application/json")
      .get()
      .build()

    return when (val result = okhttpClient.newCall(request).safeGraphqlCall()) {
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
    id = it.id,
    displayName = it.displayName,
    icon = SearchableClaim.Icon(
      darkUrl = it.icon,
      lightUrl = it.icon,
    ),
    itemType = ItemType(""),
    itemProblem = ItemProblem(""),
  )
}

private const val NR_OF_ENTRYPOINTS = "4"

@Serializable
data class ClaimEntryPointDTO(
  val id: String,
  val displayName: String,
  val icon: String,
)
