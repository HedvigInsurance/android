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
import com.hedvig.android.app.navigation.CurrentDestinationHolder
import com.hedvig.android.app.notification.senders.CarAddonSender
import com.hedvig.android.app.notification.senders.ChatNotificationSender
import com.hedvig.android.app.notification.senders.ClaimClosedNotificationSender
import com.hedvig.android.app.notification.senders.ContactInfoSender
import com.hedvig.android.app.notification.senders.CrossSellNotificationSender
import com.hedvig.android.app.notification.senders.GenericNotificationSender
import com.hedvig.android.app.notification.senders.InsuranceEvidenceNotificationSender
import com.hedvig.android.app.notification.senders.InsuranceTabNotificationSender
import com.hedvig.android.app.notification.senders.PaymentNotificationSender
import com.hedvig.android.app.notification.senders.ReferralsNotificationSender
import com.hedvig.android.app.notification.senders.TravelAddonSender
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.common.di.DatabaseFile
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.design.system.hedvig.pdfrenderer.PdfDecoder
import com.hedvig.android.navigation.compose.DeepLinkMatcherProvider
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.permission.PermissionManager
import com.hedvig.app.BuildConfig
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
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

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun providePaymentNotificationSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
  ): NotificationSender = PaymentNotificationSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.Payments,
  )

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideCrossSellNotificationSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
  ): NotificationSender = CrossSellNotificationSender(
    applicationContext,
    permissionManager,
    buildConstants,
    HedvigNotificationChannel.CrossSell,
  )

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideReferralsNotificationSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
  ): NotificationSender = ReferralsNotificationSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.Referrals,
  )

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideGenericNotificationSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
  ): NotificationSender = GenericNotificationSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.Other,
  )

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideChatNotificationSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
    currentDestinationHolder: CurrentDestinationHolder,
  ): NotificationSender = ChatNotificationSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.Chat,
    currentDestinationHolder,
  )

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideClaimClosedNotificationSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
  ): NotificationSender = ClaimClosedNotificationSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.Payments,
  )

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideContactInfoSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
  ): NotificationSender = ContactInfoSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.Other,
  )

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideInsuranceTabNotificationSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
  ): NotificationSender = InsuranceTabNotificationSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.Other,
  )

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideTravelAddonSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
  ): NotificationSender = TravelAddonSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.CrossSell,
  )

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideCarAddonSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
  ): NotificationSender = CarAddonSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.CrossSell,
  )

  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideInsuranceEvidenceNotificationSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
  ): NotificationSender = InsuranceEvidenceNotificationSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.Other,
  )
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
