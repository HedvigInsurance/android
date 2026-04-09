package com.hedvig.android.app.crosssell

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.network.clients.safePost
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.utils.io.CancellationException
import org.json.JSONObject

internal class GetMemberAuthorizationCodeUseCase(
  private val httpClient: HttpClient,
  private val hedvigBuildConstants: HedvigBuildConstants,
) {
  suspend operator fun invoke(): String? {
    return try {
      httpClient
        .safePost("${hedvigBuildConstants.urlGraphqlOctopus}/member-authorization-codes")
        .fold(
          ifLeft = { networkError ->
            logcat(LogPriority.ERROR) { "Failed to fetch member authorization code: ${networkError.message}" }
            null
          },
          ifRight = { httpResponse ->
            if (httpResponse.status.isSuccess()) {
              JSONObject(httpResponse.bodyAsText()).getString("authorizationCode")
            } else {
              logcat(LogPriority.ERROR) {
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
      logcat(LogPriority.ERROR, e) { "Failed to fetch member authorization code" }
      null
    }
  }
}
