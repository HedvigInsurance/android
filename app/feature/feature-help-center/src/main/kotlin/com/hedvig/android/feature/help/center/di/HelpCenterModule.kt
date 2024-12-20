package com.hedvig.android.feature.help.center.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.help.center.HelpCenterViewModel
import com.hedvig.android.feature.help.center.ShowNavigateToInboxViewModel
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredViewModel
import com.hedvig.android.feature.help.center.data.GetInsuranceForEditCoInsuredUseCase
import com.hedvig.android.feature.help.center.data.GetInsuranceForEditCoInsuredUseCaseImpl
import com.hedvig.android.feature.help.center.data.GetMemberActionsUseCase
import com.hedvig.android.feature.help.center.data.GetMemberActionsUseCaseImpl
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.market.MarketManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val helpCenterModule = module {

  single<GetQuickLinksUseCase> {
    GetQuickLinksUseCase(
      apolloClient = get(),
      featureManager = get(),
      getMemberActionsUseCase = get<GetMemberActionsUseCase>(),
    )
  }

  single<GetMemberActionsUseCase> {
    GetMemberActionsUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
      marketManager = get<MarketManager>(),
    )
  }
  viewModel<HelpCenterViewModel> {
    HelpCenterViewModel(
      getQuickLinksUseCase = get<GetQuickLinksUseCase>(),
      hasAnyActiveConversationUseCase = get<HasAnyActiveConversationUseCase>(),
    )
  }

  viewModel<ChooseInsuranceForEditCoInsuredViewModel> {
    ChooseInsuranceForEditCoInsuredViewModel(
      getInsuranceForEditCoInsuredUseCase = get<GetInsuranceForEditCoInsuredUseCase>(),
    )
  }

  single<GetInsuranceForEditCoInsuredUseCase> {
    GetInsuranceForEditCoInsuredUseCaseImpl(
      apolloClient = get(),
      featureManager = get(),
    )
  }

  viewModel<ShowNavigateToInboxViewModel> {
    ShowNavigateToInboxViewModel(
      hasAnyActiveConversationUseCase = get<HasAnyActiveConversationUseCase>(),
    )
  }
}
