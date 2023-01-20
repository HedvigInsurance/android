package com.hedvig.android.odyssey.repository

import com.hedvig.android.core.common.await
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.android.odyssey.network.toUpdateRequest
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
  suspend fun getOrCreateClaim(
    itemType: String?,
    itemProblem: String?,
  ): ClaimResult

  suspend fun updateClaim(claimState: ClaimState, nrOfInputs: Int): ClaimResult
  suspend fun getClaim(): ClaimResult
  suspend fun openClaim(amount: MonetaryAmount? = null): ClaimResult
}

sealed interface ClaimResult {
  data class Success(
    val claimState: ClaimState,
    val inputs: List<Input>,
    val resolution: Resolution,
  ) : ClaimResult

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

  override suspend fun getOrCreateClaim(itemType: String?, itemProblem: String?): ClaimResult {
    return when (val res = getClaim()) {
      is ClaimResult.Success -> res
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

  override suspend fun updateClaim(claimState: ClaimState, nrOfInputs: Int): ClaimResult {
    val updateRequest = claimState.toUpdateRequest()
    val requestBody = json.encodeToString(updateRequest).toRequestBody()
    val request = Request.Builder()
      .header("Content-Type", "application/json")
      .url("$BASE_URL/api/automation-claims")
      .patch(requestBody)
      .build()

    val response = okHttpClient.newCall(request).await()
    return handleResponse(response, nrOfInputs)
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
    val requestBody = if (amount != null) {
      JSONObject()
        .put("type", "SingleItemPayout")
        .put("payoutAmount", amount.format())
        .put("method", "AutomaticAutogiroPayout")
        .toString()
        .toRequestBody()
    } else {
      ByteArray(0).toRequestBody()
    }

    val request = Request.Builder()
      .header("Content-Type", "application/json")
      .url("$BASE_URL/api/automation-claims/open")
      .post(requestBody)
      .build()

    val response = okHttpClient.newCall(request).await()
    return handleResponse(response)
  }

  private fun handleResponse(response: Response, nrOfInputs: Int? = null) = if (response.isSuccessful) {
    parseClaimDto(response, nrOfInputs)
  } else {
    ClaimResult.Error("${response.code} ${response.message}")
  }

  private fun parseClaimDto(response: Response, nrOfInputs: Int?): ClaimResult {
    val claimResponse = response.body?.string()
    return if (claimResponse == null || claimResponse.isEmpty()) {
      ClaimResult.Error("No claim found in response!")
    } else {
      val claimDto = json.decodeFromString<AutomationClaimDTO2>(claimResponse)
      val (state, inputs, resolution) = claimDto.toClaim(nrOfInputs)
      ClaimResult.Success(
        claimState = state,
        inputs = inputs,
        resolution = resolution,
      )
    }
  }

  companion object {
    private const val BASE_URL = "https://gateway.test.hedvig.com/claims"
  }
}
