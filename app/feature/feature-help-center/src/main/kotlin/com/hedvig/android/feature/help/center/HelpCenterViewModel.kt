package com.hedvig.android.feature.help.center

import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.help.center.data.GetHelpCenterFAQUseCase
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class HelpCenterViewModel(
  getQuickLinksUseCase: GetQuickLinksUseCase,
  hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
  getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase,
) : MoleculeViewModel<HelpCenterEvent, HelpCenterUiState>(
    initialState = HelpCenterUiState(
      topics = listOf(),
      questions = listOf(),
      selectedQuickAction = null,
      quickLinksUiState = HelpCenterUiState.QuickLinkUiState.Loading,
      search = null,
      showNavigateToInboxButton = false,
    ),
    presenter = HelpCenterPresenter(
      getQuickLinksUseCase = getQuickLinksUseCase,
      hasAnyActiveConversationUseCase = hasAnyActiveConversationUseCase,
      getHelpCenterFAQUseCase = getHelpCenterFAQUseCase,
    ),
  )
