package com.hedvig.android.auth.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.auth.AndroidAccessTokenProvider
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.auth.LogoutUseCaseImpl
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.auth.event.AuthEventBroadcaster
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.auth.event.AuthEventStorage
import com.hedvig.android.auth.interceptor.AuthTokenRefreshingInterceptor
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.ioDispatcherQualifier
import com.hedvig.android.initializable.Initializable
import com.hedvig.authlib.AuthEnvironment
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.OkHttpNetworkAuthRepository
import kotlin.coroutines.CoroutineContext
import okhttp3.OkHttpClient
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
  single<AccessTokenProvider> { AndroidAccessTokenProvider(get()) }
  single<AuthTokenRefreshingInterceptor> { AuthTokenRefreshingInterceptor(get()) }
  single<AuthTokenService> {
    AuthTokenServiceImpl(
      get<AuthTokenStorage>(),
      get<AuthRepository>(),
      get<AuthEventStorage>(),
      get<ApplicationScope>(),
    )
  }
  single<AuthTokenStorage> { AuthTokenStorage(get<DataStore<Preferences>>()) }
  single<AuthEventStorage> { AuthEventStorage() }
  single<AuthEventBroadcaster> {
    AuthEventBroadcaster(
      authEventStorage = get<AuthEventStorage>(),
      authEventListeners = getAll<AuthEventListener>().toSet(),
      applicationScope = get<ApplicationScope>(),
      coroutineContext = get<CoroutineContext>(ioDispatcherQualifier),
    )
  } bind Initializable::class

  single<MemberIdService> {
    MemberIdService(
      authTokenStorage = get<AuthTokenStorage>(),
    )
  }

  single<AuthRepository> {
    OkHttpNetworkAuthRepository(
      environment = if (get<HedvigBuildConstants>().isProduction) {
        AuthEnvironment.PRODUCTION
      } else {
        AuthEnvironment.STAGING
      },
      additionalHttpHeadersProvider = { emptyMap() },
      okHttpClientBuilder = get<OkHttpClient.Builder>(),
    )
  }

  single<LogoutUseCase> {
    LogoutUseCaseImpl(
      get<AuthTokenService>(),
      get<ApplicationScope>(),
    )
  }
}
