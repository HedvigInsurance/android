package com.hedvig.android.feature.help.center

import androidx.lifecycle.ViewModel
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.help.center.data.GetHelpCenterFAQUseCase
import com.hedvig.android.feature.help.center.data.GetPuppyGuideUseCase
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Inject
@ViewModelKey
@ContributesIntoMap(ActivityRetainedScope::class, binding<ViewModel>())
internal class HelpCenterViewModel(
  getQuickLinksUseCase: GetQuickLinksUseCase,
  hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
  getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase,
  getPuppyGuideUseCase: GetPuppyGuideUseCase,
  backstack: Backstack,
) : MoleculeViewModel<HelpCenterEvent, HelpCenterUiState>(
    initialState = HelpCenterUiState(
      topics = listOf(),
      questions = listOf(),
      selectedQuickAction = null,
      quickLinksUiState = HelpCenterUiState.QuickLinkUiState.Loading,
      search = null,
      showNavigateToInboxButton = false,
      puppyGuide = null,
    ),
    presenter = HelpCenterPresenter(
      getQuickLinksUseCase = getQuickLinksUseCase,
      hasAnyActiveConversationUseCase = hasAnyActiveConversationUseCase,
      getHelpCenterFAQUseCase = getHelpCenterFAQUseCase,
      getPuppyGuideUseCase = getPuppyGuideUseCase,
      backstack = backstack,
    ),
  )
