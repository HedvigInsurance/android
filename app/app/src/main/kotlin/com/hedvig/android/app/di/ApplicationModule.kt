@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.android.app.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.network.okHttpClient
import com.hedvig.android.apollo.auth.listeners.di.apolloAuthListenersModule
import com.hedvig.android.apollo.auth.listeners.di.languageAuthListenersModule
import com.hedvig.android.apollo.di.networkCacheManagerModule
import com.hedvig.android.app.apollo.DeviceIdInterceptor
import com.hedvig.android.app.apollo.LoggingInterceptor
import com.hedvig.android.app.apollo.LogoutOnUnauthenticatedInterceptor
import com.hedvig.android.app.logginginterceptor.HedvigHttpLoggingInterceptor
import com.hedvig.android.app.notification.senders.ChatNotificationSender
import com.hedvig.android.app.notification.senders.ClaimClosedNotificationSender
import com.hedvig.android.app.notification.senders.ContactInfoSender
import com.hedvig.android.app.notification.senders.CrossSellNotificationSender
import com.hedvig.android.app.notification.senders.GenericNotificationSender
import com.hedvig.android.app.notification.senders.InsuranceTabNotificationSender
import com.hedvig.android.app.notification.senders.PaymentNotificationSender
import com.hedvig.android.app.notification.senders.ReferralsNotificationSender
import com.hedvig.android.app.notification.senders.TravelAddonSender
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.di.authModule
import com.hedvig.android.auth.interceptor.AuthTokenRefreshingInterceptor
import com.hedvig.android.core.appreview.di.coreAppReviewModule
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.coreCommonModule
import com.hedvig.android.core.common.di.databaseFileQualifier
import com.hedvig.android.core.common.di.datastoreFileQualifier
import com.hedvig.android.core.datastore.di.dataStoreModule
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.di.demoModule
import com.hedvig.android.core.fileupload.fileUploadModule
import com.hedvig.android.data.addons.di.dataAddonsModule
import com.hedvig.android.data.changetier.di.dataChangeTierModule
import com.hedvig.android.data.claimflow.di.claimFlowDataModule
import com.hedvig.android.data.conversations.di.dataConversationsModule
import com.hedvig.android.data.paying.member.di.dataPayingMemberModule
import com.hedvig.android.data.settings.datastore.di.settingsDatastoreModule
import com.hedvig.android.data.termination.di.terminationDataModule
import com.hedvig.android.database.di.databaseAndroidModule
import com.hedvig.android.database.di.databaseModule
import com.hedvig.android.datadog.core.addDatadogConfiguration
import com.hedvig.android.datadog.core.di.datadogModule
import com.hedvig.android.datadog.demo.tracking.di.datadogDemoTrackingModule
import com.hedvig.android.design.system.hedvig.pdfrenderer.PdfDecoder
import com.hedvig.android.feature.addon.purchase.di.addonPurchaseModule
import com.hedvig.android.feature.change.tier.di.chooseTierModule
import com.hedvig.android.feature.chat.di.chatModule
import com.hedvig.android.feature.claim.details.di.claimDetailsModule
import com.hedvig.android.feature.claimtriaging.di.claimTriagingModule
import com.hedvig.android.feature.connect.payment.trustly.di.connectPaymentTrustlyModule
import com.hedvig.android.feature.deleteaccount.di.deleteAccountModule
import com.hedvig.android.feature.editcoinsured.di.editCoInsuredModule
import com.hedvig.android.feature.help.center.di.helpCenterModule
import com.hedvig.android.feature.home.di.homeModule
import com.hedvig.android.feature.insurances.di.insurancesModule
import com.hedvig.android.feature.login.di.loginModule
import com.hedvig.android.feature.movingflow.di.movingFlowModule
import com.hedvig.android.feature.odyssey.di.odysseyModule
import com.hedvig.android.feature.payments.di.paymentsModule
import com.hedvig.android.feature.profile.di.profileModule
import com.hedvig.android.feature.terminateinsurance.di.terminateInsuranceModule
import com.hedvig.android.feature.travelcertificate.di.travelCertificateModule
import com.hedvig.android.featureflags.di.featureManagerModule
import com.hedvig.android.language.LanguageService
import com.hedvig.android.language.di.languageMigrationModule
import com.hedvig.android.language.di.languageModule
import com.hedvig.android.market.di.marketManagerModule
import com.hedvig.android.market.di.setMarketModule
import com.hedvig.android.memberreminders.di.memberRemindersModule
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.di.deepLinkModule
import com.hedvig.android.notification.badge.data.di.notificationBadgeModule
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.firebase.di.firebaseNotificationModule
import com.hedvig.android.shared.foreverui.ui.di.foreverModule
import com.hedvig.android.shared.tier.comparison.di.comparisonModule
import com.hedvig.android.tracking.datadog.di.trackingDatadogModule
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import java.io.File
import okhttp3.OkHttpClient
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber

