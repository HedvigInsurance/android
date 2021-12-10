package com.hedvig.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCache
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.interceptor.ApolloInterceptorFactory
import com.apollographql.apollo.subscription.SubscriptionConnectionParams
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.authenticate.SharedPreferencesAuthenticationTokenService
import com.hedvig.app.authenticate.SharedPreferencesLoginStatusService
import com.hedvig.app.authenticate.insurely.GetDataCollectionUseCase
import com.hedvig.app.authenticate.insurely.InsurelyAuthViewModel
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.adyen.AdyenRepository
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinViewModel
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinViewModelImpl
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutViewModel
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutViewModelImpl
import com.hedvig.app.feature.chat.data.ChatEventDataStore
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.feature.chat.service.ChatTracker
import com.hedvig.app.feature.chat.usecase.TriggerFreeTextChatUseCase
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.feature.chat.viewmodel.UserViewModel
import com.hedvig.app.feature.claims.data.ClaimsRepository
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.connectpayin.ConnectPaymentViewModel
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.ui.CrossSellResultViewModel
import com.hedvig.app.feature.crossselling.ui.CrossSellTracker
import com.hedvig.app.feature.crossselling.ui.CrossSellingResult
import com.hedvig.app.feature.crossselling.ui.detail.CrossSellDetailViewModel
import com.hedvig.app.feature.crossselling.ui.detail.CrossSellFaqViewModel
import com.hedvig.app.feature.crossselling.ui.detail.CrossSellNotificationMetadata
import com.hedvig.app.feature.crossselling.usecase.GetCrossSellsContractTypesUseCase
import com.hedvig.app.feature.crossselling.usecase.GetCrossSellsUseCase
import com.hedvig.app.feature.embark.EmbarkRepository
import com.hedvig.app.feature.embark.EmbarkTracker
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.EmbarkViewModelImpl
import com.hedvig.app.feature.embark.GraphQLQueryUseCase
import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.feature.embark.ValueStoreImpl
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderViewModel
import com.hedvig.app.feature.embark.passages.datepicker.DatePickerViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.ExternalInsurerViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.GetInsuranceProvidersUseCase
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.InsuranceProviderParameter
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.StartDataCollectionUseCase
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionItem
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionParams
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionViewModel
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentViewModel
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionParams
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionViewModel
import com.hedvig.app.feature.embark.passages.textaction.TextActionParameter
import com.hedvig.app.feature.embark.passages.textaction.TextActionViewModel
import com.hedvig.app.feature.genericauth.CreateOtpAttemptUseCase
import com.hedvig.app.feature.genericauth.GenericAuthViewModel
import com.hedvig.app.feature.genericauth.otpinput.OtpInputViewModel
import com.hedvig.app.feature.genericauth.otpinput.ReSendOtpCodeUseCase
import com.hedvig.app.feature.genericauth.otpinput.ReSendOtpCodeUseCaseImpl
import com.hedvig.app.feature.genericauth.otpinput.SendOtpCodeUseCase
import com.hedvig.app.feature.genericauth.otpinput.SendOtpCodeUseCaseImpl
import com.hedvig.app.feature.home.data.GetHomeUseCase
import com.hedvig.app.feature.home.service.HomeTracker
import com.hedvig.app.feature.home.ui.HomeViewModel
import com.hedvig.app.feature.home.ui.HomeViewModelImpl
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressViewModel
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressViewModelImpl
import com.hedvig.app.feature.home.ui.changeaddress.GetAddressChangeStoryIdUseCase
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModelImpl
import com.hedvig.app.feature.insurance.ui.tab.InsuranceViewModel
import com.hedvig.app.feature.insurance.ui.tab.InsuranceViewModelImpl
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsViewModel
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
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import com.hedvig.app.feature.loggedin.ui.LoggedInRepository
import com.hedvig.app.feature.loggedin.ui.LoggedInTracker
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModelImpl
import com.hedvig.app.feature.marketing.data.MarketingRepository
import com.hedvig.app.feature.marketing.service.MarketingTracker
import com.hedvig.app.feature.marketing.ui.MarketingViewModel
import com.hedvig.app.feature.marketing.ui.MarketingViewModelImpl
import com.hedvig.app.feature.marketpicker.LanguageRepository
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManager
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManagerImpl
import com.hedvig.app.feature.marketpicker.MarketPickerTracker
import com.hedvig.app.feature.marketpicker.MarketPickerViewModel
import com.hedvig.app.feature.marketpicker.MarketPickerViewModelImpl
import com.hedvig.app.feature.marketpicker.MarketRepository
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.offer.OfferViewModelImpl
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetViewModel
import com.hedvig.app.feature.offer.ui.changestartdate.EditStartDateUseCase
import com.hedvig.app.feature.offer.ui.checkout.ApproveQuotesUseCase
import com.hedvig.app.feature.offer.ui.checkout.CheckoutViewModel
import com.hedvig.app.feature.offer.ui.checkout.SignQuotesUseCase
import com.hedvig.app.feature.offer.usecase.GetPostSignDependenciesUseCase
import com.hedvig.app.feature.offer.usecase.GetQuoteUseCase
import com.hedvig.app.feature.offer.usecase.GetQuotesUseCase
import com.hedvig.app.feature.offer.usecase.RefreshQuotesUseCase
import com.hedvig.app.feature.onboarding.ChoosePlanRepository
import com.hedvig.app.feature.onboarding.ChoosePlanViewModel
import com.hedvig.app.feature.onboarding.ChoosePlanViewModelImpl
import com.hedvig.app.feature.onboarding.MemberIdRepository
import com.hedvig.app.feature.onboarding.MemberIdViewModel
import com.hedvig.app.feature.onboarding.MemberIdViewModelImpl
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.feature.profile.service.ProfileTracker
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModelImpl
import com.hedvig.app.feature.profile.ui.payment.PaymentRepository
import com.hedvig.app.feature.profile.ui.payment.PaymentTracker
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModelImpl
import com.hedvig.app.feature.ratings.RatingsTracker
import com.hedvig.app.feature.referrals.data.RedeemReferralCodeRepository
import com.hedvig.app.feature.referrals.data.ReferralsRepository
import com.hedvig.app.feature.referrals.service.ReferralsTracker
import com.hedvig.app.feature.referrals.ui.activated.ReferralsActivatedViewModel
import com.hedvig.app.feature.referrals.ui.activated.ReferralsActivatedViewModelImpl
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeViewModel
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeViewModelImpl
import com.hedvig.app.feature.referrals.ui.redeemcode.RedeemCodeViewModel
import com.hedvig.app.feature.referrals.ui.tab.ReferralsViewModel
import com.hedvig.app.feature.referrals.ui.tab.ReferralsViewModelImpl
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.MarketManagerImpl
import com.hedvig.app.feature.settings.SettingsViewModel
import com.hedvig.app.feature.swedishbankid.sign.SwedishBankIdSignViewModel
import com.hedvig.app.feature.swedishbankid.sign.usecase.ManuallyRecheckSwedishBankIdSignStatusUseCase
import com.hedvig.app.feature.swedishbankid.sign.usecase.SubscribeToSwedishBankIdSignStatusUseCase
import com.hedvig.app.feature.tracking.FirebaseTracker
import com.hedvig.app.feature.tracking.MixpanelTracker
import com.hedvig.app.feature.tracking.TrackerSink
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.feature.trustly.TrustlyRepository
import com.hedvig.app.feature.trustly.TrustlyTracker
import com.hedvig.app.feature.trustly.TrustlyViewModel
import com.hedvig.app.feature.trustly.TrustlyViewModelImpl
import com.hedvig.app.feature.welcome.WelcomeRepository
import com.hedvig.app.feature.welcome.WelcomeTracker
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.feature.whatsnew.WhatsNewTracker
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewViewModelImpl
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationViewModel
import com.hedvig.app.feature.zignsec.usecase.StartDanishAuthUseCase
import com.hedvig.app.feature.zignsec.usecase.StartNorwegianAuthUseCase
import com.hedvig.app.feature.zignsec.usecase.SubscribeToAuthStatusUseCase
import com.hedvig.app.service.FileService
import com.hedvig.app.service.badge.CrossSellNotificationBadgeService
import com.hedvig.app.service.badge.NotificationBadgeService
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.app.service.push.managers.CrossSellNotificationManager
import com.hedvig.app.service.push.managers.PaymentNotificationManager
import com.hedvig.app.terminated.TerminatedTracker
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.ApolloTimberLogger
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.apollo.SunsettingInterceptor
import com.hedvig.app.util.featureflags.FeatureManager
import com.mixpanel.android.mpmetrics.MixpanelAPI
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber
import java.time.Clock
import java.util.Locale

