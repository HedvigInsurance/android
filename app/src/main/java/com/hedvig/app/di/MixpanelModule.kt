package com.hedvig.app.di

import android.content.Context
import com.hedvig.app.R
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class MixpanelModule {

    @Provides
    fun provideMixPanel(@ApplicationContext context: Context): MixpanelAPI {
        return MixpanelAPI.getInstance(
            context,
            context.getString(R.string.MIXPANEL_PROJECT_TOKEN)
        )
    }
}
