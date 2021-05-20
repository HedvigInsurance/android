package com.hedvig.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.google.firebase.messaging.FirebaseMessaging
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.authenticate.SharedPreferencesAuthenticationTokenService
import com.hedvig.app.authenticate.SharedPreferencesLoginStatusService
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
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.feature.chat.viewmodel.UserViewModel
import com.hedvig.app.feature.claims.data.ClaimsRepository
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.connectpayin.ConnectPaymentViewModel
import com.hedvig.app.feature.embark.EmbarkRepository
import com.hedvig.app.feature.embark.EmbarkTracker
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.EmbarkViewModelImpl
import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.feature.embark.ValueStoreImpl
import com.hedvig.app.feature.embark.passages.datepicker.DatePickerViewModel
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionItem
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionParams
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionViewModel
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentViewModel
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionParams
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionViewModel
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerViewModel
import com.hedvig.app.feature.embark.passages.textaction.TextActionParameter
import com.hedvig.app.feature.embark.passages.textaction.TextActionViewModel
import com.hedvig.app.feature.home.data.HomeRepository
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
import com.hedvig.app.feature.loggedin.service.GetCrossSellsUseCase
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import com.hedvig.app.feature.loggedin.ui.LoggedInRepository
import com.hedvig.app.feature.loggedin.ui.LoggedInTracker
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
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.app.service.push.managers.PaymentNotificationManager
import com.hedvig.app.terminated.TerminatedTracker
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.featureflags.FeatureManager
import com.mixpanel.android.mpmetrics.MixpanelAPI
import java.time.Clock
import java.util.Locale
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

fun isDebug() = BuildConfig.DEBUG || BuildConfig.APPLICATION_ID == "com.hedvig.test.app"

fun shouldOverrideFeatureFlags(): Boolean {
    if (false) {
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

val whatsNewModule = module {
    viewModel<WhatsNewViewModel> { WhatsNewViewModelImpl(get()) }
}

val insuranceModule = module {
    viewModel<InsuranceViewModel> { InsuranceViewModelImpl(get()) }
    viewModel<ContractDetailViewModel> { ContractDetailViewModelImpl(get(), get(), get(), get()) }
}

val marketingModule = module {
    viewModel<MarketingViewModel> { MarketingViewModelImpl(get(), get()) }
}

val offerModule = module {
    viewModel<OfferViewModel> { (ids: List<String>, shouldShowOnNextAppStart: Boolean) ->
        OfferViewModelImpl(ids, get(), get(), get(), get(), get(), get(), get(), shouldShowOnNextAppStart, get())
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
    viewModel<EmbarkViewModel> { (storyName: String) -> EmbarkViewModelImpl(get(), get(), get(), get(), storyName) }
}

val valueStoreModule = module {
    factory<ValueStore> { ValueStoreImpl() }
}

val previousInsViewModel = module {
    viewModel { PreviousInsurerViewModel() }
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
    viewModel<ChangeAddressViewModel> { ChangeAddressViewModelImpl(get(), get()) }
}

val changeDateBottomSheetModule = module {
    viewModel { (data: ChangeDateBottomSheetData) -> ChangeDateBottomSheetViewModel(get(), get(), data) }
}

val checkoutModule = module {
    viewModel { (ids: List<String>) -> CheckoutViewModel(ids, get(), get(), get()) }
}

val serviceModule = module {
    single { FileService(get()) }
    single<LoginStatusService> { SharedPreferencesLoginStatusService(get(), get(), get()) }
    single<AuthenticationTokenService> { SharedPreferencesAuthenticationTokenService(get()) }
    single { TabNotificationService(get(), get()) }
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
    single { EmbarkRepository(get(), get(), get(), get()) }
    single { ReferralsRepository(get()) }
    single { LoggedInRepository(get(), get()) }
    single { HomeRepository(get(), get()) }
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
    single { EmbarkTracker(get()) }
    single {
        // Workaround for https://github.com/InsertKoinIO/koin/issues/1146
        TrackingFacade(getAll<TrackerSink>().distinct())
    }
    single { MixpanelTracker(get()) } bind TrackerSink::class
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
    single { GetCrossSellsUseCase(get()) }
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

val featureRuntimeBehaviorModule = module {
    single { FeatureManager(get(), isDebug()) }
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
    single {
        PreferenceDataStoreFactory.create(
            produceFile = {
                get<Context>().preferencesDataStoreFile("hedvig_data_store_preferences")
            }
        )
    }
}
