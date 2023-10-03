package com.hedvig.android.feature.home.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.home.claimdetail.data.GetClaimDetailUiStateFlowUseCase
import com.hedvig.android.feature.home.claimdetail.data.GetClaimDetailUseCase
import com.hedvig.android.feature.home.claimdetail.ui.ClaimDetailViewModel
import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimViewModel
import com.hedvig.android.feature.home.claims.pledge.HonestyPledgeViewModel
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseDemo
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseImpl
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.feature.home.legacychangeaddress.CreateQuoteCartUseCase
import com.hedvig.android.feature.home.legacychangeaddress.GetAddressChangeStoryIdUseCase
import com.hedvig.android.feature.home.legacychangeaddress.GetUpcomingAgreementUseCase
import com.hedvig.android.feature.home.legacychangeaddress.LegacyChangeAddressViewModel
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
  single<CreateQuoteCartUseCase> { CreateQuoteCartUseCase(get<ApolloClient>(giraffeClient), get(), get()) }
  single<GetAddressChangeStoryIdUseCase> {
    GetAddressChangeStoryIdUseCase(get<ApolloClient>(giraffeClient), get(), get())
  }
  single<GetClaimDetailUiStateFlowUseCase> { GetClaimDetailUiStateFlowUseCase(get()) }
  single<GetClaimDetailUseCase> { GetClaimDetailUseCase(get<ApolloClient>(giraffeClient), get()) }
  single<GetHomeDataUseCaseImpl> {
    GetHomeDataUseCaseImpl(
      get<ApolloClient>(giraffeClient),
      get<LanguageService>(),
      get<GetMemberRemindersUseCase>(),
      get<GetTravelCertificateSpecificationsUseCase>(),
    )
  }
  single<GetHomeDataUseCaseDemo> {
    GetHomeDataUseCaseDemo()
  }
  single {
    GetHomeDataUseCaseProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<GetHomeDataUseCaseImpl>(),
      demoImpl = get<GetHomeDataUseCaseDemo>(),
    )
  }
  single<GetUpcomingAgreementUseCase> { GetUpcomingAgreementUseCase(get<ApolloClient>(giraffeClient), get()) }
  viewModel<LegacyChangeAddressViewModel> { LegacyChangeAddressViewModel(get(), get(), get()) }
  viewModel<ClaimDetailViewModel> { (claimId: String) -> ClaimDetailViewModel(claimId, get(), get()) }
  viewModel<CommonClaimViewModel> { CommonClaimViewModel(get()) }
  viewModel<HomeViewModel> {
    HomeViewModel(
      get<GetHomeDataUseCaseProvider>(),
      get<FeatureManager>(),
    )
  }
  viewModel<HonestyPledgeViewModel> { HonestyPledgeViewModel(get()) }
}
