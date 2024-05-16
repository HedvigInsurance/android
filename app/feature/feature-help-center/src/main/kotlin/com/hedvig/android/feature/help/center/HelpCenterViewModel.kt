package com.hedvig.android.feature.help.center

import android.content.Context
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.feature.help.center.model.commonQuestions
import com.hedvig.android.feature.help.center.model.commonTopics
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class HelpCenterViewModel(
  getQuickLinksUseCase: GetQuickLinksUseCase,
  context: Context,
) : MoleculeViewModel<HelpCenterEvent, HelpCenterUiState>(
    initialState = HelpCenterUiState(
      topics = commonTopics,
      questions = commonQuestions,
      selectedQuickAction = null,
      quickLinksUiState = HelpCenterUiState.QuickLinkUiState.Loading,
      search = null,
    ),
    presenter = HelpCenterPresenter(
      getQuickLinksUseCase = getQuickLinksUseCase,
      context = context,
    ),
  )
