package com.hedvig.app.di

import android.content.Context
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManager
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class LocaleBroadcastManagerModule {

    @Provides
    fun provideLocaleBroadcastManager(@ApplicationContext context: Context): LocaleBroadcastManager {
        return LocaleBroadcastManagerImpl(context)
    }
}
