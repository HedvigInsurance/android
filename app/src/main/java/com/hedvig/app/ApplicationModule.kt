@file:Suppress("RemoveExplicitTypeArguments")

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
import com.google.firebase.messaging.FirebaseMessaging
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.authenticate.DeviceIdDataStore
import com.hedvig.app.authenticate.DeviceIdStore
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.authenticate.SharedPreferencesAuthenticationTokenService
import com.hedvig.app.authenticate.SharedPreferencesLoginStatusService
import com.hedvig.app.authenticate.UserViewModel
import com.hedvig.app.authenticate.insurely.GetDataCollectionUseCase
import com.hedvig.app.authenticate.insurely.InsurelyAuthViewModel
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.addressautocompletion.data.GetDanishAddressAutoCompletionUseCase
import com.hedvig.app.feature.addressautocompletion.data.GetFinalDanishAddressSelectionUseCase
import com.hedvig.app.feature.addressautocompletion.ui.AddressAutoCompleteViewModel
import com.hedvig.app.feature.adyen.AdyenRepository
import com.hedvig.app.feature.adyen.ConnectPaymentUseCase
import com.hedvig.app.feature.adyen.ConnectPayoutUseCase
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinViewModel
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinViewModelImpl
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutViewModel
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutViewModelImpl
import com.hedvig.app.feature.chat.data.ChatEventDataStore
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.feature.chat.service.ChatNotificationSender
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.feature.checkout.CheckoutViewModel
import com.hedvig.app.feature.checkout.EditCheckoutUseCase
import com.hedvig.app.feature.claimdetail.data.GetClaimDetailUiStateFlowUseCase
import com.hedvig.app.feature.claimdetail.data.GetClaimDetailUseCase
import com.hedvig.app.feature.claimdetail.ui.ClaimDetailViewModel
import com.hedvig.app.feature.claims.data.ClaimsRepository
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimViewModel
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeViewModel
import com.hedvig.app.feature.connectpayin.ConnectPaymentViewModel
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.ui.detail.CrossSellDetailViewModel
import com.hedvig.app.feature.crossselling.ui.detail.CrossSellFaqViewModel
import com.hedvig.app.feature.crossselling.usecase.GetCrossSellsContractTypesUseCase
import com.hedvig.app.feature.crossselling.usecase.GetCrossSellsUseCase
import com.hedvig.app.feature.embark.EmbarkRepository
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.EmbarkViewModelImpl
import com.hedvig.app.feature.embark.GraphQLQueryUseCase
import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.feature.embark.ValueStoreImpl
import com.hedvig.app.feature.embark.passages.addressautocomplete.EmbarkAddressAutoCompleteViewModel
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderViewModel
import com.hedvig.app.feature.embark.passages.datepicker.DatePickerViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.ExternalInsurerViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.GetInsuranceProvidersUseCase
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.AskForPriceInfoViewModel
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
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.embark.ui.TooltipViewModel
import com.hedvig.app.feature.genericauth.CreateOtpAttemptUseCase
import com.hedvig.app.feature.genericauth.GenericAuthViewModel
import com.hedvig.app.feature.genericauth.otpinput.OtpInputViewModel
import com.hedvig.app.feature.genericauth.otpinput.ReSendOtpCodeUseCase
import com.hedvig.app.feature.genericauth.otpinput.ReSendOtpCodeUseCaseImpl
import com.hedvig.app.feature.genericauth.otpinput.SendOtpCodeUseCase
import com.hedvig.app.feature.genericauth.otpinput.SendOtpCodeUseCaseImpl
import com.hedvig.app.feature.hanalytics.HAnalyticsExperimentManager
import com.hedvig.app.feature.hanalytics.HAnalyticsExperimentManagerImpl
import com.hedvig.app.feature.hanalytics.HAnalyticsImpl
import com.hedvig.app.feature.hanalytics.HAnalyticsService
import com.hedvig.app.feature.hanalytics.HAnalyticsServiceImpl
import com.hedvig.app.feature.hanalytics.HAnalyticsSink
import com.hedvig.app.feature.hanalytics.NetworkHAnalyticsSink
import com.hedvig.app.feature.hanalytics.SendHAnalyticsEventUseCase
import com.hedvig.app.feature.hanalytics.SendHAnalyticsEventUseCaseImpl
import com.hedvig.app.feature.home.data.GetHomeUseCase
import com.hedvig.app.feature.home.model.HomeItemsBuilder
import com.hedvig.app.feature.home.ui.HomeViewModel
import com.hedvig.app.feature.home.ui.HomeViewModelImpl
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressViewModel
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressViewModelImpl
import com.hedvig.app.feature.home.ui.changeaddress.GetAddressChangeStoryIdUseCase
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModelImpl
import com.hedvig.app.feature.insurance.ui.detail.GetContractDetailsUseCase
import com.hedvig.app.feature.insurance.ui.tab.InsuranceViewModel
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsViewModel
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
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModelImpl
import com.hedvig.app.feature.marketing.MarketingViewModel
import com.hedvig.app.feature.marketing.data.GetInitialMarketPickerValuesUseCase
import com.hedvig.app.feature.marketing.data.GetMarketingBackgroundUseCase
import com.hedvig.app.feature.marketing.data.MarketingRepository
import com.hedvig.app.feature.marketing.data.SubmitMarketAndLanguagePreferencesUseCase
import com.hedvig.app.feature.marketing.data.UpdateApplicationLanguageUseCase
import com.hedvig.app.feature.marketpicker.LanguageRepository
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManager
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.offer.OfferViewModelImpl
import com.hedvig.app.feature.offer.model.QuoteCartFragmentToOfferModelMapper
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetViewModel
import com.hedvig.app.feature.offer.ui.changestartdate.QuoteCartEditStartDateUseCase
import com.hedvig.app.feature.offer.usecase.AddPaymentTokenUseCase
import com.hedvig.app.feature.offer.usecase.CreateAccessTokenUseCase
import com.hedvig.app.feature.offer.usecase.EditCampaignUseCase
import com.hedvig.app.feature.offer.usecase.GetExternalInsuranceProviderUseCase
import com.hedvig.app.feature.offer.usecase.ObserveOfferStateUseCase
import com.hedvig.app.feature.offer.usecase.StartCheckoutUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionresult.GetDataCollectionResultUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase
import com.hedvig.app.feature.offer.usecase.providerstatus.GetProviderDisplayNameUseCase
import com.hedvig.app.feature.onboarding.ChoosePlanViewModel
import com.hedvig.app.feature.onboarding.GetBundlesUseCase
import com.hedvig.app.feature.onboarding.GetMemberIdUseCase
import com.hedvig.app.feature.onboarding.MemberIdViewModel
import com.hedvig.app.feature.onboarding.MemberIdViewModelImpl
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.profile.ui.aboutapp.AboutAppViewModel
import com.hedvig.app.feature.profile.ui.charity.CharityViewModel
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoViewModel
import com.hedvig.app.feature.profile.ui.payment.PaymentRepository
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModelImpl
import com.hedvig.app.feature.profile.ui.tab.ProfileQueryDataToProfileUiStateMapper
import com.hedvig.app.feature.referrals.data.RedeemReferralCodeRepository
import com.hedvig.app.feature.referrals.data.ReferralsRepository
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
import com.hedvig.app.feature.tracking.ApplicationLifecycleTracker
import com.hedvig.app.feature.trustly.TrustlyRepository
import com.hedvig.app.feature.trustly.TrustlyViewModel
import com.hedvig.app.feature.trustly.TrustlyViewModelImpl
import com.hedvig.app.feature.welcome.WelcomeRepository
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewViewModelImpl
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationViewModel
import com.hedvig.app.feature.zignsec.usecase.StartDanishAuthUseCase
import com.hedvig.app.feature.zignsec.usecase.StartNorwegianAuthUseCase
import com.hedvig.app.feature.zignsec.usecase.SubscribeToAuthStatusUseCase
import com.hedvig.app.service.FileService
import com.hedvig.app.service.badge.CrossSellNotificationBadgeService
import com.hedvig.app.service.badge.NotificationBadgeService
import com.hedvig.app.service.badge.ReferralsNotificationBadgeService
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.app.service.push.senders.CrossSellNotificationSender
import com.hedvig.app.service.push.senders.GenericNotificationSender
import com.hedvig.app.service.push.senders.NotificationSender
import com.hedvig.app.service.push.senders.PaymentNotificationSender
import com.hedvig.app.service.push.senders.ReferralsNotificationSender
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.ApolloTimberLogger
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.apollo.DeviceIdInterceptor
import com.hedvig.app.util.apollo.GraphQLQueryHandler
import com.hedvig.app.util.apollo.SunsettingInterceptor
import com.hedvig.app.util.featureflags.ClearHAnalyticsExperimentsCacheUseCase
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.FeatureManagerImpl
import com.hedvig.app.util.featureflags.flags.DevFeatureFlagProvider
import com.hedvig.app.util.featureflags.flags.HAnalyticsFeatureFlagProvider
import com.hedvig.app.util.featureflags.loginmethod.DevLoginMethodProvider
import com.hedvig.app.util.featureflags.loginmethod.HAnalyticsLoginMethodProvider
import com.hedvig.app.util.featureflags.paymenttype.DevPaymentTypeProvider
import com.hedvig.app.util.featureflags.paymenttype.HAnalyticsPaymentTypeProvider
import com.hedvig.hanalytics.HAnalytics
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.ParametersHolder
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber
import java.time.Clock
import java.util.Locale
import java.util.concurrent.TimeUnit