private val networkModule = module {
  single<NormalizedCacheFactory> {
    MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
  }
  factory<OkHttpClient.Builder> {
    val languageService = get<LanguageService>()
    val builder: OkHttpClient.Builder = OkHttpClient
      .Builder()
      .addDatadogConfiguration(get<HedvigBuildConstants>())
      .addInterceptor { chain ->
        chain.proceed(
          chain
            .request()
            .newBuilder()
            .header("User-Agent", makeUserAgent(languageService.getLanguage().toBcp47Format()))
            .header("Accept-Language", languageService.getLanguage().toBcp47Format())
            .header("hedvig-language", languageService.getLanguage().toBcp47Format())
            .header("apollographql-client-name", BuildConfig.APPLICATION_ID)
            .header("apollographql-client-version", BuildConfig.VERSION_NAME)
            .header("X-Build-Version", BuildConfig.VERSION_CODE.toString())
            .header("X-App-Version", BuildConfig.VERSION_NAME)
            .header("X-System-Version", Build.VERSION.SDK_INT.toString())
            .header("X-Platform", "ANDROID")
            .header("X-Model", "${Build.MANUFACTURER} ${Build.MODEL}")
            .build(),
        )
      }.addInterceptor(DeviceIdInterceptor(get(), get()))
    if (!get<HedvigBuildConstants>().isProduction) {
      val logger = HedvigHttpLoggingInterceptor { message ->
        if (message.contains("Content-Disposition")) {
          Timber.tag("OkHttp").v("File upload omitted from log")
        } else {
          Timber.tag("OkHttp").v(message)
        }
      }
      logger.level = HedvigHttpLoggingInterceptor.Level.BODY
      builder.addInterceptor(logger)
    }
    builder
  }
  single<OkHttpClient> {
    // Add auth interceptor on the OkHttpClient itself which is used by GraphQL
    // The OkHttpClient.Builder configuration does not need to get this automatic token refreshing behavior because
    // there are callers which do not need it, or would even stop working if they did, like the coil implementation
    val okHttpBuilder = get<OkHttpClient.Builder>().addInterceptor(get<AuthTokenRefreshingInterceptor>())
    okHttpBuilder.build()
  }
  single<ApolloClient.Builder> {
    ApolloClient
      .Builder()
      .okHttpClient(get<OkHttpClient>())
      .addInterceptor(LoggingInterceptor())
      .addInterceptor(LogoutOnUnauthenticatedInterceptor(get<AuthTokenService>(), get<DemoManager>()))
      .normalizedCache(get<NormalizedCacheFactory>())
  }
  single<ApolloClient> {
    get<ApolloClient.Builder>()
      .copy()
      .httpServerUrl(get<HedvigBuildConstants>().urlGraphqlOctopus)
      .build()
  }
}

fun makeUserAgent(languageBCP47: String): String = buildString {
  append(BuildConfig.APPLICATION_ID)
  append(" ")
  append(BuildConfig.VERSION_NAME)
  append(" ")
  append("(Android")
  append(" ")
  append(Build.VERSION.RELEASE)
  append("; ")
  append(Build.BRAND)
  append(" ")
  append(Build.MODEL)
  append("; ")
  append(Build.DEVICE)
  append("; ")
  append(languageBCP47)
  append(")")
}

