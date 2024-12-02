package com.hedvig.android.auth

import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.io.encoding.Base64
import kotlin.io.encoding.Base64.PaddingOption
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MemberIdService(
  private val authTokenStorage: AuthTokenStorage,
) {
  fun getMemberId(): Flow<String?> {
    return authTokenStorage.getTokens().map { authTokens ->
      authTokens?.accessToken?.token?.let { stringToken ->
        extractMemberIdFromAccessToken(stringToken)
      }
    }.distinctUntilChanged()
  }

  /**
   * [accessToken] must be the token returned from auth-lib. Will simply return null if the wrong token is passed, or if
   * the token is malformed in some way.
   */
  @OptIn(ExperimentalEncodingApi::class)
  private fun extractMemberIdFromAccessToken(accessToken: String): String? {
    return try {
      val payload = accessToken.split(".").getOrNull(1) ?: return null
      val decodedPayload = Base64
        .withPadding(PaddingOption.ABSENT)
        .decode(payload)
        .decodeToString()
      val payloadJsonObject: JsonObject = Json.parseToJsonElement(decodedPayload).jsonObject
      val subContent: JsonElement = payloadJsonObject.getOrElse("sub") {
        logcat(LogPriority.ERROR) { "Failed to find `sub` in jsonElement for accessToken: $accessToken" }
        return null
      }
      val subText = subContent.jsonPrimitive.content
      if (!subText.startsWith("mem_")) {
        logcat(LogPriority.ERROR) { "Failed to find the `mem_` prefix for accessToken: $accessToken" }
        return null
      }
      subText.removePrefix("mem_")
    } catch (exception: SerializationException) {
      logcat(LogPriority.ERROR, exception) { "Got serializationException for accessToken: $accessToken" }
      null
    } catch (exception: IllegalArgumentException) {
      logcat(LogPriority.ERROR, exception) { "Got illegalArgumentException for accessToken: $accessToken" }
      null
    }
  }
}
