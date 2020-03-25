package com.hedvig.app

import android.content.Context
import android.os.Build
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCache
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.facebook.appevents.AppEventsLogger
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.firebase.analytics.FirebaseAnalytics
import com.hedvig.app.authenticate.AuthTracker
import com.hedvig.app.data.debit.DirectDebitRepository
import com.hedvig.app.feature.adyen.AdyenViewModel
import com.hedvig.app.feature.adyen.AdyenViewModelImpl
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.feature.chat.service.ChatTracker
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.feature.chat.viewmodel.UserViewModel
import com.hedvig.app.feature.claims.data.ClaimsRepository
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.dashboard.data.DashboardRepository
import com.hedvig.app.feature.dashboard.service.DashboardTracker
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.KeyGearValuationViewModel
import com.hedvig.app.feature.keygear.KeyGearValuationViewModelImpl
import com.hedvig.app.feature.keygear.data.DeviceInformationService
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import com.hedvig.app.feature.keygear.ui.createitem.CreateKeyGearItemViewModel
import com.hedvig.app.feature.keygear.ui.createitem.CreateKeyGearItemViewModelImpl
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModelImpl
import com.hedvig.app.feature.keygear.ui.tab.KeyGearViewModel
import com.hedvig.app.feature.keygear.ui.tab.KeyGearViewModelImpl
import com.hedvig.app.feature.language.LanguageAndMarketViewModel
import com.hedvig.app.feature.language.LanguageRepository
import com.hedvig.app.feature.language.LanguageSelectionTracker
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import com.hedvig.app.feature.loggedin.ui.BaseTabViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInTracker
import com.hedvig.app.feature.marketing.data.MarketingStoriesRepository
import com.hedvig.app.feature.marketing.service.MarketingTracker
import com.hedvig.app.feature.marketing.ui.MarketingStoriesViewModel
import com.hedvig.app.feature.marketing.ui.MarketingStoriesViewModelImpl
import com.hedvig.app.feature.marketpicker.MarketRepository
import com.hedvig.app.feature.norway.NorwegianAuthenticationRepository
import com.hedvig.app.feature.norway.NorwegianAuthenticationViewModel
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.offer.OfferViewModelImpl
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.feature.profile.service.ProfileTracker
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModelImpl
import com.hedvig.app.feature.profile.ui.payment.PaymentTracker
import com.hedvig.app.feature.profile.ui.payment.TrustlyTracker
import com.hedvig.app.feature.ratings.RatingsTracker
import com.hedvig.app.feature.referrals.ReferralRepository
import com.hedvig.app.feature.referrals.ReferralViewModel
import com.hedvig.app.feature.referrals.ReferralsTracker
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.welcome.WelcomeRepository
import com.hedvig.app.feature.welcome.WelcomeTracker
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.feature.whatsnew.WhatsNewTracker
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.service.FileService
import com.hedvig.app.service.LoginStatusService
import com.hedvig.app.terminated.TerminatedTracker
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.viewmodel.DirectDebitViewModel
import com.hedvig.app.viewmodel.DirectDebitViewModelImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import timber.log.Timber
import java.io.File
import java.util.Locale

fun isDebug() = BuildConfig.DEBUG || BuildConfig.APP_ID == "com.hedvig.test.app"

val applicationModule = module {
    single { FirebaseAnalytics.getInstance(get()) }
    single { AppEventsLogger.newLogger(get()) }
    single {
        SimpleCache(
            File(get<Context>().cacheDir, "hedvig_story_video_cache"),
            LeastRecentlyUsedCacheEvictor((10 * 1024 * 1024).toLong()),
            ExoDatabaseProvider(get())
        )
    }
    single<NormalizedCacheFactory<LruNormalizedCache>> {
        LruNormalizedCacheFactory(
            EvictionPolicy.builder().maxSizeBytes(
                1000 * 1024
            ).build()
        )
    }
    single {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original
                    .newBuilder()
                    .method(original.method, original.body)
                get<Context>().getAuthenticationToken()?.let { token ->
                    builder.header("Authorization", token)
                }
                chain.proceed(builder.build())
            }
            .addInterceptor { chain ->
                chain.proceed(
                    chain
                        .request()
                        .newBuilder()
                        .header("User-Agent", makeUserAgent(get()))
                        .build()
                )
            }
            .addInterceptor { chain ->
                chain.proceed(
                    chain
                        .request()
                        .newBuilder()
                        .header("Accept-Language", makeLocaleString(get()))
                        .build()
                )
            }
        if (isDebug()) {
            val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Timber.tag("OkHttp").i(message)
                }
            })
            logger.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logger)
        }
        builder.build()
    }
    single {
        ApolloClientWrapper(get(), get(), get())
    }
}

