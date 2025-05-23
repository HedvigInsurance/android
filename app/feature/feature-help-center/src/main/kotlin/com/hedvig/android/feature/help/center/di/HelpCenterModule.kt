package com.hedvig.android.feature.help.center.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.help.center.HelpCenterViewModel
import com.hedvig.android.feature.help.center.ShowNavigateToInboxViewModel
import com.hedvig.android.feature.help.center.data.GetHelpCenterFAQUseCase
import com.hedvig.android.feature.help.center.data.GetHelpCenterFAQUseCaseImpl
import com.hedvig.android.feature.help.center.data.GetHelpCenterQuestionUseCase
import com.hedvig.android.feature.help.center.data.GetHelpCenterQuestionUseCaseImpl
import com.hedvig.android.feature.help.center.data.GetHelpCenterTopicUseCase
import com.hedvig.android.feature.help.center.data.GetHelpCenterTopicUseCaseImpl
import com.hedvig.android.feature.help.center.data.GetInsuranceForEditCoInsuredUseCase
import com.hedvig.android.feature.help.center.data.GetInsuranceForEditCoInsuredUseCaseImpl
import com.hedvig.android.feature.help.center.data.GetMemberActionsUseCase
import com.hedvig.android.feature.help.center.data.GetMemberActionsUseCaseImpl
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionViewModel
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicViewModel
import com.hedvig.android.featureflags.FeatureManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val helpCenterModule = module {

  single<GetHelpCenterFAQUseCase> {
    GetHelpCenterFAQUseCaseImpl(get<ApolloClient>())
  }

  single<GetHelpCenterTopicUseCase> {
    GetHelpCenterTopicUseCaseImpl(get<GetHelpCenterFAQUseCase>())
  }

  single<GetQuickLinksUseCase> {
    GetQuickLinksUseCase(
      apolloClient = get(),
      featureManager = get(),
      getMemberActionsUseCase = get<GetMemberActionsUseCase>(),
    )
  }

  single<GetHelpCenterQuestionUseCase> {
    GetHelpCenterQuestionUseCaseImpl(get<GetHelpCenterFAQUseCase>())
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
      getHelpCenterFAQUseCase = get<GetHelpCenterFAQUseCase>(),
    )
  }

  viewModel<HelpCenterTopicViewModel> { params ->
    HelpCenterTopicViewModel(
      topicId = params.get(),
      getHelpCenterTopicUseCase = get<GetHelpCenterTopicUseCase>(),
    )
  }

  viewModel<HelpCenterQuestionViewModel> { params ->
    HelpCenterQuestionViewModel(
      questionId = params.get(),
      getHelpCenterQuestionUseCase = get<GetHelpCenterQuestionUseCase>(),
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
