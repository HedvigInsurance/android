package com.hedvig.android.feature.help.center

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.help.center.commonclaim.CommonClaim
import com.hedvig.android.feature.help.center.data.GetCommonClaimsUseCase
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.model.commonQuestions
import com.hedvig.android.feature.help.center.model.commonTopics
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

internal sealed interface HelpCenterEvent {
  data class OnQuickActionSelected(val quickAction: QuickAction) : HelpCenterEvent

  data object OnDismissQuickActionDialog : HelpCenterEvent
}

internal data class HelpCenterUiState(
  val topics: ImmutableList<Topic>,
  val questions: ImmutableList<Question>,
  val quickLinks: ImmutableList<QuickLinkType>,
  val isLoadingQuickLinks: Boolean,
  val selectedQuickAction: QuickAction?,
) {
  sealed interface QuickLinkType {
    data class QuickActionType(val quickAction: QuickAction) : QuickLinkType

    data class CommonClaimType(val commonClaim: CommonClaim) : QuickLinkType
  }
}

internal class HelpCenterPresenter(
  private val getCommonClaimsUseCase: GetCommonClaimsUseCase,
  private val getQuickLinksUseCase: GetQuickLinksUseCase,
) : MoleculePresenter<HelpCenterEvent, HelpCenterUiState> {
  @Composable
  override fun MoleculePresenterScope<HelpCenterEvent>.present(lastState: HelpCenterUiState): HelpCenterUiState {
    var quickActions by remember {
      mutableStateOf(
        lastState.quickLinks.filterIsInstance<HelpCenterUiState.QuickLinkType.QuickActionType>().map { it.quickAction },
      )
    }
    var commonClaims by remember {
      mutableStateOf(
        lastState.quickLinks.filterIsInstance<HelpCenterUiState.QuickLinkType.CommonClaimType>().map { it.commonClaim },
      )
    }
    var selectedQuickAction by remember { mutableStateOf<QuickAction?>(null) }
    var isLoadingQuickLinks by remember { mutableStateOf(lastState.quickLinks.isEmpty()) }

    CollectEvents { event ->
      selectedQuickAction = when (event) {
        is HelpCenterEvent.OnQuickActionSelected -> event.quickAction
        is HelpCenterEvent.OnDismissQuickActionDialog -> null
      }
    }

    LaunchedEffect(Unit) {
      isLoadingQuickLinks = true
      combine(
        flow { emit(getQuickLinksUseCase.invoke()) },
        flow { emit(getCommonClaimsUseCase.invoke()) },
      ) { quickLinksResult, commonClaimsResult ->
        quickLinksResult.fold(
          ifLeft = { quickActions = persistentListOf() },
          ifRight = { quickActions = it },
        )
        commonClaimsResult.fold(
          ifLeft = { commonClaims = persistentListOf() },
          ifRight = { commonClaims = it },
        )
        isLoadingQuickLinks = false
      }.collectLatest {}
    }

    return HelpCenterUiState(
      topics = commonTopics,
      questions = commonQuestions,
      quickLinks = persistentListOf(
        *quickActions.map { HelpCenterUiState.QuickLinkType.QuickActionType(it) }.toTypedArray(),
        *commonClaims.map { HelpCenterUiState.QuickLinkType.CommonClaimType(it) }.toTypedArray(),
      ),
      selectedQuickAction = selectedQuickAction,
      isLoadingQuickLinks = isLoadingQuickLinks,
    )
  }
}