@SuppressLint("UnsafeOptInUsageError")
private val videoPlayerModule = module {
  single<SimpleCache> {
    val applicationContext = get<Context>().applicationContext
    val cacheSize = 100 * 1024 * 1024 // 100MB cache
    val cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize.toLong())
    val databaseProvider = StandaloneDatabaseProvider(applicationContext)
    SimpleCache(
      File(applicationContext.cacheDir, "media"),
      cacheEvictor,
      databaseProvider,
    )
  }
}

private val buildConstantsModule = module {
  single<HedvigBuildConstants> {
    val context = get<Context>()
    object : HedvigBuildConstants {
      override val urlGraphqlOctopus: String = context.getString(R.string.OCTOPUS_GRAPHQL_URL)
      override val urlBaseWeb: String = context.getString(R.string.WEB_BASE_URL)
      override val urlOdyssey: String = context.getString(R.string.ODYSSEY_URL)
      override val urlBotService: String = context.getString(R.string.BOT_SERVICE)
      override val urlClaimsService: String = context.getString(R.string.CLAIMS_SERVICE)
      override val deepLinkHosts: List<String> = listOf(
        context.getString(R.string.DEEP_LINK_DOMAIN_HOST) + context.getString(R.string.DEEP_LINK_DOMAIN_PATH_PREFIX),
        context.getString(R.string.DEEP_LINK_DOMAIN_HOST_OLD),
      )

      override val appVersionName: String = BuildConfig.VERSION_NAME
      override val appVersionCode: String = BuildConfig.VERSION_CODE.toString()

      override val appId: String = BuildConfig.APPLICATION_ID

      override val isDebug: Boolean = BuildConfig.DEBUG
      override val isProduction: Boolean =
        BuildConfig.BUILD_TYPE == "release" && BuildConfig.APPLICATION_ID == "com.hedvig.app"
      override val buildApiVersion: Int = Build.VERSION.SDK_INT
    }
  }
}

private val notificationModule = module {
  single<PaymentNotificationSender> {
    PaymentNotificationSender(
      get<Context>(),
      get<HedvigBuildConstants>(),
      get<HedvigDeepLinkContainer>(),
      HedvigNotificationChannel.Payments,
    )
  } bind NotificationSender::class
  single<CrossSellNotificationSender> {
    CrossSellNotificationSender(
      get<Context>(),
      get<HedvigBuildConstants>(),
      HedvigNotificationChannel.CrossSell,
    )
  } bind NotificationSender::class
  single<ReferralsNotificationSender> {
    ReferralsNotificationSender(
      get<Context>(),
      get<HedvigBuildConstants>(),
      get<HedvigDeepLinkContainer>(),
      HedvigNotificationChannel.Referrals,
    )
  } bind NotificationSender::class
  single<GenericNotificationSender> {
    GenericNotificationSender(
      get<Context>(),
      get<HedvigBuildConstants>(),
      HedvigNotificationChannel.Other,
    )
  } bind NotificationSender::class
  single<ChatNotificationSender> {
    ChatNotificationSender(
      get<Context>(),
      get<HedvigBuildConstants>(),
      get<HedvigDeepLinkContainer>(),
      HedvigNotificationChannel.Chat,
    )
  } bind NotificationSender::class
  single<ClaimClosedNotificationSender> {
    ClaimClosedNotificationSender(
      get<Context>(),
      get<HedvigBuildConstants>(),
      get<HedvigDeepLinkContainer>(),
      HedvigNotificationChannel.Payments, // todo: it is related to pay-outs
    )
  } bind NotificationSender::class
  single<ContactInfoSender> {
    ContactInfoSender(
      get<Context>(),
      get<HedvigBuildConstants>(),
      get<HedvigDeepLinkContainer>(),
      HedvigNotificationChannel.Other,
    )
  } bind NotificationSender::class
  single<InsuranceTabNotificationSender> {
    InsuranceTabNotificationSender(
      get<Context>(),
      get<HedvigBuildConstants>(),
      get<HedvigDeepLinkContainer>(),
      HedvigNotificationChannel.Other,
    )
  } bind NotificationSender::class
  single<TravelAddonSender> {
    TravelAddonSender(
      get<Context>(),
      get<HedvigBuildConstants>(),
      get<HedvigDeepLinkContainer>(),
      HedvigNotificationChannel.CrossSell,
    )
  } bind NotificationSender::class
}