fun isDebug() = BuildConfig.DEBUG || BuildConfig.APPLICATION_ID == "com.hedvig.test.app"

fun shouldOverrideFeatureFlags(app: HedvigApplication): Boolean {
    if (app.isTestBuild) {
        return false
    }
    if (BuildConfig.DEBUG) {
        return true
    }
    if (BuildConfig.APPLICATION_ID == "com.hedvig.test.app") {
        return true
    }

    return false
}

val applicationModule = module {
    single { androidApplication() as HedvigApplication }
    single {
        MixpanelAPI.getInstance(
            get(),
            get<Context>().getString(R.string.MIXPANEL_PROJECT_TOKEN)
        )
    }
    single<NormalizedCacheFactory<LruNormalizedCache>> {
        LruNormalizedCacheFactory(
            EvictionPolicy.builder().maxSizeBytes(
                (1000 * 1024).toLong()
            ).build()
        )
    }
    single {
        val marketManager = get<MarketManager>()
        val context = get<Context>()
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original
                    .newBuilder()
                    .method(original.method, original.body)
                get<AuthenticationTokenService>().authenticationToken?.let { token ->
                    builder.header("Authorization", token)
                }
                chain.proceed(builder.build())
            }
            .addInterceptor { chain ->
                chain.proceed(
                    chain
                        .request()
                        .newBuilder()
                        .header("User-Agent", makeUserAgent(context, marketManager.market))
                        .header("Accept-Language", makeLocaleString(context, marketManager.market))
                        .header("apollographql-client-name", BuildConfig.APPLICATION_ID)
                        .header("apollographql-client-version", BuildConfig.VERSION_NAME)
                        .header("X-Build-Version", BuildConfig.VERSION_CODE.toString())
                        .header("X-App-Version", BuildConfig.VERSION_NAME)
                        .header("X-System-Version", Build.VERSION.SDK_INT.toString())
                        .header("X-Platform", "ANDROID")
                        .header("X-Model", "${Build.MANUFACTURER} ${Build.MODEL}")
                        .build()
                )
            }
        if (isDebug()) {
            val logger = HttpLoggingInterceptor { message ->
                if (message.contains("Content-Disposition")) {
                    Timber.tag("OkHttp").i("File upload omitted from log")
                } else {
                    Timber.tag("OkHttp").i(message)
                }
            }
            logger.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logger)
        }
        builder.build()
    }
    single { SunsettingInterceptor.Factory(get()) } bind ApolloInterceptorFactory::class
    single {
        val builder = ApolloClient
            .builder()
            .serverUrl(get<HedvigApplication>().graphqlUrl)
            .okHttpClient(get())
            .subscriptionConnectionParams {
                SubscriptionConnectionParams(
                    mapOf("Authorization" to get<AuthenticationTokenService>().authenticationToken)
                )
            }
            .subscriptionTransportFactory(
                WebSocketSubscriptionTransport.Factory(
                    get<HedvigApplication>().graphqlSubscriptionUrl,
                    get<OkHttpClient>()
                )
            )
            .normalizedCache(get())

        CUSTOM_TYPE_ADAPTERS.customAdapters.forEach { (t, a) -> builder.addCustomTypeAdapter(t, a) }

        getAll<ApolloInterceptorFactory>().distinct().forEach {
            builder.addApplicationInterceptorFactory(it)
        }

        if (isDebug()) {
            builder.logger(ApolloTimberLogger())
        }
        builder.build()
    }
    single { Firebase.analytics }
}

