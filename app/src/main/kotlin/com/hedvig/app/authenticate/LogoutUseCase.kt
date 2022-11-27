package com.hedvig.app.authenticate

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.android.auth.LoginStatusService
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.app.util.apollo.reconnectSubscriptions
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

class LogoutUseCase(
  private val pushTokenManager: PushTokenManager,
  private val marketManager: MarketManager,
  private val loginStatusService: LoginStatusService,
  private val apolloClient: ApolloClient,
  private val userRepository: UserRepository,
  private val authenticationTokenService: AuthenticationTokenService,
  private val chatEventStore: ChatEventStore,
  private val featureManager: FeatureManager,
  private val hAnalytics: HAnalytics,
  private val applicationScope: ApplicationScope,
) {

  fun invoke() {
    applicationScope.launch { hAnalytics.loggedOut() }
    applicationScope.launch { userRepository.logout() }
    applicationScope.launch {
      loginStatusService.isLoggedIn = false
      authenticationTokenService.authenticationToken = null
    }
    applicationScope.launch { marketManager.market = null }
    applicationScope.launch { pushTokenManager.refreshToken() }
    applicationScope.launch { featureManager.invalidateExperiments() }
    applicationScope.launch { chatEventStore.resetChatClosedCounter() }
    applicationScope.launch { apolloClient.reconnectSubscriptions() }
  }
}
