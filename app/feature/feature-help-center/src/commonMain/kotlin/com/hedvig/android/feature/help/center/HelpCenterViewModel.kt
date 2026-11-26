package com.hedvig.android.feature.help.center

import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.help.center.data.GetHelpCenterFAQUseCase
import com.hedvig.android.feature.help.center.data.GetPuppyGuideUseCase
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.memberquickactions.GetMemberQuickActionsUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class HelpCenterViewModel(
  getMemberQuickActionsUseCase: GetMemberQuickActionsUseCase,
  getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase,
  getPuppyGuideUseCase: GetPuppyGuideUseCase,
  featureManager: FeatureManager,
  backstack: Backstack,
) : MoleculeViewModel<HelpCenterEvent, HelpCenterUiState>(
    initialState = HelpCenterUiState(
      topics = listOf(),
      questions = listOf(),
      selectedQuickAction = null,
      quickLinksUiState = HelpCenterUiState.QuickLinkUiState.Loading,
      search = null,
      puppyGuide = null,
    ),
    presenter = HelpCenterPresenter(
      getMemberQuickActionsUseCase = getMemberQuickActionsUseCase,
      getHelpCenterFAQUseCase = getHelpCenterFAQUseCase,
      featureManager = featureManager,
      getPuppyGuideUseCase = getPuppyGuideUseCase,
      backstack = backstack,
    ),
  )