fun makeUserAgent(context: Context, market: Market?) =
    "${
    BuildConfig.APPLICATION_ID
    } ${
    BuildConfig.VERSION_NAME
    } (Android ${
    Build.VERSION.RELEASE
    }; ${
    Build.BRAND
    } ${
    Build.MODEL
    }; ${
    Build.DEVICE
    }; ${
    getLocale(context, market).language
    })"

fun makeLocaleString(context: Context, market: Market?): String = getLocale(context, market).toLanguageTag()

fun getLocale(context: Context, market: Market?): Locale {
    val locale = if (market == null) {
        Language.from(Language.SETTING_EN_SE)
    } else {
        Language.fromSettings(context, market)
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        locale.apply(context).resources.configuration.locales.get(0)
    } else {
        @Suppress("DEPRECATION")
        locale.apply(context).resources.configuration.locale
    }
}

val viewModelModule = module {
    viewModel { ClaimsViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get(), get(), get()) }
    viewModel { UserViewModel(get(), get(), get()) }
    viewModel { RedeemCodeViewModel(get()) }
    viewModel { WelcomeViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { DatePickerViewModel() }
    viewModel { params -> SimpleSignAuthenticationViewModel(params.get(), get(), get(), get()) }
    viewModel { (data: MultiActionParams) -> MultiActionViewModel(data) }
    viewModel { (componentState: MultiActionItem.Component?, multiActionParams: MultiActionParams) ->
        AddComponentViewModel(
            componentState,
            multiActionParams
        )
    }
    viewModel { TerminatedContractsViewModel(get()) }
    viewModel { (autoStartToken: String, quoteIds: List<String>) ->
        SwedishBankIdSignViewModel(autoStartToken, quoteIds, get(), get(), get(), get())
    }
    viewModel { (result: CrossSellingResult) -> CrossSellResultViewModel(result, get()) }
    viewModel { AudioRecorderViewModel(get()) }
    viewModel { CrossSellFaqViewModel(get()) }
    viewModel { (notificationMetadata: CrossSellNotificationMetadata?, crossSell: CrossSellData) ->
        CrossSellDetailViewModel(notificationMetadata, crossSell, get())
    }
    viewModel { GenericAuthViewModel(get()) }
    viewModel { (otpId: String, credential: String) -> OtpInputViewModel(otpId, credential, get(), get(), get()) }
}