fun isDebug() = BuildConfig.DEBUG || BuildConfig.APPLICATION_ID == "com.hedvig.test.app"

val applicationModule = module {
    single { androidApplication() as HedvigApplication }
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
            // Temporary fix until back-end problems are handled
            .readTimeout(30, TimeUnit.SECONDS)
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
            .addInterceptor(DeviceIdInterceptor(get()))
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

fun makeLocaleString(context: Context, market: Market?): String =
    getLocale(context, market).toLanguageTag()

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
    viewModel { ChatViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { (quoteCartId: QuoteCartId?) -> RedeemCodeViewModel(quoteCartId, get(), get()) }
    viewModel { UserViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { WelcomeViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { DatePickerViewModel() }
    viewModel { params -> SimpleSignAuthenticationViewModel(params.get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (data: MultiActionParams) -> MultiActionViewModel(data) }
    viewModel { (componentState: MultiActionItem.Component?, multiActionParams: MultiActionParams) ->
        AddComponentViewModel(
            componentState,
            multiActionParams
        )
    }
    viewModel { TerminatedContractsViewModel(get()) }
    viewModel { (quoteCartId: QuoteCartId) ->
        SwedishBankIdSignViewModel(
            loginStatusService = get(),
            hAnalytics = get(),
            quoteCartId = quoteCartId,
            offerRepository = get(),
            createAccessTokenUseCase = get(),
            featureManager = get(),
        )
    }
    viewModel { AudioRecorderViewModel(get()) }
    viewModel { CrossSellFaqViewModel(get()) }
    viewModel { (crossSell: CrossSellData) ->
        CrossSellDetailViewModel(crossSell, get(), get())
    }
    viewModel { GenericAuthViewModel(get()) }
    viewModel { (otpId: String, credential: String) ->
        OtpInputViewModel(
            otpId,
            credential,
            get(),
            get(),
            get()
        )
    }
    viewModel { parametersHolder: ParametersHolder ->
        EmbarkAddressAutoCompleteViewModel(
            parametersHolder.getOrNull()
        )
    }
    viewModel { parametersHolder ->
        AddressAutoCompleteViewModel(
            parametersHolder.getOrNull(),
            get(),
            get()
        )
    }
    viewModel { (claimId: String) -> ClaimDetailViewModel(claimId, get(), get(), get(), get()) }
    viewModel { HonestyPledgeViewModel(get()) }
    viewModel { (commonClaimId: String) -> CommonClaimViewModel(commonClaimId, get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { TooltipViewModel(get()) }
    viewModel { (collectionId: String) -> AskForPriceInfoViewModel(collectionId, get()) }
    viewModel { CharityViewModel(get()) }
    viewModel { MyInfoViewModel(get()) }
    viewModel { AboutAppViewModel(get()) }
    viewModel { MarketingViewModel(get(), get(), get(), get(), get(), get(), get()) }
}

val choosePlanModule = module {
    viewModel { ChoosePlanViewModel(get(), get(), get()) }
}

val onboardingModule = module {
    viewModel<MemberIdViewModel> { MemberIdViewModelImpl(get()) }
}

val loggedInModule = module {
    viewModel<LoggedInViewModel> { LoggedInViewModelImpl(get(), get(), get(), get(), get()) }
}

val whatsNewModule = module {
    viewModel<WhatsNewViewModel> { WhatsNewViewModelImpl(get()) }
}

val insuranceModule = module {
    viewModel { InsuranceViewModel(get(), get(), get()) }
    viewModel<ContractDetailViewModel> { (contractId: String) ->
        ContractDetailViewModelImpl(contractId, get(), get(), get())
    }
}

val offerModule = module {
    single<OfferRepository> { OfferRepository(get(), get(), get()) }
    viewModel<OfferViewModel> { parametersHolder: ParametersHolder ->
        OfferViewModelImpl(
            quoteCartId = parametersHolder.get(),
            offerRepository = get(),
            loginStatusService = get(),
            startCheckoutUseCase = get(),
            shouldShowOnNextAppStart = parametersHolder.get(),
            chatRepository = get(),
            editCampaignUseCase = get(),
            featureManager = get(),
            addPaymentTokenUseCase = get(),
            getExternalInsuranceProviderUseCase = get(),
            getBundleVariantUseCase = get(),
        )
    }
    single { SubscribeToDataCollectionStatusUseCase(get()) }
    single { GetProviderDisplayNameUseCase(get()) }
    single { GetDataCollectionResultUseCase(get()) }
    single { QuoteCartFragmentToOfferModelMapper(get()) }
}

val profileModule = module {
    single<ProfileQueryDataToProfileUiStateMapper> { ProfileQueryDataToProfileUiStateMapper(get(), get(), get()) }
    single<ProfileRepository> { ProfileRepository(get()) }
    viewModel<ProfileViewModel> { ProfileViewModel(get(), get(), get()) }
}

val keyGearModule = module {
    viewModel<KeyGearViewModel> { KeyGearViewModelImpl(get(), get()) }
    viewModel<KeyGearItemDetailViewModel> { KeyGearItemDetailViewModelImpl(get()) }
    viewModel<CreateKeyGearItemViewModel> { CreateKeyGearItemViewModelImpl(get()) }
    viewModel<KeyGearValuationViewModel> { KeyGearValuationViewModelImpl(get()) }
}

val paymentModule = module {
    viewModel<PaymentViewModel> { PaymentViewModelImpl(get(), get(), get(), get()) }
}

val adyenModule = module {
    viewModel<AdyenConnectPayinViewModel> { AdyenConnectPayinViewModelImpl(get(), get()) }
    viewModel<AdyenConnectPayoutViewModel> { AdyenConnectPayoutViewModelImpl(get()) }
}

val embarkModule = module {
    viewModel<EmbarkViewModel> { (storyName: String) ->
        EmbarkViewModelImpl(
            embarkRepository = get(),
            loginStatusService = get(),
            graphQLQueryUseCase = get(),
            chatRepository = get(),
            valueStore = get(),
            hAnalytics = get(),
            storyName = storyName,
            createQuoteCartUseCase = get(),
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
    viewModel<ReferralsViewModel> { ReferralsViewModelImpl(get()) }
    viewModel<ReferralsActivatedViewModel> { ReferralsActivatedViewModelImpl(get()) }
    viewModel<ReferralsEditCodeViewModel> { ReferralsEditCodeViewModelImpl(get()) }
}

val homeModule = module {
    single<HomeItemsBuilder> { HomeItemsBuilder(get()) }
    viewModel<HomeViewModel> { HomeViewModelImpl(get(), get(), get()) }
}

val connectPaymentModule = module {
    viewModel { ConnectPaymentViewModel(get(), get(), get()) }
}

val trustlyModule = module {
    viewModel<TrustlyViewModel> { TrustlyViewModelImpl(get(), get()) }
}

val changeAddressModule = module {
    viewModel<ChangeAddressViewModel> { ChangeAddressViewModelImpl(get(), get(), get(), get()) }
}

val changeDateBottomSheetModule = module {
    viewModel { (data: ChangeDateBottomSheetData) -> ChangeDateBottomSheetViewModel(get(), data, get()) }
}

val checkoutModule = module {
    viewModel { (ids: List<String>, quoteCartId: QuoteCartId) ->
        CheckoutViewModel(
            quoteIds = ids,
            quoteCartId = quoteCartId,
            signQuotesUseCase = get(),
            editQuotesUseCase = get(),
            createAccessTokenUseCase = get(),
            marketManager = get(),
            loginStatusService = get(),
            hAnalytics = get(),
            offerRepository = get(),
            featureManager = get(),
            bundleVariantUseCase = get(),
        )
    }
}

val retrievePriceModule = module {
    viewModel { (data: InsuranceProviderParameter) ->
        RetrievePriceViewModel(
            collectionId = data.selectedInsuranceProviderCollectionId,
            insurerName = data.selectedInsuranceProviderName,
            marketManager = get(),
            startDataCollectionUseCase = get(),
            hAnalytics = get(),
        )
    }
}

val externalInsuranceModule = module {
    viewModel { ExternalInsurerViewModel(get(), get()) }
}

val insurelyAuthModule = module {
    viewModel { (reference: String, providerId: String) ->
        InsurelyAuthViewModel(
            reference,
            get(),
            providerId,
            get()
        )
    }
}

val serviceModule = module {
    single { FileService(get()) }
    single<LoginStatusService> { SharedPreferencesLoginStatusService(get(), get(), get()) }
    single<AuthenticationTokenService> { SharedPreferencesAuthenticationTokenService(get()) }

    single { TabNotificationService(get(), get()) }
    single { CrossSellNotificationBadgeService(get(), get()) }
    single { ReferralsNotificationBadgeService(get(), get()) }
    single { NotificationBadgeService(get()) }

    single { DeviceInformationService(get()) }
}

val repositoriesModule = module {
    single { ChatRepository(get(), get(), get()) }
    single { PayinStatusRepository(get()) }
    single { ClaimsRepository(get(), get()) }
    single { RedeemReferralCodeRepository(get(), get()) }
    single { UserRepository(get()) }
    single { WhatsNewRepository(get(), get(), get()) }
    single { WelcomeRepository(get(), get()) }
    single { LanguageRepository(get()) }
    single { OfferRepository(get(), get(), get()) }
    single { KeyGearItemsRepository(get(), get(), get(), get()) }
    single { MarketingRepository(get(), get()) }
    single { AdyenRepository(get(), get()) }
    single { EmbarkRepository(get(), get()) }
    single { ReferralsRepository(get()) }
    single { LoggedInRepository(get(), get()) }
    single { GetHomeUseCase(get(), get()) }
    single { TrustlyRepository(get()) }
    single { GetMemberIdUseCase(get()) }
    single { PaymentRepository(get(), get()) }
    single { GetBundlesUseCase(get(), get()) }
}

val trackerModule = module {
    single<HAnalytics> { HAnalyticsImpl(get(), get()) }
    single<SendHAnalyticsEventUseCase> {
        // Workaround for https://github.com/InsertKoinIO/koin/issues/1146
        val allAnalyticsSinks = getAll<HAnalyticsSink>().distinct()
        SendHAnalyticsEventUseCaseImpl(allAnalyticsSinks)
    }
    single<HAnalyticsExperimentManager> { HAnalyticsExperimentManagerImpl(get(), get()) }
    single<NetworkHAnalyticsSink> { NetworkHAnalyticsSink(get()) } bind HAnalyticsSink::class
    single<HAnalyticsService> {
        HAnalyticsServiceImpl(get(), get(), get(), get<Context>().getString(R.string.HANALYTICS_URL))
    }
    single<ApplicationLifecycleTracker> { ApplicationLifecycleTracker(get()) }
    single<ClearHAnalyticsExperimentsCacheUseCase> { ClearHAnalyticsExperimentsCacheUseCase(get()) }
}

val localeBroadcastManagerModule = module {
    single<LocaleBroadcastManager> { LocaleBroadcastManager(get()) }
}

val marketManagerModule = module {
    single<MarketManager> { MarketManagerImpl(get()) }
}

val notificationModule = module {
    single { PaymentNotificationSender(get(), get(), get()) } bind NotificationSender::class
    single { CrossSellNotificationSender(get(), get()) } bind NotificationSender::class
    single { ChatNotificationSender(get()) } bind NotificationSender::class
    single { ReferralsNotificationSender(get()) } bind NotificationSender::class
    single { GenericNotificationSender(get()) } bind NotificationSender::class
}

val clockModule = module { single { Clock.systemDefaultZone() } }

val localeManagerModule = module {
    single { LocaleManager(get(), get()) }
}

val useCaseModule = module {
    single { GetUpcomingAgreementUseCase(get(), get()) }
    single { GetAddressChangeStoryIdUseCase(get(), get(), get()) }
    single { StartDanishAuthUseCase(get()) }
    single { StartNorwegianAuthUseCase(get()) }
    single { SubscribeToAuthStatusUseCase(get()) }
    single { StartCheckoutUseCase(get(), get()) }
    single { LogoutUseCase(get(), get(), get(), get(), get(), get(), get()) }
    single { GetContractsUseCase(get(), get()) }
    single { GetCrossSellsContractTypesUseCase(get(), get()) }
    single { GraphQLQueryUseCase(get()) }
    single { GetCrossSellsUseCase(get(), get()) }
    single { StartDataCollectionUseCase(get(), get()) }
    single { GetInsuranceProvidersUseCase(get(), get()) }
    single { CreateOtpAttemptUseCase(get()) }
    single<SendOtpCodeUseCase> { SendOtpCodeUseCaseImpl(get()) }
    single<ReSendOtpCodeUseCase> { ReSendOtpCodeUseCaseImpl(get()) }
    single { GetDataCollectionUseCase(get(), get()) }
    single { GetClaimDetailUseCase(get(), get()) }
    single { GetClaimDetailUiStateFlowUseCase(get()) }
    single { GetContractDetailsUseCase(get(), get()) }
    single<GetDanishAddressAutoCompletionUseCase> { GetDanishAddressAutoCompletionUseCase(get()) }
    single<GetFinalDanishAddressSelectionUseCase> { GetFinalDanishAddressSelectionUseCase(get()) }
    single { CreateQuoteCartUseCase(get(), get(), get()) }
    single { SubmitMarketAndLanguagePreferencesUseCase(get(), get(), get(), get()) }
    single { GetMarketingBackgroundUseCase(get(), get()) }
    single { UpdateApplicationLanguageUseCase(get(), get(), get()) }
    single { GetInitialMarketPickerValuesUseCase(get(), get(), get(), get()) }
    single<EditCheckoutUseCase> { EditCheckoutUseCase(get(), get()) }
    single<QuoteCartEditStartDateUseCase> { QuoteCartEditStartDateUseCase(get(), get()) }
    single<CreateAccessTokenUseCase> { CreateAccessTokenUseCase(get(), get()) }
    single<EditCampaignUseCase> { EditCampaignUseCase(get(), get()) }
    single<AddPaymentTokenUseCase> { AddPaymentTokenUseCase(get()) }
    single<ConnectPaymentUseCase> { ConnectPaymentUseCase(get(), get(), get()) }
    single<ConnectPayoutUseCase> { ConnectPayoutUseCase(get(), get()) }
    single<GetExternalInsuranceProviderUseCase> { GetExternalInsuranceProviderUseCase(get(), get(), get()) }
    single<ObserveOfferStateUseCase> { ObserveOfferStateUseCase(get()) }
}

val cacheManagerModule = module {
    single { CacheManager(get()) }
}

val pushTokenManagerModule = module {
    single { PushTokenManager(FirebaseMessaging.getInstance()) }
}

val sharedPreferencesModule = module {
    single<SharedPreferences> {
        get<Context>().getSharedPreferences(
            "hedvig_shared_preference",
            MODE_PRIVATE
        )
    }
}

val featureManagerModule = module {
    single<FeatureManager> {
        if (BuildConfig.DEBUG) {
            FeatureManagerImpl(
                DevFeatureFlagProvider(get()),
                DevLoginMethodProvider(get()),
                DevPaymentTypeProvider(get()),
                get(),
            )
        } else {
            FeatureManagerImpl(
                HAnalyticsFeatureFlagProvider(get()),
                HAnalyticsLoginMethodProvider(get()),
                HAnalyticsPaymentTypeProvider(get()),
                get(),
            )
        }
    }
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
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = {
                get<Context>().preferencesDataStoreFile("hedvig_data_store_preferences")
            }
        )
    }
}

val deviceIdStoreModule = module {
    single<DeviceIdStore> { DeviceIdDataStore(get()) }
}

val graphQLQueryModule = module {
    single<GraphQLQueryHandler> { GraphQLQueryHandler(get(), get(), get()) }
}
