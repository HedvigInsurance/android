package com.hedvig.app.di

import android.content.Context
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.util.svg.GlideApp
import com.hedvig.app.util.svg.SvgSoftwareLayerSetter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RequestBuilderModule {

    @Provides
    fun provideRequestBuilder(
        @ApplicationContext context: Context
    ): RequestBuilder<PictureDrawable> {
        return GlideApp.with(context)
            .`as`(PictureDrawable::class.java)
            .listener(SvgSoftwareLayerSetter())
    }
}
