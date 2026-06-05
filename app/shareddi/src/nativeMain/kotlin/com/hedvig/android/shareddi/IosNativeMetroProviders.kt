package com.hedvig.android.shareddi

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

@ContributesTo(AppScope::class)
interface IosNativeMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideImageLoader(
    @BaseHttpClient baseHttpClient: HttpClient,
  ): ImageLoader = ImageLoader.Builder(PlatformContext.INSTANCE)
    .components {
      add(KtorNetworkFetcherFactory(baseHttpClient))
    }
    .build()
}
