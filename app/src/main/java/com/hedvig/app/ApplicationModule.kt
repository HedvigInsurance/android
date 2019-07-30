package com.hedvig.app

import android.content.Context
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCache
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.firebase.analytics.FirebaseAnalytics
import com.hedvig.app.data.analytics.AnalyticsRepository
import com.hedvig.app.data.debit.DirectDebitRepository
import com.hedvig.app.feature.chat.UserRepository
import com.hedvig.app.feature.claims.data.ClaimsRepository
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.dashboard.data.DashboardRepository
import com.hedvig.app.feature.dashboard.service.DashboardTracker
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import com.hedvig.app.feature.loggedin.ui.BaseTabViewModel
import com.hedvig.app.feature.welcome.WelcomeRepository
import com.hedvig.app.feature.welcome.WelcomeTracker
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.feature.marketing.data.MarketingStoriesRepository
import com.hedvig.app.feature.marketing.service.MarketingTracker
import com.hedvig.app.feature.marketing.ui.MarketingStoriesViewModel
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.feature.profile.service.ProfileTracker
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.referrals.ReferralRepository
import com.hedvig.app.feature.referrals.ReferralViewModel
import com.hedvig.app.feature.referrals.ReferralsTracker
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.feature.whatsnew.WhatsNewTracker
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.service.FileService
import com.hedvig.app.service.LoginStatusService
import com.hedvig.app.service.Referrals
import com.hedvig.app.service.RemoteConfig
import com.hedvig.app.service.TextKeys
import com.hedvig.app.terminated.TerminatedTracker
import com.hedvig.app.viewmodel.DirectDebitViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import timber.log.Timber
import java.io.File
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.chat.ChatRepository
import com.hedvig.app.feature.chat.ChatViewModel
import com.hedvig.app.feature.chat.UserViewModel
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.viewmodel.AnalyticsViewModel

fun isDebug() = BuildConfig.DEBUG || BuildConfig.APP_ID == "com.hedvig.test.app"

val applicationModule = module {
    single { FirebaseAnalytics.getInstance(get()) }
    single {
        SimpleCache(
            File(get<Context>().cacheDir, "hedvig_story_video_cache"),
            LeastRecentlyUsedCacheEvictor((10 * 1024 * 1024).toLong())
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
                    .method(original.method(), original.body())
                get<Context>().getAuthenticationToken()?.let { token ->
                    builder.header("Authorization", token)
                }
                chain.proceed(builder.build())
            }
        if (isDebug()) {
            builder.addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
                Timber.tag("OkHttp").i(message)
            }).setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        builder.build()
    }
    single {
        ApolloClientWrapper(get(), get(), get())
    }
}

val viewModelModule = module {
    viewModel { MarketingStoriesViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { ClaimsViewModel(get(), get()) }
    viewModel { DirectDebitViewModel(get()) }
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { WhatsNewViewModel(get()) }
    viewModel { BaseTabViewModel(get(), get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { UserViewModel(get(), get()) }
    viewModel { ReferralViewModel(get()) }
    viewModel { WelcomeViewModel(get()) }
    viewModel { OfferViewModel(get()) }
}

val serviceModule = module {
    single { FileService(get()) }
    single { LoginStatusService(get(), get()) }
    single { Referrals(get()) }
    single { RemoteConfig() }
    single { TextKeys(get()) }
    single { TabNotificationService(get()) }
    single { AnalyticsViewModel(get()) }
}

val repositoriesModule = module {
    single { ChatRepository(get(), get(), get()) }
    single { DirectDebitRepository(get()) }
    single { ClaimsRepository(get()) }
    single { DashboardRepository(get()) }
    single { MarketingStoriesRepository(get(), get(), get()) }
    single { ProfileRepository(get()) }
    single { ReferralRepository(get()) }
    single { UserRepository(get()) }
    single { WhatsNewRepository(get(), get()) }
    single { WelcomeRepository(get()) }
    single { OfferRepository(get()) }
    single { AnalyticsRepository(get()) }
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
}
