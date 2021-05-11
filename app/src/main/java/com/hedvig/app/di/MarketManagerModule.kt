package com.hedvig.app.di

import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.MarketManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MarketManagerModule {

    @Binds
    abstract fun provideMarketManager(marketManagerImpl: MarketManagerImpl): MarketManager
}
