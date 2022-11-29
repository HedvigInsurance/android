@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
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
import com.datadog.android.DatadogInterceptor
import com.google.firebase.messaging.FirebaseMessaging
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.interceptor.AccessTokenAuthenticator
import com.hedvig.android.auth.interceptor.ExistingAuthTokenAppendingInterceptor
import com.hedvig.android.core.common.di.LogInfoType
import com.hedvig.android.core.common.di.datastoreFileQualifier
import com.hedvig.android.core.common.di.isDebugQualifier
import com.hedvig.android.core.common.di.logInfoQualifier
import com.hedvig.android.hanalytics.android.di.appIdQualifier
import com.hedvig.android.hanalytics.android.di.appVersionCodeQualifier
import com.hedvig.android.hanalytics.android.di.appVersionNameQualifier
import com.hedvig.android.hanalytics.android.di.hAnalyticsUrlQualifier
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.android.navigation.Navigator
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.authenticate.UserViewModel
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
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionItem
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionParams
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionViewModel
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentViewModel
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionParams
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionViewModel
import com.hedvig.app.feature.embark.passages.textaction.TextActionParameter
import com.hedvig.app.feature.embark.passages.textaction.TextActionViewModel
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.embark.ui.GetMemberIdUseCase
import com.hedvig.app.feature.embark.ui.MemberIdViewModel
import com.hedvig.app.feature.embark.ui.MemberIdViewModelImpl
import com.hedvig.app.feature.embark.ui.TooltipViewModel
import com.hedvig.app.feature.genericauth.CreateOtpAttemptUseCase
import com.hedvig.app.feature.genericauth.CreateOtpAttemptUseCaseImpl
import com.hedvig.app.feature.genericauth.GenericAuthViewModel
import com.hedvig.app.feature.genericauth.otpinput.OtpInputViewModel
import com.hedvig.app.feature.genericauth.otpinput.ReSendOtpCodeUseCase
import com.hedvig.app.feature.genericauth.otpinput.ReSendOtpCodeUseCaseImpl
import com.hedvig.app.feature.genericauth.otpinput.SendOtpCodeUseCase
import com.hedvig.app.feature.genericauth.otpinput.SendOtpCodeUseCaseImpl
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
import com.hedvig.app.feature.loggedin.ui.LoggedInRepository
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModelImpl
import com.hedvig.app.feature.marketing.MarketingViewModel
import com.hedvig.app.feature.marketing.data.GetInitialMarketPickerValuesUseCase
import com.hedvig.app.feature.marketing.data.GetMarketingBackgroundUseCase
import com.hedvig.app.feature.marketing.data.UpdateApplicationLanguageUseCase
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
import com.hedvig.app.feature.marketpicker.LanguageRepository
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.offer.OfferViewModelImpl
import com.hedvig.app.feature.offer.SelectedVariantStore
import com.hedvig.app.feature.offer.model.QuoteCartFragmentToOfferModelMapper
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetViewModel
import com.hedvig.app.feature.offer.ui.changestartdate.QuoteCartEditStartDateUseCase
import com.hedvig.app.feature.offer.usecase.AddPaymentTokenUseCase
import com.hedvig.app.feature.offer.usecase.CreateAccessTokenUseCase
import com.hedvig.app.feature.offer.usecase.CreateAccessTokenUseCaseImpl
import com.hedvig.app.feature.offer.usecase.EditCampaignUseCase
import com.hedvig.app.feature.offer.usecase.GetQuoteCartCheckoutUseCase
import com.hedvig.app.feature.offer.usecase.ObserveOfferStateUseCase
import com.hedvig.app.feature.offer.usecase.ObserveQuoteCartCheckoutUseCase
import com.hedvig.app.feature.offer.usecase.ObserveQuoteCartCheckoutUseCaseImpl
import com.hedvig.app.feature.offer.usecase.StartCheckoutUseCase
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.profile.ui.aboutapp.AboutAppViewModel
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
import com.hedvig.app.feature.settings.ChangeLanguageUseCase
import com.hedvig.app.feature.settings.SettingsViewModel
import com.hedvig.app.feature.swedishbankid.sign.SwedishBankIdSignViewModel
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
import com.hedvig.app.feature.zignsec.usecase.SubscribeToAuthResultUseCase
import com.hedvig.app.service.FileService
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.app.service.push.senders.CrossSellNotificationSender
import com.hedvig.app.service.push.senders.GenericNotificationSender
import com.hedvig.app.service.push.senders.NotificationSender
import com.hedvig.app.service.push.senders.PaymentNotificationSender
import com.hedvig.app.service.push.senders.ReferralsNotificationSender
import com.hedvig.app.util.apollo.DeviceIdInterceptor
import com.hedvig.app.util.apollo.GraphQLQueryHandler
import com.hedvig.app.util.apollo.NetworkCacheManager
import com.hedvig.app.util.apollo.ReopenSubscriptionException
import com.hedvig.app.util.apollo.SunsettingInterceptor
import com.hedvig.app.util.extensions.startChat
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.ParametersHolder
import org.koin.dsl.bind
import org.koin.dsl.module
import slimber.log.i
import timber.log.Timber
import java.io.File
import java.time.Clock
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.pow