fun makeUserAgent(context: Context) =
    "${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE}; ${Build.BRAND} ${Build.MODEL}; ${Build.DEVICE}; ${getLocale(
        context
    ).language})"

fun makeLocaleString(context: Context): String =
    getLocale(context).toLanguageTag()

fun getLocale(context: Context): Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    (Language.fromSettings(context)?.apply(context) ?: context).resources.configuration.locales.get(
        0
    )
} else {
    @Suppress("DEPRECATION")
    (Language.fromSettings(context)?.apply(context) ?: context).resources.configuration.locale
}

val viewModelModule = module {
    viewModel { ClaimsViewModel(get(), get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { WhatsNewViewModel(get()) }
    viewModel { BaseTabViewModel(get(), get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { UserViewModel(get(), get()) }
    viewModel { ReferralViewModel(get()) }
    viewModel { WelcomeViewModel(get()) }
    viewModel { NorwegianAuthenticationViewModel(get()) }
}

val marketingModule = module {
    viewModel<MarketingStoriesViewModel> { MarketingStoriesViewModelImpl(get()) }
}

val languageAndMarketModule = module {
    viewModel { LanguageAndMarketViewModel(get(), get(), get()) }
}

val offerModule = module {
    viewModel<OfferViewModel> { OfferViewModelImpl(get()) }
}

val profileModule = module {
    viewModel<ProfileViewModel> { ProfileViewModelImpl(get(), get()) }
}

val directDebitModule = module {
    viewModel<DirectDebitViewModel> { DirectDebitViewModelImpl(get()) }
}

val keyGearModule = module {
    viewModel<KeyGearViewModel> { KeyGearViewModelImpl(get(), get()) }
    viewModel<KeyGearItemDetailViewModel> { KeyGearItemDetailViewModelImpl(get()) }
    viewModel<CreateKeyGearItemViewModel> { CreateKeyGearItemViewModelImpl(get()) }
    viewModel<KeyGearValuationViewModel> { KeyGearValuationViewModelImpl(get()) }
}

val adyenModule = module {
    viewModel<AdyenViewModel> { AdyenViewModelImpl() }
}

val serviceModule = module {
    single { FileService(get()) }
    single { LoginStatusService(get(), get()) }
    single { TabNotificationService(get()) }
    single { DeviceInformationService(get()) }
}

val repositoriesModule = module {
    single { ChatRepository(get(), get(), get()) }
    single { DirectDebitRepository(get()) }
    single { ClaimsRepository(get(), get()) }
    single { DashboardRepository(get()) }
    single { MarketingStoriesRepository(get(), get(), get()) }
    single { ProfileRepository(get()) }
    single { ReferralRepository(get()) }
    single { UserRepository(get()) }
    single { WhatsNewRepository(get(), get()) }
    single { WelcomeRepository(get(), get()) }
    single { OfferRepository(get()) }
    single { LanguageRepository(get()) }
    single { KeyGearItemsRepository(get(), get(), get()) }
    single { MarketRepository(get()) }
    single { NorwegianAuthenticationRepository(get()) }
}

val trackerModule = module {
    single { ClaimsTracker(get()) }
    single { DashboardTracker(get()) }
    single { MarketingTracker(get()) }
    single { ProfileTracker(get()) }
    single { WhatsNewTracker(get()) }
    single { ReferralsTracker(get()) }
    single { TerminatedTracker(get()) }
    single { WelcomeTracker(get()) }
    single { OfferTracker(get(), get()) }
    single { ChatTracker(get()) }
    single { AuthTracker(get()) }
    single { TrustlyTracker(get()) }
    single { PaymentTracker(get()) }
    single { LanguageSelectionTracker(get()) }
    single { RatingsTracker(get()) }
    single { LoggedInTracker(get()) }
    single { KeyGearTracker(get()) }
}
