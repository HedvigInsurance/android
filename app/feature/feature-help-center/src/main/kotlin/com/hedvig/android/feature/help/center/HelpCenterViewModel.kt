package com.hedvig.android.feature.help.center

import com.hedvig.android.feature.help.center.data.GetCommonClaimsUseCase
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.feature.help.center.model.commonQuestions
import com.hedvig.android.feature.help.center.model.commonTopics
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class HelpCenterViewModel(
  getCommonClaimsUseCase: GetCommonClaimsUseCase,
  getQuickLinksUseCase: GetQuickLinksUseCase,
) : MoleculeViewModel<HelpCenterEvent, HelpCenterUiState>(
    initialState = HelpCenterUiState(
      topics = commonTopics,
      questions = commonQuestions,
      selectedQuickAction = null,
      quickLinksUiState = HelpCenterUiState.QuickLinkUiState.Loading,
    ),
    presenter = HelpCenterPresenter(
      getQuickLinksUseCase = getQuickLinksUseCase,
    ),
  )