fun isDebug() = BuildConfig.DEBUG || BuildConfig.APPLICATION_ID == "com.hedvig.test.app"

val applicationModule = module {
  single { androidApplication() as HedvigApplication }
  single<NormalizedCacheFactory> {
    MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
  }
  single<OkHttpClient> {
    val languageService = get<LanguageService>()
    val builder = OkHttpClient.Builder()
      // Temporary fix until back-end problems are handled
      .readTimeout(30, TimeUnit.SECONDS)
      .addInterceptor(DatadogInterceptor())
      .addInterceptor(get<ExistingAuthTokenAppendingInterceptor>())
      .addInterceptor { chain ->
        chain.proceed(
          chain
            .request()
            .newBuilder()
            .header("User-Agent", makeUserAgent(languageService.getLocale()))
            .header("Accept-Language", languageService.getLocale().toLanguageTag())
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
      .authenticator(AccessTokenAuthenticator(get()))
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
  single<SunsettingInterceptor> { SunsettingInterceptor(get()) } bind ApolloInterceptor::class
  single<ApolloClient.Builder> {
    val interceptors = getAll<ApolloInterceptor>().distinct()
    ApolloClient.Builder()
      .httpServerUrl(get<HedvigApplication>().graphqlUrl)
      .webSocketServerUrl(get<HedvigApplication>().graphqlSubscriptionUrl)
      .okHttpClient(get<OkHttpClient>())
      .webSocketReopenWhen { throwable, reconnectAttempt ->
        if (throwable is ReopenSubscriptionException) {
          return@webSocketReopenWhen true
        }
        if (reconnectAttempt < 3) {
          delay(2.0.pow(reconnectAttempt.toDouble()).toLong()) // Retry after 1 - 2 - 4 seconds
          return@webSocketReopenWhen true
        }
        false
      }
      .wsProtocol(
        SubscriptionWsProtocol.Factory(
          connectionPayload = {
            mapOf("Authorization" to get<AuthTokenService>().getToken()?.token)
          },
        ),
      )
      .normalizedCache(get<NormalizedCacheFactory>())
      .addInterceptors(interceptors)
  }
}

val apolloClientModule = module {
  single<ApolloClient> {
    val builder: ApolloClient.Builder = get()
    builder.build()
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

val viewModelModule = module {
  viewModel { ClaimsViewModel(get(), get()) }
  viewModel { ChatViewModel(get(), get(), get()) }
  viewModel { (quoteCartId: QuoteCartId?) -> RedeemCodeViewModel(quoteCartId, get(), get()) }
  viewModel { UserViewModel(get(), get(), get(), get(), get(), get(), get()) }
  viewModel { WelcomeViewModel(get()) }
  viewModel {
    SettingsViewModel(
      hAnalytics = get(),
      changeLanguageUseCase = get(),
    )
  }
  viewModel { DatePickerViewModel() }
  viewModel { params ->
    SimpleSignAuthenticationViewModel(params.get(), get(), get(), get(), get(), get(), get())
  }
  viewModel { (data: MultiActionParams) -> MultiActionViewModel(data) }
  viewModel { (componentState: MultiActionItem.Component?, multiActionParams: MultiActionParams) ->
    AddComponentViewModel(
      componentState,
      multiActionParams,
    )
  }
  viewModel { TerminatedContractsViewModel(get()) }
  viewModel { (quoteCartId: QuoteCartId) ->
    SwedishBankIdSignViewModel(quoteCartId, get(), get(), get(), get())
  }
  viewModel { AudioRecorderViewModel(get()) }
  viewModel { (crossSell: CrossSellData) ->
    CrossSellFaqViewModel(crossSell, get(), get())
  }
  viewModel { (crossSell: CrossSellData) ->
    CrossSellDetailViewModel(crossSell.action, get(), get())
  }
  viewModel { GenericAuthViewModel(get(), get()) }
  viewModel<OtpInputViewModel> { (verifyUrl: String, resendUrl: String, credential: String) ->
    OtpInputViewModel(
      verifyUrl,
      resendUrl,
      credential,
      get(),
      get(),
      get(),
    )
  }
  viewModel { parametersHolder: ParametersHolder ->
    EmbarkAddressAutoCompleteViewModel(
      parametersHolder.getOrNull(),
    )
  }
  viewModel { parametersHolder ->
    AddressAutoCompleteViewModel(
      parametersHolder.getOrNull(),
      get(),
      get(),
    )
  }
  viewModel { (claimId: String) -> ClaimDetailViewModel(claimId, get(), get(), get()) }
  viewModel { HonestyPledgeViewModel(get()) }
  viewModel { CommonClaimViewModel(get()) }
  viewModel { TooltipViewModel(get()) }
  viewModel { MyInfoViewModel(get()) }
  viewModel { AboutAppViewModel(get()) }
  viewModel { MarketingViewModel(get<MarketManager>().market, get(), get(), get(), get(), get()) }
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
  viewModel { InsuranceViewModel(get(), get(), get(), get()) }
  viewModel<ContractDetailViewModel> { (contractId: String) ->
    ContractDetailViewModelImpl(contractId, get(), get(), get())
  }
}

val offerModule = module {
  single<OfferRepository> { OfferRepository(get(), get(), get(), get()) }
  viewModel<OfferViewModel> { parametersHolder: ParametersHolder ->
    OfferViewModelImpl(
      quoteCartId = parametersHolder.get(),
      selectedContractTypes = parametersHolder.get(),
      offerRepository = get(),
      loginStatusService = get(),
      startCheckoutUseCase = get(),
      chatRepository = get(),
      editCampaignUseCase = get(),
      featureManager = get(),
      addPaymentTokenUseCase = get(),
      getBundleVariantUseCase = get(),
      selectedVariantStore = get(),
      getQuoteCartCheckoutUseCase = get(),
    )
  }
  single { QuoteCartFragmentToOfferModelMapper(get()) }
  single<GetQuoteCartCheckoutUseCase> { GetQuoteCartCheckoutUseCase(get()) }
  single<ObserveQuoteCartCheckoutUseCase> { ObserveQuoteCartCheckoutUseCaseImpl(get()) }
  single<SelectedVariantStore> { SelectedVariantStore() }
}

val profileModule = module {
  single<ProfileQueryDataToProfileUiStateMapper> { ProfileQueryDataToProfileUiStateMapper(get(), get(), get()) }
  single<ProfileRepository> { ProfileRepository(get()) }
  viewModel<ProfileViewModel> { ProfileViewModel(get(), get(), get()) }
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
    )
  }
}

val valueStoreModule = module {
  factory<ValueStore> { ValueStoreImpl() }
}

val textActionSetModule = module {
  viewModel { (data: TextActionParameter) -> TextActionViewModel(data) }
}

val navigatorModule = module {
  single<Navigator> { Navigator(navigateToChat = { startChat() }) }
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

val stringConstantsModule = module {
  single<String>(hAnalyticsUrlQualifier) { get<Context>().getString(R.string.HANALYTICS_URL) }
  single<String>(appVersionNameQualifier) { BuildConfig.VERSION_NAME }
  single<String>(appVersionCodeQualifier) { BuildConfig.VERSION_CODE.toString() }
  single<String>(appIdQualifier) { BuildConfig.APPLICATION_ID }
  single<Boolean>(isDebugQualifier) { BuildConfig.DEBUG }
}

val checkoutModule = module {
  viewModel { (selectedVariantId: String, quoteCartId: QuoteCartId) ->
    CheckoutViewModel(
      selectedVariantId = selectedVariantId,
      quoteCartId = quoteCartId,
      signQuotesUseCase = get(),
      editQuotesUseCase = get(),
      createAccessTokenUseCase = get(),
      marketManager = get(),
      loginStatusService = get(),
      offerRepository = get(),
      featureManager = get(),
      bundleVariantUseCase = get(),
      selectedVariantStore = get(),
    )
  }
}

val externalInsuranceModule = module {
  viewModel { ExternalInsurerViewModel(get(), get()) }
}

val serviceModule = module {
  single<FileService> { FileService(get()) }
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
  single { AdyenRepository(get(), get()) }
  single { EmbarkRepository(get(), get()) }
  single { ReferralsRepository(get()) }
  single { LoggedInRepository(get(), get()) }
  single { GetHomeUseCase(get(), get()) }
  single { TrustlyRepository(get()) }
  single { GetMemberIdUseCase(get()) }
  single { PaymentRepository(get(), get()) }
}

val notificationModule = module {
  single { PaymentNotificationSender(get(), get(), get(), get()) } bind NotificationSender::class
  single { CrossSellNotificationSender(get(), get(), get()) } bind NotificationSender::class
  single { ChatNotificationSender(get()) } bind NotificationSender::class
  single { ReferralsNotificationSender(get()) } bind NotificationSender::class
  single { GenericNotificationSender(get()) } bind NotificationSender::class
}

val clockModule = module { single { Clock.systemDefaultZone() } }

val useCaseModule = module {
  single { GetUpcomingAgreementUseCase(get(), get()) }
  single { GetAddressChangeStoryIdUseCase(get(), get(), get()) }
  single { StartDanishAuthUseCase(get()) }
  single { StartNorwegianAuthUseCase(get()) }
  single { SubscribeToAuthResultUseCase(get()) }
  single { StartCheckoutUseCase(get(), get(), get()) }
  single { LogoutUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  single { GetContractsUseCase(get(), get()) }
  single { GraphQLQueryUseCase(get()) }
  single { GetCrossSellsUseCase(get(), get()) }
  single { GetInsuranceProvidersUseCase(get(), get()) }
  single<CreateOtpAttemptUseCase> { CreateOtpAttemptUseCaseImpl(get()) }
  single<SendOtpCodeUseCase> { SendOtpCodeUseCaseImpl(get()) }
  single<ReSendOtpCodeUseCase> { ReSendOtpCodeUseCaseImpl(get()) }
  single { GetClaimDetailUseCase(get(), get()) }
  single { GetClaimDetailUiStateFlowUseCase(get()) }
  single { GetContractDetailsUseCase(get(), get()) }
  single<GetDanishAddressAutoCompletionUseCase> { GetDanishAddressAutoCompletionUseCase(get()) }
  single<GetFinalDanishAddressSelectionUseCase> { GetFinalDanishAddressSelectionUseCase(get()) }
  single { CreateQuoteCartUseCase(get(), get(), get()) }
  single {
    UploadMarketAndLanguagePreferencesUseCase(
      apolloClient = get(),
      languageService = get(),
    )
  }
  single { GetMarketingBackgroundUseCase(get(), get()) }
  single {
    UpdateApplicationLanguageUseCase(
      marketManager = get(),
      languageService = get(),
    )
  }
  single { GetInitialMarketPickerValuesUseCase(get(), get(), get(), get()) }
  single<EditCheckoutUseCase> {
    EditCheckoutUseCase(
      languageService = get(),
      graphQLQueryHandler = get(),
    )
  }
  single<QuoteCartEditStartDateUseCase> { QuoteCartEditStartDateUseCase(get(), get()) }
  single<CreateAccessTokenUseCase> { CreateAccessTokenUseCaseImpl(get(), get()) }
  single<EditCampaignUseCase> { EditCampaignUseCase(get(), get()) }
  single<AddPaymentTokenUseCase> { AddPaymentTokenUseCase(get()) }
  single<ConnectPaymentUseCase> { ConnectPaymentUseCase(get(), get(), get()) }
  single<ConnectPayoutUseCase> { ConnectPayoutUseCase(get(), get()) }
  single<ObserveOfferStateUseCase> { ObserveOfferStateUseCase(get(), get()) }
  single<ChangeLanguageUseCase> {
    ChangeLanguageUseCase(
      apolloClient = get(),
      languageService = get(),
      cacheManager = get(),
    )
  }
}

val cacheManagerModule = module {
  single { NetworkCacheManager(get()) }
}

val pushTokenManagerModule = module {
  single { PushTokenManager(FirebaseMessaging.getInstance()) }
}

val sharedPreferencesModule = module {
  single<SharedPreferences> {
    get<Context>().getSharedPreferences(
      "hedvig_shared_preference",
      MODE_PRIVATE,
    )
  }
}

val datastoreAndroidModule = module {
  single<File>(datastoreFileQualifier) {
    // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore/src/main/java/androidx/datastore/DataStoreFile.kt;l=35-36
    get<Context>().applicationContext.filesDir
  }
}

val logModule = module {
  single<LogInfoType>(logInfoQualifier) {
    ::i
  }
}

val coilModule = module {
  single<ImageLoader> {
    ImageLoader.Builder(get())
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

val chatEventModule = module {
  single<ChatEventStore> { ChatEventDataStore(get()) }
}

val graphQLQueryModule = module {
  single<GraphQLQueryHandler> { GraphQLQueryHandler(get(), get(), get()) }
}
