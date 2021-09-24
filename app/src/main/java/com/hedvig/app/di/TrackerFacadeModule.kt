package com.hedvig.app.di

import com.hedvig.app.feature.tracking.EngineeringTracker
import com.hedvig.app.feature.tracking.MixpanelTracker
import com.hedvig.app.feature.tracking.TrackingFacade
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class TrackerFacadeModule {

    @Provides
    fun provideTrackingFacade(
        mixpanelTracker: MixpanelTracker,
        engineeringTracker: EngineeringTracker
    ): TrackingFacade {
        return TrackingFacade(
            listOf(mixpanelTracker, engineeringTracker)
        )
    }
}
