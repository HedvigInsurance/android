package com.hedvig.app.authenticate

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.auth.AuthTokenServiceProvider
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.util.apollo.reconnectSubscriptions
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

internal class LogoutUseCaseImpl(
  private val marketManager: MarketManager,
  private val apolloClient: ApolloClient,
  private val userRepository: UserRepository,
  private val authTokenServiceProvider: AuthTokenServiceProvider,
  private val chatEventStore: ChatEventStore,
  private val featureManager: FeatureManager,
  private val hAnalytics: HAnalytics,
  private val applicationScope: ApplicationScope,
  private val demoManager: DemoManager,
) : LogoutUseCase {
  override fun invoke() {
    logcat { "Logout usecase called" }
    applicationScope.launch { hAnalytics.loggedOut() }
    applicationScope.launch { userRepository.logout() }
    applicationScope.launch { authTokenServiceProvider.provide().logoutAndInvalidateTokens() }
    applicationScope.launch { marketManager.market = null }
    applicationScope.launch { featureManager.invalidateExperiments() }
    applicationScope.launch { apolloClient.apolloStore.clearAll() }
    applicationScope.launch { chatEventStore.resetChatClosedCounter() }
    applicationScope.launch { apolloClient.reconnectSubscriptions() }
    applicationScope.launch { demoManager.setDemoMode(false) }
  }
}
