package com.hedvig.app.authenticate

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.android.auth.LoginStatusService
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.app.util.apollo.reconnectSubscriptions

class LogoutUseCase(
  private val pushTokenManager: PushTokenManager,
  private val marketManager: MarketManager,
  private val loginStatusService: LoginStatusService,
  private val apolloClient: ApolloClient,
  private val userRepository: UserRepository,
  private val authenticationTokenService: AuthenticationTokenService,
  private val chatEventStore: ChatEventStore,
) {

  sealed class LogoutResult {
    object Success : LogoutResult()
    data class Error(val message: String?) : LogoutResult()
  }

  suspend fun invoke(): LogoutResult = when (val result = userRepository.logout()) {
    is OperationResult.Error -> LogoutResult.Error(result.message)
    is OperationResult.Success -> {
      clearLoginStatus()
      clearMarket()
      clearAuthenticationToken()
      apolloClient.reconnectSubscriptions()
      runCatching { pushTokenManager.refreshToken() }
      chatEventStore.resetChatClosedCounter()
      LogoutResult.Success
    }
  }

  private fun clearAuthenticationToken() {
    authenticationTokenService.authenticationToken = null
  }

  private fun clearMarket() {
    marketManager.market = null
  }

  private fun clearLoginStatus() {
    loginStatusService.isLoggedIn = false
    authenticationTokenService.authenticationToken = null
  }
}
