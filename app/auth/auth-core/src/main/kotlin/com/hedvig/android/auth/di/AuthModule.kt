package com.hedvig.android.auth.di

import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.auth.AndroidAccessTokenProvider
import com.hedvig.android.auth.AuthTokenServiceDemo
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.AuthTokenServiceProvider
import com.hedvig.android.auth.event.AuthEventBroadcaster
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.auth.interceptor.AuthTokenRefreshingInterceptor
import com.hedvig.android.auth.interceptor.MigrateTokenInterceptor
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.auth.storage.SharedPreferencesAuthenticationTokenService
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.ioDispatcherQualifier
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val authModule = module {
  single<AccessTokenProvider> { AndroidAccessTokenProvider(get()) }
  single<AuthTokenRefreshingInterceptor> { AuthTokenRefreshingInterceptor(get()) }
  single<AuthTokenServiceImpl> { AuthTokenServiceImpl(get(), get(), get(), get<ApplicationScope>()) }
  single<AuthTokenServiceDemo> { AuthTokenServiceDemo() }
  single<AuthTokenStorage> { AuthTokenStorage(get()) }
  single<AuthEventBroadcaster> {
    AuthEventBroadcaster(
      authEventListeners = getAll<AuthEventListener>().toSet(),
      applicationScope = get(),
      coroutineContext = get(ioDispatcherQualifier),
    )
  }
  single<MigrateTokenInterceptor> { MigrateTokenInterceptor(get(), get()) }
  single<SharedPreferencesAuthenticationTokenService> { SharedPreferencesAuthenticationTokenService(get()) }
  single<AuthTokenServiceProvider> {
    AuthTokenServiceProvider(
      demoManager = get(),
      prodImpl = get<AuthTokenServiceImpl>(),
      demoImpl = get<AuthTokenServiceDemo>(),
    )
  }
}
