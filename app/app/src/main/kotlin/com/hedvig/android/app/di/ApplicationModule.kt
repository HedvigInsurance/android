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
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.auth.listeners.di.apolloAuthListenersModule
import com.hedvig.android.apollo.auth.listeners.di.languageAuthListenersModule
import com.hedvig.android.apollo.di.networkCacheManagerModule
import com.hedvig.android.app.apollo.LoggingInterceptor
import com.hedvig.android.app.apollo.LogoutOnUnauthenticatedInterceptor
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
import com.hedvig.android.auth.di.authModule
import com.hedvig.android.core.appreview.di.coreAppReviewModule
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.buildconstants.di.buildConstantsModule
import com.hedvig.android.core.common.di.baseHttpClientQualifier
import com.hedvig.android.core.common.di.coreCommonModule
import com.hedvig.android.core.common.di.databaseFileQualifier
import com.hedvig.android.core.datastore.di.dataStoreModule
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.di.demoModule
import com.hedvig.android.core.fileupload.fileUploadModule
import com.hedvig.android.data.addons.di.dataAddonsModule
import com.hedvig.android.data.changetier.di.dataChangeTierModule
import com.hedvig.android.data.claimflow.di.claimFlowDataModule
import com.hedvig.android.data.conversations.di.dataConversationsModule
import com.hedvig.android.data.cross.sell.after.claim.closed.di.crossSellAfterClaimClosedModule
import com.hedvig.android.data.cross.sell.after.flow.di.dataCrossSellAfterFlowModule
import com.hedvig.android.data.paying.member.di.dataPayingMemberModule
import com.hedvig.android.data.settings.datastore.di.settingsDatastoreModule
import com.hedvig.android.data.termination.di.terminationDataModule
import com.hedvig.android.database.di.databaseAndroidModule
import com.hedvig.android.database.di.databaseModule
import com.hedvig.android.datadog.core.di.datadogModule
import com.hedvig.android.datadog.demo.tracking.di.datadogDemoTrackingModule
import com.hedvig.android.design.system.hedvig.pdfrenderer.PdfDecoder
import com.hedvig.android.feature.addon.purchase.di.addonPurchaseModule
import com.hedvig.android.feature.change.tier.di.chooseTierModule
import com.hedvig.android.feature.chat.di.chatModule
import com.hedvig.android.feature.claim.details.di.claimDetailsModule
import com.hedvig.android.feature.claimhistory.di.claimHistoryModule
import com.hedvig.android.feature.claimtriaging.di.claimTriagingModule
import com.hedvig.android.feature.connect.payment.trustly.di.connectPaymentTrustlyModule
import com.hedvig.android.feature.cross.sell.sheet.di.featureCrossSellSheetModule
import com.hedvig.android.feature.deleteaccount.di.deleteAccountModule
import com.hedvig.android.feature.editcoinsured.di.editCoInsuredModule
import com.hedvig.android.feature.help.center.di.helpCenterModule
import com.hedvig.android.feature.home.di.homeModule
import com.hedvig.android.feature.insurance.certificate.di.insuranceEvidenceModule
import com.hedvig.android.feature.insurances.di.insurancesModule
import com.hedvig.android.feature.login.di.loginModule
import com.hedvig.android.feature.movingflow.di.movingFlowModule
import com.hedvig.android.feature.odyssey.di.odysseyModule
import com.hedvig.android.feature.payments.di.paymentsModule
import com.hedvig.android.feature.profile.di.profileModule
import com.hedvig.android.feature.terminateinsurance.di.terminateInsuranceModule
import com.hedvig.android.feature.travelcertificate.di.travelCertificateModule
import com.hedvig.android.featureflags.di.featureManagerModule
import com.hedvig.android.language.di.languageMigrationModule
import com.hedvig.android.language.di.languageModule
import com.hedvig.android.logging.device.model.di.loggingDeviceModelModule
import com.hedvig.android.memberreminders.di.memberRemindersModule
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.di.deepLinkModule
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.android.notification.badge.data.di.notificationBadgeModule
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.firebase.di.firebaseNotificationModule
import com.hedvig.android.shared.foreverui.ui.di.foreverModule
import com.hedvig.android.shared.tier.comparison.di.comparisonModule
import com.hedvig.android.shareddi.sharedModule
import com.hedvig.android.tracking.datadog.di.trackingDatadogModule
import com.hedvig.app.BuildConfig
import com.hedvig.feature.claim.chat.di.claimChatModule
import io.ktor.client.HttpClient
import java.io.File
import org.koin.dsl.bind
import org.koin.dsl.module