val choosePlanModule = module {
    viewModel<ChoosePlanViewModel> { ChoosePlanViewModelImpl(get()) }
}

val onboardingModule = module {
    viewModel<MemberIdViewModel> { MemberIdViewModelImpl(get()) }
}

val marketPickerModule = module {
    viewModel<MarketPickerViewModel> { MarketPickerViewModelImpl(get(), get(), get(), get(), get()) }
}

val loggedInModule = module {
    viewModel<LoggedInViewModel> { LoggedInViewModelImpl(get(), get(), get()) }
}

val whatsNewModule = module {
    viewModel<WhatsNewViewModel> { WhatsNewViewModelImpl(get()) }
}

val insuranceModule = module {
    viewModel<InsuranceViewModel> { InsuranceViewModelImpl(get(), get()) }
    viewModel<ContractDetailViewModel> { ContractDetailViewModelImpl(get(), get(), get()) }
}

val marketingModule = module {
    viewModel<MarketingViewModel> { MarketingViewModelImpl(get(), get()) }
}

val offerModule = module {
    viewModel<OfferViewModel> { (ids: List<String>, shouldShowOnNextAppStart: Boolean) ->
        OfferViewModelImpl(ids, get(), get(), get(), get(), get(), get(), get(), shouldShowOnNextAppStart, get(), get())
    }
}

val profileModule = module {
    viewModel<ProfileViewModel> { ProfileViewModelImpl(get(), get(), get()) }
}

val keyGearModule = module {
    viewModel<KeyGearViewModel> { KeyGearViewModelImpl(get(), get()) }
    viewModel<KeyGearItemDetailViewModel> { KeyGearItemDetailViewModelImpl(get()) }
    viewModel<CreateKeyGearItemViewModel> { CreateKeyGearItemViewModelImpl(get()) }
    viewModel<KeyGearValuationViewModel> { KeyGearValuationViewModelImpl(get()) }
}

val paymentModule = module {
    viewModel<PaymentViewModel> { PaymentViewModelImpl(get(), get()) }
}

