package com.hedvig.android.feature.home.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.feature.home.claimdetail.data.GetClaimDetailUiStateFlowUseCase
import com.hedvig.android.feature.home.claimdetail.data.GetClaimDetailUseCase
import com.hedvig.android.feature.home.claimdetail.ui.ClaimDetailViewModel
import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimViewModel
import com.hedvig.android.feature.home.claims.pledge.HonestyPledgeViewModel
import com.hedvig.android.feature.home.data.GetHomeUseCase
import com.hedvig.android.feature.home.home.HomeItemsBuilder
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.feature.home.legacychangeaddress.CreateQuoteCartUseCase
import com.hedvig.android.feature.home.legacychangeaddress.GetAddressChangeStoryIdUseCase
import com.hedvig.android.feature.home.legacychangeaddress.GetUpcomingAgreementUseCase
import com.hedvig.android.feature.home.legacychangeaddress.LegacyChangeAddressViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
  single<CreateQuoteCartUseCase> { CreateQuoteCartUseCase(get<ApolloClient>(giraffeClient), get(), get()) }
  single<GetAddressChangeStoryIdUseCase> {
    GetAddressChangeStoryIdUseCase(get<ApolloClient>(giraffeClient), get(), get())
  }
  single<GetClaimDetailUiStateFlowUseCase> { GetClaimDetailUiStateFlowUseCase(get()) }
  single<GetClaimDetailUseCase> { GetClaimDetailUseCase(get<ApolloClient>(giraffeClient), get()) }
  single<GetHomeUseCase> { GetHomeUseCase(get<ApolloClient>(giraffeClient), get()) }
  single<GetUpcomingAgreementUseCase> { GetUpcomingAgreementUseCase(get<ApolloClient>(giraffeClient), get()) }
  single<HomeItemsBuilder> { HomeItemsBuilder(get()) }
  viewModel<LegacyChangeAddressViewModel> { LegacyChangeAddressViewModel(get(), get(), get()) }
  viewModel<ClaimDetailViewModel> { (claimId: String) -> ClaimDetailViewModel(claimId, get(), get()) }
  viewModel<CommonClaimViewModel> { CommonClaimViewModel(get()) }
  viewModel<HomeViewModel> { HomeViewModel(get(), get(), get(), get()) }
  viewModel<HonestyPledgeViewModel> { HonestyPledgeViewModel(get()) }
}
