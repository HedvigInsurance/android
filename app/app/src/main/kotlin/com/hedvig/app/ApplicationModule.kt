@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.app

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.interceptor.ApolloInterceptor
import com.apollographql.apollo3.network.okHttpClient
import com.apollographql.apollo3.network.ws.SubscriptionWsProtocol
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.di.apolloAuthListenersModule
import com.hedvig.android.apollo.auth.listeners.di.languageAuthListenersModule
import com.hedvig.android.apollo.auth.listeners.subscription.ReopenSubscriptionException
import com.hedvig.android.apollo.di.apolloClientModule
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.app.di.appModule
import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.auth.di.authModule
import com.hedvig.android.auth.interceptor.AuthTokenRefreshingInterceptor
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.coreCommonModule
import com.hedvig.android.core.common.di.datastoreFileQualifier
import com.hedvig.android.core.datastore.di.dataStoreModule
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.di.demoModule
import com.hedvig.android.data.settings.datastore.di.settingsDatastoreModule
import com.hedvig.android.data.travelcertificate.di.claimFlowDataModule
import com.hedvig.android.data.travelcertificate.di.travelCertificateDataModule
import com.hedvig.android.datadog.core.addDatadogConfiguration
import com.hedvig.android.datadog.core.di.datadogModule
import com.hedvig.android.datadog.demo.tracking.di.datadogDemoTrackingModule
import com.hedvig.android.feature.changeaddress.di.changeAddressModule
import com.hedvig.android.feature.chat.ChatEventStore
import com.hedvig.android.feature.chat.ChatRepository
import com.hedvig.android.feature.chat.di.chatModule
import com.hedvig.android.feature.claim.details.di.claimDetailsModule
import com.hedvig.android.feature.claimtriaging.di.claimTriagingModule
import com.hedvig.android.feature.connect.payment.adyen.di.adyenFeatureModule
import com.hedvig.android.feature.connect.payment.trustly.di.connectPaymentTrustlyModule
import com.hedvig.android.feature.editcoinsured.di.editCoInsuredModule
import com.hedvig.android.feature.forever.di.foreverModule
import com.hedvig.android.feature.home.di.homeModule
import com.hedvig.android.feature.insurances.di.insurancesModule
import com.hedvig.android.feature.login.di.loginModule
import com.hedvig.android.feature.odyssey.di.odysseyModule
import com.hedvig.android.feature.payments.di.paymentsModule
import com.hedvig.android.feature.profile.di.profileModule
import com.hedvig.android.feature.terminateinsurance.di.terminateInsuranceModule
import com.hedvig.android.feature.travelcertificate.di.travelCertificateModule
import com.hedvig.android.hanalytics.android.di.hAnalyticsAndroidModule
import com.hedvig.android.hanalytics.di.hAnalyticsModule
import com.hedvig.android.hanalytics.featureflags.di.featureManagerModule
import com.hedvig.android.language.LanguageService
import com.hedvig.android.language.di.languageModule
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.di.marketManagerModule
import com.hedvig.android.memberreminders.di.memberRemindersModule
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.di.deepLinkModule
import com.hedvig.android.notification.badge.data.di.notificationBadgeModule
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.firebase.di.firebaseNotificationModule
import com.hedvig.app.authenticate.LogoutUseCaseImpl
import com.hedvig.app.feature.chat.service.ChatNotificationSender
import com.hedvig.app.feature.chat.service.ReplyWorker
import com.hedvig.app.feature.genericauth.GenericAuthViewModel
import com.hedvig.app.feature.genericauth.otpinput.OtpInputViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.ReviewDialogViewModel
import com.hedvig.app.feature.marketing.MarketingActivity
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationViewModel
import com.hedvig.app.service.push.senders.CrossSellNotificationSender
import com.hedvig.app.service.push.senders.GenericNotificationSender
import com.hedvig.app.service.push.senders.PaymentNotificationSender
import com.hedvig.app.service.push.senders.ReferralsNotificationSender
import com.hedvig.app.util.apollo.DeviceIdInterceptor
import com.hedvig.app.util.apollo.NetworkCacheManagerImpl
import com.hedvig.app.util.apollo.SunsettingInterceptor
import java.io.File
import java.util.Locale
import kotlin.math.pow
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber

private val networkModule = module {
  single { androidApplication() as HedvigApplication }
  single<NormalizedCacheFactory> {
    MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
  }
  factory<OkHttpClient.Builder> {
    val languageService = get<LanguageService>()
    val builder: OkHttpClient.Builder = OkHttpClient.Builder()
      .addDatadogConfiguration(get<HedvigBuildConstants>())
      .addInterceptor { chain ->
        chain.proceed(
          chain
            .request()
            .newBuilder()
            .header("User-Agent", makeUserAgent(languageService.getLocale()))
            .header("Accept-Language", languageService.getLocale().toLanguageTag())
            .header("hedvig-language", languageService.getLocale().toLanguageTag())
            .header("apollographql-client-name", BuildConfig.APPLICATION_ID)
            .header("apollographql-client-version", BuildConfig.VERSION_NAME)
            .header("X-Build-Version", BuildConfig.VERSION_CODE.toString())
            .header("X-App-Version", BuildConfig.VERSION_NAME)
            .header("X-System-Version", Build.VERSION.SDK_INT.toString())
            .header("X-Platform", "ANDROID")
            .header("X-Model", "${Build.MANUFACTURER} ${Build.MODEL}")
            .build(),
        )
      }
      .addInterceptor(DeviceIdInterceptor(get(), get()))
    if (!get<HedvigBuildConstants>().isProduction) {
      val logger = HttpLoggingInterceptor { message ->
        if (message.contains("Content-Disposition")) {
          Timber.tag("OkHttp").v("File upload omitted from log")
        } else {
          Timber.tag("OkHttp").v(message)
        }
      }
      logger.level = HttpLoggingInterceptor.Level.BODY
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
  single<SunsettingInterceptor> { SunsettingInterceptor(get()) } bind ApolloInterceptor::class
  single<ApolloClient.Builder> {
    val interceptors = getAll<ApolloInterceptor>().distinct()
    val accessTokenProvider = get<AccessTokenProvider>()
    ApolloClient.Builder()
      .okHttpClient(get<OkHttpClient>())
      .webSocketReopenWhen { throwable, reconnectAttempt ->
        if (throwable is ReopenSubscriptionException) {
          return@webSocketReopenWhen true
        }
        if (reconnectAttempt < 5) {
          delay(2.0.pow(reconnectAttempt.toDouble()).toLong()) // Retry after 1 - 2 - 4 - 9 - 16 seconds
          return@webSocketReopenWhen true
        }
        false
      }
      .wsProtocol(
        SubscriptionWsProtocol.Factory(
          connectionPayload = {
            val accessToken = accessTokenProvider.provide()
            logcat { "Apollo-kotlin: Subscription acquired auth token: $accessToken" }
            val authorizationHeaderValue = if (accessToken != null) {
              "Bearer $accessToken"
            } else {
              null
            }
            mapOf("Authorization" to authorizationHeaderValue)
          },
        ),
      )
      .normalizedCache(get<NormalizedCacheFactory>())
      .addInterceptors(interceptors)
  }
}

fun makeUserAgent(locale: Locale): String = buildString {
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
  append(locale.language)
  append(")")
}

private val viewModelModule = module {
  viewModel<SimpleSignAuthenticationViewModel> { params ->
    SimpleSignAuthenticationViewModel(params.get(), get(), get(), get())
  }
  viewModel<GenericAuthViewModel> { GenericAuthViewModel(get(), get()) }
  viewModel<OtpInputViewModel> { (verifyUrl: String, resendUrl: String, credential: String) ->
    OtpInputViewModel(
      verifyUrl,
      resendUrl,
      credential,
      get(),
      get(),
    )
  }
  viewModel<ReviewDialogViewModel> { ReviewDialogViewModel(get()) }
}

private val activityNavigatorModule = module {
  single<ActivityNavigator> {
    ActivityNavigator(
      application = get<Application>(),
      loggedOutActivityClass = MarketingActivity::class.java,
      buildConfigApplicationId = BuildConfig.APPLICATION_ID,
      navigateToLoggedInActivity = { clearBackstack ->
        startActivity(
          LoggedInActivity.newInstance(this, clearBackstack),
        )
      },
    )
  }
}

private val buildConstantsModule = module {
  single<HedvigBuildConstants> {
    val context = get<Context>()
    object : HedvigBuildConstants {
      override val urlGiraffeBaseApi: String = context.getString(R.string.BASE_URL)
      override val urlGiraffeGraphql: String = context.getString(R.string.GRAPHQL_URL)
      override val urlGiraffeGraphqlSubscription: String = context.getString(R.string.WS_GRAPHQL_URL)
      override val urlGraphqlOctopus: String = context.getString(R.string.OCTOPUS_GRAPHQL_URL)
      override val urlBaseWeb: String = context.getString(R.string.WEB_BASE_URL)
      override val urlHanalytics: String = context.getString(R.string.HANALYTICS_URL)
      override val urlOdyssey: String = context.getString(R.string.ODYSSEY_URL)
      override val deepLinkHost: String = context.getString(R.string.DEEP_LINK_DOMAIN_HOST)

      override val appVersionName: String = BuildConfig.VERSION_NAME
      override val appVersionCode: String = BuildConfig.VERSION_CODE.toString()

      override val appId: String = BuildConfig.APPLICATION_ID

      override val isDebug: Boolean = BuildConfig.DEBUG
      override val isProduction: Boolean =
        BuildConfig.BUILD_TYPE == "release" && BuildConfig.APPLICATION_ID == "com.hedvig.app"
    }
  }
}

private val notificationModule = module {
  single { PaymentNotificationSender(get(), get(), get()) } bind NotificationSender::class
  single { CrossSellNotificationSender(get(), get()) } bind NotificationSender::class
  single { ChatNotificationSender(get(), get<HedvigDeepLinkContainer>()) } bind NotificationSender::class
  single { ReferralsNotificationSender(get(), get()) } bind NotificationSender::class
  single { GenericNotificationSender(get()) } bind NotificationSender::class
}

private val clockModule = module {
  single<java.time.Clock> { java.time.Clock.systemDefaultZone() }
  single<kotlinx.datetime.Clock> { kotlinx.datetime.Clock.System }
  single<kotlinx.datetime.TimeZone> { kotlinx.datetime.TimeZone.currentSystemDefault() }
}

private val useCaseModule = module {
  single<LogoutUseCase> {
    LogoutUseCaseImpl(
      get<AuthTokenService>(),
      get<ChatEventStore>(),
      get<ApplicationScope>(),
      get<DemoManager>(),
    )
  }
}

private val cacheManagerModule = module {
  single<NetworkCacheManager> { NetworkCacheManagerImpl(get<ApolloClient>(giraffeClient)) }
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

private val coilModule = module {
  single<ImageLoader> {
    ImageLoader.Builder(get())
      .okHttpClient(get<OkHttpClient.Builder>().build())
      .components {
        add(SvgDecoder.Factory())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          add(ImageDecoderDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
      }
      .build()
  }
}

private val workManagerModule = module {
  worker<ReplyWorker>(named<ReplyWorker>()) {
    ReplyWorker(
      context = get<Context>(),
      params = get<WorkerParameters>(),
      chatRepository = get<ChatRepository>(),
      chatNotificationSender = get<ChatNotificationSender>(),
    )
  }
}

val applicationModule = module {
  includes(
    listOf(
      activityNavigatorModule,
      adyenFeatureModule,
      apolloAuthListenersModule,
      apolloClientModule,
      appModule,
      authModule,
      buildConstantsModule,
      cacheManagerModule,
      changeAddressModule,
      chatModule,
      claimDetailsModule,
      claimFlowDataModule,
      claimTriagingModule,
      clockModule,
      coilModule,
      connectPaymentTrustlyModule,
      coreCommonModule,
      dataStoreModule,
      datadogDemoTrackingModule,
      datadogModule,
      datastoreAndroidModule,
      deepLinkModule,
      demoModule,
      featureManagerModule,
      firebaseNotificationModule,
      foreverModule,
      hAnalyticsAndroidModule,
      hAnalyticsModule,
      homeModule,
      insurancesModule,
      languageAuthListenersModule,
      languageModule,
      loginModule,
      marketManagerModule,
      memberRemindersModule,
      networkModule,
      notificationBadgeModule,
      notificationModule,
      odysseyModule,
      paymentsModule,
      profileModule,
      settingsDatastoreModule,
      sharedPreferencesModule,
      terminateInsuranceModule,
      travelCertificateDataModule,
      travelCertificateModule,
      useCaseModule,
      viewModelModule,
      workManagerModule,
      editCoInsuredModule
    ),
  )
}
