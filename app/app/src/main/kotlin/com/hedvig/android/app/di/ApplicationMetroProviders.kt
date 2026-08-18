@file:SuppressLint("UnsafeOptInUsageError")

package com.hedvig.android.app.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.app.apollo.LoggingInterceptor
import com.hedvig.android.app.apollo.LogoutOnUnauthenticatedInterceptor
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.common.di.DatabaseFile
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.design.system.hedvig.pdfrenderer.PdfDecoder
import com.hedvig.android.navigation.compose.DeepLinkMatcherProvider
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.app.BuildConfig
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import java.io.File

@ContributesTo(AppScope::class)
interface ApplicationMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideAppBuildConfig(): AppBuildConfig = AndroidBuildConfig()

  @Provides
  @SingleIn(AppScope::class)
  fun provideExtraApolloClientConfiguration(
    authTokenService: AuthTokenService,
    demoManager: DemoManager,
  ): ExtraApolloClientConfiguration = object : ExtraApolloClientConfiguration {
    override fun configure(builder: ApolloClient.Builder): ApolloClient.Builder {
      return builder
        .addInterceptor(LogoutOnUnauthenticatedInterceptor(authTokenService, demoManager))
        .addInterceptor(LoggingInterceptor())
    }
  }

  @Provides
  @SingleIn(AppScope::class)
  fun provideSimpleCache(applicationContext: Context): SimpleCache {
    val context = applicationContext.applicationContext
    val cacheSize = 100 * 1024 * 1024 // 100MB cache
    val cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize.toLong())
    val databaseProvider = StandaloneDatabaseProvider(context)
    return SimpleCache(
      File(context.cacheDir, "media"),
      cacheEvictor,
      databaseProvider,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @DatabaseFile
  fun provideDatabaseFile(applicationContext: Context): File =
    applicationContext.applicationContext.getDatabasePath("hedvig_chat_database.db")

  @Provides
  @SingleIn(AppScope::class)
  fun provideClock(): java.time.Clock = java.time.Clock.systemDefaultZone()

  @Provides
  @SingleIn(AppScope::class)
  fun provideKotlinClock(): kotlin.time.Clock = kotlin.time.Clock.System

  @Provides
  @SingleIn(AppScope::class)
  fun provideTimeZone(): kotlinx.datetime.TimeZone = kotlinx.datetime.TimeZone.currentSystemDefault()

  @Provides
  @SingleIn(AppScope::class)
  fun provideContentResolver(applicationContext: Context): android.content.ContentResolver =
    applicationContext.contentResolver

  @Provides
  @SingleIn(AppScope::class)
  fun provideSharedPreferences(applicationContext: Context): SharedPreferences =
    applicationContext.getSharedPreferences(
      "hedvig_shared_preference",
      MODE_PRIVATE,
    )

  @Provides
  @SingleIn(AppScope::class)
  fun provideHedvigDeepLinkMatcher(deepLinkMatcherProviders: Set<DeepLinkMatcherProvider>): HedvigDeepLinkMatcher =
    HedvigDeepLinkMatcher(deepLinkMatcherProviders.flatMap { it.matchers() })

  @Provides
  @SingleIn(AppScope::class)
  fun provideImageLoader(
    applicationContext: Context,
    @BaseHttpClient httpClient: HttpClient,
  ): ImageLoader {
    val context = applicationContext.applicationContext
    return ImageLoader.Builder(applicationContext)
      .components {
        add(KtorNetworkFetcherFactory(httpClient))
        add(SvgDecoder.Factory())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          add(AnimatedImageDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
        add(PdfDecoder.Factory())
      }.memoryCache {
        MemoryCache.Builder().maxSizePercent(context).build()
      }.diskCache {
        DiskCache
          .Builder()
          .directory(context.cacheDir.resolve("coil_image_cache"))
          .build()
      }.build()
  }
}

private class AndroidBuildConfig : AppBuildConfig {
  override val debug: Boolean = BuildConfig.DEBUG
  override val applicationId: String = BuildConfig.APPLICATION_ID
  override val buildType: String = BuildConfig.BUILD_TYPE
  override val versionCode: Int = BuildConfig.VERSION_CODE
  override val versionName: String = BuildConfig.VERSION_NAME
  override val appFlavor: Flavor = when (applicationId) {
    "com.hedvig.dev.app" if buildType == "debug" -> Flavor.Develop
    "com.hedvig.app" if buildType == "staging" -> Flavor.Staging
    "com.hedvig.app" if buildType == "release" -> Flavor.Production
    else -> error("Wrong mix of applicationId and buildType [$applicationId | $buildType]")
  }
  override val osReleaseVersion: String = Build.VERSION.RELEASE
  override val osSdkVersion: Int = Build.VERSION.SDK_INT
  override val brand: String = Build.BRAND
  override val model: String = Build.MODEL
  override val device: String = Build.DEVICE
  override val manufacturer: String = Build.MANUFACTURER
}
