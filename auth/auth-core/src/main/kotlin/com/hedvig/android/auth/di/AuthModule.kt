package com.hedvig.android.auth.di

import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.auth.AndroidAccessTokenProvider
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.interceptor.AuthTokenRefreshingInterceptor
import com.hedvig.android.auth.interceptor.MigrateTokenInterceptor
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.auth.storage.SharedPreferencesAuthenticationTokenService
import com.hedvig.android.core.common.ApplicationScope
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val authModule = module {
  single<AccessTokenProvider> { AndroidAccessTokenProvider(get(), get()) }
  single<AuthTokenRefreshingInterceptor> { AuthTokenRefreshingInterceptor(get()) }
  single<AuthTokenService> { AuthTokenServiceImpl(get(), get(), get<ApplicationScope>()) }
  single<AuthTokenStorage> { AuthTokenStorage(get()) }
  single<MigrateTokenInterceptor> { MigrateTokenInterceptor(get(), get()) }
  single<SharedPreferencesAuthenticationTokenService> { SharedPreferencesAuthenticationTokenService(get()) }
}
