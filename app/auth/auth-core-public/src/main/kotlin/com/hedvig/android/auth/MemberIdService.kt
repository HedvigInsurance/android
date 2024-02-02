package com.hedvig.android.auth

import android.util.Base64
import com.hedvig.android.auth.storage.AuthTokenStorage
import kotlinx.coroutines.flow.Flow
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
    }
  }

  /**
   * [accessToken] must be the token returned from auth-lib. Will simply return null if the wrong token is passed, or if
   * the token is malformed in some way.
   */
  private fun extractMemberIdFromAccessToken(accessToken: String): String? {
    return try {
      val payload = accessToken.split(".").getOrNull(1) ?: return null
      val decodedPayload = Base64.decode(payload, Base64.DEFAULT).decodeToString()
      val payloadJsonObject: JsonObject = Json.parseToJsonElement(decodedPayload).jsonObject
      val subContent: JsonElement = payloadJsonObject.getOrElse("sub") { return null }
      val subText = subContent.jsonPrimitive.content
      if (!subText.startsWith("mem_")) {
        return null
      }
      subText.removePrefix("mem_")
    } catch (ignored: SerializationException) {
      null
    } catch (ignored: IllegalArgumentException) {
      null
    }
  }
}
