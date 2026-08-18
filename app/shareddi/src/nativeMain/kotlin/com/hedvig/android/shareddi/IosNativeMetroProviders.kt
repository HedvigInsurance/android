package com.hedvig.android.shareddi

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
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

  /**
   * iOS has no Nav3 back stack yet, but the shared commonMain ViewModels inject [Backstack]. This
   * stub satisfies that dependency with a plain in-memory list so the graph resolves; the back-stack
   * mutations it receives are not wired to any iOS presentation until iOS navigation lands.
   */
  @Provides
  @SingleIn(AppScope::class)
  fun provideBackstack(): Backstack = object : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableListOf()
  }
}
