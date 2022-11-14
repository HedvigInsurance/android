package com.hedvig.android.auth.di

import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.android.auth.SharedPreferencesAuthenticationTokenService
import org.koin.dsl.module

val authModule = module {
  single<AuthenticationTokenService> { SharedPreferencesAuthenticationTokenService(get()) }
}