private val clockModule = module {
  single<java.time.Clock> { java.time.Clock.systemDefaultZone() }
  single<kotlinx.datetime.Clock> { kotlinx.datetime.Clock.System }
  single<kotlinx.datetime.TimeZone> { kotlinx.datetime.TimeZone.currentSystemDefault() }
}

private val sharedPreferencesModule = module {
  single<SharedPreferences> {
    get<Context>().getSharedPreferences(
      "hedvig_shared_preference",
      MODE_PRIVATE,
    )
  }
}

private val datastoreAndroidModule = module {
  single<File>(datastoreFileQualifier) {
    // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore/src/main/java/androidx/datastore/DataStoreFile.kt;l=35-36
    get<Context>().applicationContext.filesDir
  }
}

private val databaseChatAndroidModule = module {
  single<File>(databaseFileQualifier) {
    val applicationContext = get<Context>()
    val dbFile = applicationContext.getDatabasePath("hedvig_chat_database.db")
    // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore/src/main/java/androidx/datastore/DataStoreFile.kt;l=35-36
    dbFile
  }
}

private val coilModule = module {
  single<ImageLoader> {
    val applicationContext = get<Context>().applicationContext
    ImageLoader
      .Builder(get())
      .okHttpClient(get<OkHttpClient.Builder>().build())
      .components {
        add(SvgDecoder.Factory())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          add(ImageDecoderDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
        add(PdfDecoder.Factory())
      }.memoryCache {
        MemoryCache.Builder(applicationContext).build()
      }.diskCache {
        DiskCache
          .Builder()
          .directory(applicationContext.cacheDir.resolve("coil_image_cache"))
          .build()
      }.build()
  }
}

val applicationModule = module {
  includes(
    listOf(
      addonPurchaseModule,
      apolloAuthListenersModule,
      appModule,
      videoPlayerModule,
      authModule,
      buildConstantsModule,
      chooseTierModule,
      chatModule,
      claimDetailsModule,
      claimFlowDataModule,
      claimTriagingModule,
      clockModule,
      coilModule,
      comparisonModule,
      connectPaymentTrustlyModule,
      coreAppReviewModule,
      coreCommonModule,
      dataChangeTierModule,
      dataConversationsModule,
      dataPayingMemberModule,
      dataStoreModule,
      databaseAndroidModule,
      databaseChatAndroidModule,
      databaseModule,
      datadogDemoTrackingModule,
      datadogModule,
      datastoreAndroidModule,
      deepLinkModule,
      deleteAccountModule,
      demoModule,
      editCoInsuredModule,
      featureManagerModule,
      fileUploadModule,
      firebaseNotificationModule,
      foreverModule,
      helpCenterModule,
      homeModule,
      insurancesModule,
      languageAuthListenersModule,
      languageMigrationModule,
      languageModule,
      loginModule,
      marketManagerModule,
      memberRemindersModule,
      movingFlowModule,
      networkCacheManagerModule,
      networkModule,
      notificationBadgeModule,
      notificationModule,
      odysseyModule,
      paymentsModule,
      profileModule,
      setMarketModule,
      settingsDatastoreModule,
      sharedPreferencesModule,
      terminateInsuranceModule,
      terminationDataModule,
      trackingDatadogModule,
      travelCertificateModule,
      dataAddonsModule,
    ),
  )
}
