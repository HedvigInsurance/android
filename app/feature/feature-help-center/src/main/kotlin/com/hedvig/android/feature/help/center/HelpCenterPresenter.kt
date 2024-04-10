package com.hedvig.android.feature.help.center

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal sealed interface HelpCenterEvent {
  data class OnQuickActionSelected(val quickAction: QuickAction) : HelpCenterEvent

  data object OnDismissQuickActionDialog : HelpCenterEvent
}

internal data class HelpCenterUiState(
  val topics: ImmutableList<Topic>,
  val questions: ImmutableList<Question>,
  val quickLinksUiState: QuickLinkUiState,
  val selectedQuickAction: QuickAction?,
) {
  data class QuickLink(val quickAction: QuickAction)

  sealed interface QuickLinkUiState {
    data object Loading : QuickLinkUiState

    data object NoQuickLinks : QuickLinkUiState

    data class QuickLinks(val quickLinks: ImmutableList<QuickLink>) : QuickLinkUiState
  }
}

internal class HelpCenterPresenter(
  private val getQuickLinksUseCase: GetQuickLinksUseCase,
) : MoleculePresenter<HelpCenterEvent, HelpCenterUiState> {
  @Composable
  override fun MoleculePresenterScope<HelpCenterEvent>.present(lastState: HelpCenterUiState): HelpCenterUiState {
    var currentUiState by remember {
      mutableStateOf(lastState)
    }
    var selectedQuickAction by remember { mutableStateOf<QuickAction?>(null) }

    CollectEvents { event ->
      selectedQuickAction = when (event) {
        is HelpCenterEvent.OnQuickActionSelected -> event.quickAction
        is HelpCenterEvent.OnDismissQuickActionDialog -> null
      }
    }

    LaunchedEffect(selectedQuickAction) {
      currentUiState = currentUiState.copy(selectedQuickAction = selectedQuickAction)
    }

    LaunchedEffect(Unit) {
      currentUiState = currentUiState.copy(quickLinksUiState = HelpCenterUiState.QuickLinkUiState.Loading)
      getQuickLinksUseCase.invoke().fold(
        ifLeft = {
          currentUiState = currentUiState.copy(quickLinksUiState = HelpCenterUiState.QuickLinkUiState.NoQuickLinks)
        },
        ifRight = {
          val list = it.map { action ->
            HelpCenterUiState.QuickLink(action)
          }.toImmutableList()
          currentUiState = currentUiState.copy(
            quickLinksUiState = HelpCenterUiState.QuickLinkUiState.QuickLinks(list),
          )
        },
      )
    }

    return currentUiState
  }
}
