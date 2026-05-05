package com.hedvig.android.app.crosssell

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.network.clients.safePost
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.utils.io.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class GetMemberAuthorizationCodeUseCase(
  private val httpClient: HttpClient,
  private val hedvigBuildConstants: HedvigBuildConstants,
) {
  suspend fun invoke(): String? {
    return try {
      httpClient
        .safePost("${hedvigBuildConstants.urlAuthService}/member-authorization-codes")
        .fold(
          ifLeft = { networkError ->
            logcat(LogPriority.WARN) { "Failed to fetch member authorization code: ${networkError.message}" }
            null
          },
          ifRight = { httpResponse ->
            if (httpResponse.status.isSuccess()) {
              Json.decodeFromString<MemberAuthorizationCodeResponse>(httpResponse.bodyAsText()).authorizationCode
            } else {
              logcat(LogPriority.WARN) {
                "Failed to fetch member authorization code, status: ${httpResponse.status}"
              }
              null
            }
          },
        )
    } catch (e: Exception) {
      if (e is CancellationException) {
        throw e
      }
      logcat(LogPriority.WARN, e) { "Failed to fetch member authorization code" }
      null
    }
  }
}

@Serializable
private data class MemberAuthorizationCodeResponse(val authorizationCode: String)
