package com.hedvig.android.feature.help.center

import com.hedvig.android.feature.help.center.model.QuickLink
import com.hedvig.android.feature.help.center.model.commonQuestions
import com.hedvig.android.feature.help.center.model.commonTopics
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.molecule.android.MoleculeViewModel
import kotlinx.collections.immutable.persistentListOf

internal class HelpCenterViewModel(
  featureManager: FeatureManager,
) : MoleculeViewModel<HelpCenterEvent, HelpCenterUiState>(
initialState = HelpCenterUiState(
  topics = commonTopics,
  questions = commonQuestions,
  quickLinks = persistentListOf(QuickLink.ChangeBank),
),
  presenter = HelpCenterPresenter(featureManager)
)