val adyenModule = module {
    viewModel<AdyenConnectPayinViewModel> { AdyenConnectPayinViewModelImpl(get()) }
    viewModel<AdyenConnectPayoutViewModel> { AdyenConnectPayoutViewModelImpl(get()) }
}

val embarkModule = module {
    viewModel<EmbarkViewModel> { (storyName: String) ->
        EmbarkViewModelImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            storyName
        )
    }
}

val valueStoreModule = module {
    factory<ValueStore> { ValueStoreImpl() }
}

val textActionSetModule = module {
    viewModel { (data: TextActionParameter) -> TextActionViewModel(data) }
}

val numberActionSetModule = module {
    viewModel { (data: NumberActionParams) -> NumberActionViewModel(data) }
}

val referralsModule = module {
    viewModel<ReferralsViewModel> {
        ReferralsViewModelImpl(
            get()
        )
    }
    viewModel<ReferralsActivatedViewModel> { ReferralsActivatedViewModelImpl(get()) }
    viewModel<ReferralsEditCodeViewModel> { ReferralsEditCodeViewModelImpl(get()) }
}

val homeModule = module {
    viewModel<HomeViewModel> { HomeViewModelImpl(get(), get()) }
}

val connectPaymentModule = module {
    viewModel { ConnectPaymentViewModel(get(), get()) }
}

val trustlyModule = module {
    viewModel<TrustlyViewModel> { TrustlyViewModelImpl(get()) }
}

val changeAddressModule = module {
    viewModel<ChangeAddressViewModel> { ChangeAddressViewModelImpl(get(), get(), get()) }
}

val changeDateBottomSheetModule = module {
    viewModel { (data: ChangeDateBottomSheetData) -> ChangeDateBottomSheetViewModel(get(), get(), data) }
}

val checkoutModule = module {
    viewModel { (ids: List<String>) -> CheckoutViewModel(ids, get(), get(), get()) }
}

val retrievePriceModule = module {
    viewModel { (data: InsuranceProviderParameter) ->
        RetrievePriceViewModel(
            collectionId = data.selectedInsuranceProviderCollectionId,
            insurerName = data.selectedInsuranceProviderName,
            marketManager = get(),
            startDataCollectionUseCase = get()
        )
    }
}

val externalInsuranceModule = module {
    viewModel { ExternalInsurerViewModel(get()) }
}

val insurelyAuthModule = module {
    viewModel { (reference: String) -> InsurelyAuthViewModel(reference, get()) }
}

val serviceModule = module {
    single { FileService(get()) }
    single<LoginStatusService> { SharedPreferencesLoginStatusService(get(), get(), get()) }
    single<AuthenticationTokenService> { SharedPreferencesAuthenticationTokenService(get()) }

    single { TabNotificationService(get()) }
    single { CrossSellNotificationBadgeService(get(), get()) }
    single { NotificationBadgeService(get()) }

    single { DeviceInformationService(get()) }
}

val repositoriesModule = module {
    single { ChatRepository(get(), get(), get()) }
    single { PayinStatusRepository(get()) }
    single { ClaimsRepository(get(), get()) }
    single { ProfileRepository(get()) }
    single { RedeemReferralCodeRepository(get(), get()) }
    single { UserRepository(get()) }
    single { WhatsNewRepository(get(), get(), get()) }
    single { WelcomeRepository(get(), get()) }
    single { OfferRepository(get(), get()) }
    single { LanguageRepository(get(), get(), get(), get()) }
    single { KeyGearItemsRepository(get(), get(), get(), get()) }
    single { MarketRepository(get(), get(), get()) }
    single { MarketingRepository(get(), get()) }
    single { AdyenRepository(get(), get()) }
    single { EmbarkRepository(get(), get(), get(), get(), get()) }
    single { ReferralsRepository(get()) }
    single { LoggedInRepository(get(), get()) }
    single { GetHomeUseCase(get(), get()) }
    single { TrustlyRepository(get()) }
    single { MemberIdRepository(get()) }
    single { PaymentRepository(get(), get()) }
    single { ChoosePlanRepository(get(), get()) }
}

