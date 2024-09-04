package com.hedvig.android.feature.help.center.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.help.center.HelpCenterViewModel
import com.hedvig.android.feature.help.center.data.GetMemberActionsUseCase
import com.hedvig.android.feature.help.center.data.GetMemberActionsUseCaseImpl
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.featureflags.FeatureManager
import org.koin.androidx.viewmodel.dsl.viewModel
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
    )
  }
  viewModel<HelpCenterViewModel> {
    HelpCenterViewModel(
      getQuickLinksUseCase = get<GetQuickLinksUseCase>(),
      hasAnyActiveConversationUseCase = get<HasAnyActiveConversationUseCase>(),
    )
  }
}
