package com.hedvig.android.desktop

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.navigation.compose.DeepLinkMatcherProvider
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import java.io.File
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import okio.Path.Companion.toOkioPath

@ContributesTo(AppScope::class)
internal interface DesktopMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideKotlinClock(): Clock = Clock.System

  @Provides
  @SingleIn(AppScope::class)
  fun provideTimeZone(): TimeZone = TimeZone.currentSystemDefault()

  @Provides
  @SingleIn(AppScope::class)
  fun provideHedvigDeepLinkMatcher(deepLinkMatcherProviders: Set<DeepLinkMatcherProvider>): HedvigDeepLinkMatcher =
    HedvigDeepLinkMatcher(deepLinkMatcherProviders.flatMap { it.matchers() })

  @Provides
  @SingleIn(AppScope::class)
  fun provideImageLoader(
    @BaseHttpClient httpClient: HttpClient,
    @DesktopDataDirectory dataDirectory: File,
  ): ImageLoader {
    val context = PlatformContext.INSTANCE
    return ImageLoader.Builder(context)
      .components {
        add(KtorNetworkFetcherFactory(httpClient))
        add(SvgDecoder.Factory())
      }.memoryCache {
        MemoryCache.Builder().maxSizePercent(context).build()
      }.diskCache {
        DiskCache.Builder()
          .directory(dataDirectory.resolve("coil_image_cache").toOkioPath())
          .build()
      }.build()
  }
}