val trackerModule = module {
    single { ClaimsTracker(get()) }
    single { ProfileTracker(get()) }
    single { WhatsNewTracker(get()) }
    single { ReferralsTracker(get()) }
    single { TerminatedTracker(get()) }
    single { WelcomeTracker(get()) }
    single { OfferTracker(get()) }
    single { ChatTracker(get()) }
    single { TrustlyTracker(get()) }
    single { PaymentTracker(get()) }
    single { RatingsTracker(get()) }
    single { LoggedInTracker(get()) }
    single { KeyGearTracker(get()) }
    single { InsuranceTracker(get()) }
    single { MarketingTracker(get()) }
    single { HomeTracker(get()) }
    single { ScreenTracker(get()) }
    single { CrossSellTracker(get()) }
    single {
        // Workaround for https://github.com/InsertKoinIO/koin/issues/1146
        TrackingFacade(getAll<TrackerSink>().distinct())
    }
    single { MixpanelTracker(get()) } bind TrackerSink::class
    single { FirebaseTracker(get()) } bind TrackerSink::class
}

val localeBroadcastManagerModule = module {
    single<LocaleBroadcastManager> { LocaleBroadcastManagerImpl(get()) }
}

val marketPickerTrackerModule = module {
    single { MarketPickerTracker(get()) }
}

val marketManagerModule = module {
    single<MarketManager> { MarketManagerImpl(get()) }
}

val notificationModule = module {
    single { PaymentNotificationManager(get()) }
    single { CrossSellNotificationManager(get(), get()) }
}

val clockModule = module { single { Clock.systemDefaultZone() } }

val embarkTrackerModule = module {
    single { EmbarkTracker(get()) }
}

val localeManagerModule = module {
    single { LocaleManager(get(), get()) }
}

val useCaseModule = module {
    single { GetUpcomingAgreementUseCase(get(), get()) }
    single { GetAddressChangeStoryIdUseCase(get()) }
    single { StartDanishAuthUseCase(get()) }
    single { StartNorwegianAuthUseCase(get()) }
    single { SubscribeToAuthStatusUseCase(get()) }
    single { GetQuotesUseCase(get()) }
    single { GetQuoteUseCase(get()) }
    single { EditStartDateUseCase(get(), get()) }
    single { SignQuotesUseCase(get(), get(), get()) }
    single { ApproveQuotesUseCase(get(), get(), get(), get()) }
    single { RefreshQuotesUseCase(get()) }
    single { LogoutUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { GetContractsUseCase(get(), get()) }
    single { ManuallyRecheckSwedishBankIdSignStatusUseCase(get()) }
    single { SubscribeToSwedishBankIdSignStatusUseCase(get()) }
    single { GetPostSignDependenciesUseCase(get()) }
    single { GetCrossSellsContractTypesUseCase(get(), get()) }
    single { GraphQLQueryUseCase(get()) }
    single { GetCrossSellsUseCase(get(), get()) }
    single { StartDataCollectionUseCase(get(), get()) }
    single { GetInsuranceProvidersUseCase(get(), get()) }
    single { CreateOtpAttemptUseCase(get()) }
    single<SendOtpCodeUseCase> { SendOtpCodeUseCaseImpl(get()) }
    single<ReSendOtpCodeUseCase> { ReSendOtpCodeUseCaseImpl(get()) }
    single { TriggerFreeTextChatUseCase(get()) }
    single { GetDataCollectionUseCase(get(), get()) }
}

val cacheManagerModule = module {
    single { CacheManager(get()) }
}

val pushTokenManagerModule = module {
    single { PushTokenManager(FirebaseMessaging.getInstance()) }
}

val sharedPreferencesModule = module {
    single<SharedPreferences> { get<Context>().getSharedPreferences("hedvig_shared_preference", MODE_PRIVATE) }
}

val featureManagerModule = module {
    single { FeatureManager(get()) }
}

val coilModule = module {
    single {
        ImageLoader.Builder(get())
            .componentRegistry {
                add(SvgDecoder(get()))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder(get()))
                } else {
                    add(GifDecoder())
                }
            }
            .build()
    }
}

val chatEventModule = module {
    single<ChatEventStore> { ChatEventDataStore(get()) }
}

val dataStoreModule = module {
    @Suppress("RemoveExplicitTypeArguments")
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = {
                get<Context>().preferencesDataStoreFile("hedvig_data_store_preferences")
            }
        )
    }
}