private val networkModule = module {
  single<ExtraApolloClientConfiguration> {
    object : ExtraApolloClientConfiguration {
      override fun configure(builder: ApolloClient.Builder): ApolloClient.Builder {
        return builder
          .addInterceptor(LogoutOnUnauthenticatedInterceptor(get<AuthTokenService>(), get<DemoManager>()))
          .addInterceptor(LoggingInterceptor())
      }
    }
  }
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
      HedvigNotificationChannel.Payments,
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

  single<InsuranceEvidenceNotificationSender> {
    InsuranceEvidenceNotificationSender(
      get<Context>(),
      get<HedvigBuildConstants>(),
      get<HedvigDeepLinkContainer>(),
      HedvigNotificationChannel.Other,
    )
  } bind NotificationSender::class
}

private val clockModule = module {
  single<java.time.Clock> { java.time.Clock.systemDefaultZone() }
  single<kotlin.time.Clock> { kotlin.time.Clock.System }
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

private val databaseChatAndroidModule = module {
  single<File>(databaseFileQualifier) {
    val applicationContext = get<Context>().applicationContext
    val dbFile = applicationContext.getDatabasePath("hedvig_chat_database.db")
    // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore/src/main/java/androidx/datastore/DataStoreFile.kt;l=35-36
    dbFile
  }
}

private val coilModule = module {
  single<ImageLoader> {
    val applicationContext = get<Context>().applicationContext
    ImageLoader.Builder(get<Context>())
      .components {
        add(KtorNetworkFetcherFactory(get<HttpClient>(baseHttpClientQualifier)))
        add(SvgDecoder.Factory())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          add(AnimatedImageDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
        add(PdfDecoder.Factory())
      }.memoryCache {
        MemoryCache.Builder().maxSizePercent(applicationContext).build()
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
      authModule,
      buildConstantsModule,
      chatModule,
      chooseTierModule,
      claimChatModule,
      claimDetailsModule,
      claimFlowDataModule,
      claimHistoryModule,
      claimTriagingModule,
      clockModule,
      coilModule,
      comparisonModule,
      connectPaymentTrustlyModule,
      coreAppReviewModule,
      coreCommonModule,
      crossSellAfterClaimClosedModule,
      dataAddonsModule,
      dataChangeTierModule,
      dataConversationsModule,
      dataCrossSellAfterFlowModule,
      dataPayingMemberModule,
      dataStoreModule,
      databaseAndroidModule,
      databaseChatAndroidModule,
      databaseModule,
      datadogDemoTrackingModule,
      datadogModule,
      deepLinkModule,
      deleteAccountModule,
      demoModule,
      editCoInsuredModule,
      featureCrossSellSheetModule,
      featureManagerModule,
      fileUploadModule,
      firebaseNotificationModule,
      foreverModule,
      helpCenterModule,
      homeModule,
      insuranceEvidenceModule,
      insurancesModule,
      languageAuthListenersModule,
      languageMigrationModule,
      languageModule,
      loggingDeviceModelModule,
      loginModule,
      memberRemindersModule,
      movingFlowModule,
      networkCacheManagerModule,
      networkModule,
      notificationBadgeModule,
      notificationModule,
      odysseyModule,
      paymentsModule,
      profileModule,
      settingsDatastoreModule,
      sharedModule(AndroidBuildConfig()),
      sharedPreferencesModule,
      terminateInsuranceModule,
      terminationDataModule,
      trackingDatadogModule,
      travelCertificateModule,
      videoPlayerModule,
    ),
  )
}

private class AndroidBuildConfig() : AppBuildConfig {
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
