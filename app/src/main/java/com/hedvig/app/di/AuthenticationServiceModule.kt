package com.hedvig.app.di

import android.content.SharedPreferences
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.authenticate.SharedPreferencesAuthenticationTokenService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AuthenticationServiceModule {

    @Provides
    fun provideAuthenticationService(
        sharedPreferences: SharedPreferences
    ): AuthenticationTokenService {
        return SharedPreferencesAuthenticationTokenService(sharedPreferences)
    }
}
