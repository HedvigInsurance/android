package com.hedvig.android.app.apollo

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * The app's Apollo interceptors, shared by every host so they run in the same order everywhere: a member whose
 * session the backend rejects is logged out first, then the request is logged.
 */
@ContributesTo(AppScope::class)
interface ApolloInterceptorMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideExtraApolloClientConfiguration(
    authTokenService: AuthTokenService,
    demoManager: DemoManager,
  ): ExtraApolloClientConfiguration = object : ExtraApolloClientConfiguration {
    override fun configure(builder: ApolloClient.Builder): ApolloClient.Builder {
      return builder
        .addInterceptor(LogoutOnUnauthenticatedInterceptor(authTokenService, demoManager))
        .addInterceptor(LoggingInterceptor())
    }
  }
}
