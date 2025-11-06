package com.hedvig.android.feature.help.center

import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.help.center.data.GetHelpCenterFAQUseCase
import com.hedvig.android.feature.help.center.data.GetPuppyGuideUseCase
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class HelpCenterViewModel(
  getQuickLinksUseCase: GetQuickLinksUseCase,
  hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
  getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase,
  getPuppyGuideUseCase: GetPuppyGuideUseCase

) : MoleculeViewModel<HelpCenterEvent, HelpCenterUiState>(
    initialState = HelpCenterUiState(
      topics = listOf(),
      questions = listOf(),
      selectedQuickAction = null,
      quickLinksUiState = HelpCenterUiState.QuickLinkUiState.Loading,
      search = null,
      showNavigateToInboxButton = false,
      puppyGuide = null
    ),
    presenter = HelpCenterPresenter(
      getQuickLinksUseCase = getQuickLinksUseCase,
      hasAnyActiveConversationUseCase = hasAnyActiveConversationUseCase,
      getHelpCenterFAQUseCase = getHelpCenterFAQUseCase,
      getPuppyGuideUseCase = getPuppyGuideUseCase
    ),
  )
