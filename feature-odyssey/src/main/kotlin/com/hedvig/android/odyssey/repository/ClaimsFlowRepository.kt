package com.hedvig.android.odyssey

import com.hedvig.android.core.common.await
import com.hedvig.android.odyssey.model.Claim
import com.hedvig.android.odyssey.network.toUpdateRequest
import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.toClaim
import com.hedvig.common.remote.money.MonetaryAmount
import com.hedvig.common.remote.money.format
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

interface ClaimsFlowRepository {
  suspend fun createOrRestartClaim(
    itemType: String?,
    itemProblem: String?,
  ): ClaimResult

  suspend fun updateClaim(claimState: Claim.ClaimState): ClaimResult
  suspend fun getClaim(): ClaimResult
  suspend fun openClaim(amount: MonetaryAmount? = null): ClaimResult
}

sealed interface ClaimResult {
  data class Success(val claim: Claim) : ClaimResult
  data class Error(val message: String) : ClaimResult
}

class NetworkClaimsFlowRepository(
  private val okHttpClient: OkHttpClient,
) : ClaimsFlowRepository {

  private val json = Json {
    allowSpecialFloatingPointValues = true
    isLenient = true
    allowStructuredMapKeys = true
    ignoreUnknownKeys = true
  }

  override suspend fun createOrRestartClaim(itemType: String?, itemProblem: String?): ClaimResult {
    return when (getClaim()) {
      is ClaimResult.Success -> restartClaim(itemType, itemProblem)
      is ClaimResult.Error -> createClaim(itemType, itemProblem)
    }
  }

  private suspend fun createClaim(itemType: String?, itemProblem: String?): ClaimResult {
    val body = JSONObject(mapOf("itemType" to itemType, "itemProblem" to itemProblem))
      .toString()
      .toRequestBody()

    val request = Request.Builder()
      .header("Content-Type", "application/json")
      .url("$BASE_URL/api/automation-claims")
      .post(body)
      .build()

    val response = okHttpClient.newCall(request).await()
    return handleResponse(response)
  }

  private suspend fun restartClaim(itemType: String?, itemProblem: String?): ClaimResult {
    val body = JSONObject(mapOf("itemType" to itemType, "itemProblem" to itemProblem))
      .toString()
      .toRequestBody()

    val request = Request.Builder()
      .header("Content-Type", "application/json")
      .url("$BASE_URL/api/automation-claims/restart")
      .post(body)
      .build()

    val response = okHttpClient.newCall(request).await()
    return handleResponse(response)
  }

  override suspend fun updateClaim(claimState: Claim.ClaimState): ClaimResult {
    val updateRequest = claimState.toUpdateRequest()
    val requestBody = json.encodeToString(updateRequest).toRequestBody()
    val request = Request.Builder()
      .header("Content-Type", "application/json")
      .url("$BASE_URL/api/automation-claims")
      .patch(requestBody)
      .build()

    val response = okHttpClient.newCall(request).await()
    return handleResponse(response)
  }

  override suspend fun getClaim(): ClaimResult {
    val request = Request.Builder()
      .header("Content-Type", "application/json")
      .url("$BASE_URL/api/automation-claims")
      .get()
      .build()

    val response = okHttpClient.newCall(request).await()
    return handleResponse(response)
  }

  override suspend fun openClaim(amount: MonetaryAmount?): ClaimResult {
    val requestJson = if (amount != null) {
      JSONObject()
        .put("type", "SingleItemPayout")
        .put("payoutAmount", amount.format())
        .put("method", "AutomaticAutogiroPayout")
        .toString()
    } else {
      JSONObject().toString()
    }

    val request = Request.Builder()
      .header("Content-Type", "application/json")
      .url("$BASE_URL/api/automation-claims/open")
      .post(requestJson.toRequestBody())
      .build()

    val response = okHttpClient.newCall(request).await()
    return handleResponse(response)
  }

  private fun handleResponse(response: Response) = if (response.isSuccessful) {
    parseClaimDto(response)
  } else {
    ClaimResult.Error("${response.code} ${response.message}")
  }

  private fun parseClaimDto(response: Response): ClaimResult {
    val claimResponse = response.body?.string()
    return if (claimResponse == null || claimResponse.isEmpty()) {
      ClaimResult.Error("No claim found in response!")
    } else {
      val claimDto = json.decodeFromString<AutomationClaimDTO2>(claimResponse)
      ClaimResult.Success(claimDto.toClaim())
    }
  }

  companion object {
    private const val BASE_URL = "https://gateway.test.hedvig.com/claims"
  }
}
