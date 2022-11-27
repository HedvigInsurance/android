package com.hedvig.android.auth.di

import com.hedvig.android.auth.AuthRepository
import com.hedvig.android.auth.AuthenticationRepository
import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.android.auth.DataStoreAuthenticationRepository
import com.hedvig.android.auth.LoginStatusService
import com.hedvig.android.auth.NetworkAuthRepository
import com.hedvig.android.auth.SharedPreferencesAuthenticationTokenService
import com.hedvig.android.auth.SharedPreferencesLoginStatusService
import org.koin.dsl.module

val authModule = module {
  single<AuthRepository> { NetworkAuthRepository(get(), "https://auth.dev.hedvigit.com") }
  single<AuthenticationRepository> { DataStoreAuthenticationRepository(get(), get()) }
  single<AuthenticationTokenService> { SharedPreferencesAuthenticationTokenService(get()) }
  single<LoginStatusService> { SharedPreferencesLoginStatusService(get(), get()) }
}
