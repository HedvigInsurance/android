package com.hedvig.android.feature.help.center

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.NonEmptyList
import arrow.core.merge
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.help.center.HelpCenterEvent.ClearNavigation
import com.hedvig.android.feature.help.center.HelpCenterEvent.ClearSearchQuery
import com.hedvig.android.feature.help.center.HelpCenterEvent.NavigateToQuickAction
import com.hedvig.android.feature.help.center.HelpCenterEvent.OnDismissQuickActionDialog
import com.hedvig.android.feature.help.center.HelpCenterEvent.OnQuickActionSelected
import com.hedvig.android.feature.help.center.HelpCenterEvent.UpdateSearchResults
import com.hedvig.android.feature.help.center.HelpCenterUiState.ActiveSearchState.Empty
import com.hedvig.android.feature.help.center.HelpCenterUiState.ActiveSearchState.Success
import com.hedvig.android.feature.help.center.HelpCenterUiState.Search
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.map

internal sealed interface HelpCenterEvent {
  data class OnQuickActionSelected(val quickAction: QuickAction) : HelpCenterEvent

  data object OnDismissQuickActionDialog : HelpCenterEvent

  data class UpdateSearchResults(
    val searchQuery: String,
    val results: HelpCenterUiState.HelpSearchResults?,
  ) : HelpCenterEvent

  data object ClearSearchQuery : HelpCenterEvent

  data class NavigateToQuickAction(val destination: QuickLinkDestination) : HelpCenterEvent

  data object ClearNavigation : HelpCenterEvent
}

internal data class HelpCenterUiState(
  val topics: List<Topic>,
  val questions: List<Question>,
  val quickLinksUiState: QuickLinkUiState,
  val selectedQuickAction: QuickAction?,
  val search: Search?,
  val showNavigateToInboxButton: Boolean,
  val destinationToNavigate: QuickLinkDestination? = null,
) {
  data class QuickLink(val quickAction: QuickAction)

  sealed interface QuickLinkUiState {
    data object Loading : QuickLinkUiState

    data object NoQuickLinks : QuickLinkUiState

    data class QuickLinks(val quickLinks: NonEmptyList<QuickLink>) : QuickLinkUiState
  }

  data class Search(
    val searchQuery: String?,
    val activeSearchState: ActiveSearchState,
  )

  sealed interface ActiveSearchState {
    data object Empty : ActiveSearchState

    data class Success(val results: HelpSearchResults) : ActiveSearchState
  }

  data class HelpSearchResults(
    val filteredQuickLinks: NonEmptyList<QuickLink>?,
    val filteredQuestions: NonEmptyList<Question>?,
  )
}

internal class HelpCenterPresenter(
  private val getQuickLinksUseCase: GetQuickLinksUseCase,
  private val hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
) : MoleculePresenter<HelpCenterEvent, HelpCenterUiState> {
  @Composable
  override fun MoleculePresenterScope<HelpCenterEvent>.present(lastState: HelpCenterUiState): HelpCenterUiState {
    var selectedQuickAction by remember { mutableStateOf<QuickAction?>(null) }
    var quickLinksUiState by remember { mutableStateOf(lastState.quickLinksUiState) }
    var currentState by remember {
      mutableStateOf(lastState)
    }
    val hasAnyActiveConversation by remember(hasAnyActiveConversationUseCase) {
      hasAnyActiveConversationUseCase.invoke().map { it.mapLeft { false }.merge() }
    }.collectAsState(false)

    CollectEvents { event ->
      when (event) {
        is OnQuickActionSelected -> selectedQuickAction = event.quickAction
        is OnDismissQuickActionDialog -> selectedQuickAction = null
        ClearSearchQuery -> {
          currentState = currentState.copy(search = null)
        }

        is UpdateSearchResults -> {
          currentState = if (event.results == null) {
            currentState.copy(
              search = Search(
                event.searchQuery,
                Empty,
              ),
            )
          } else {
            currentState.copy(
              search = Search(
                event.searchQuery,
                Success(event.results),
              ),
            )
          }
        }

        ClearNavigation -> {
          selectedQuickAction = null
          currentState = currentState.copy(destinationToNavigate = null)
        }
        is NavigateToQuickAction -> {
          selectedQuickAction = null
          currentState = currentState.copy(destinationToNavigate = event.destination)
        }
      }
    }

    LaunchedEffect(Unit) {
      if (quickLinksUiState !is HelpCenterUiState.QuickLinkUiState.QuickLinks) {
        quickLinksUiState = HelpCenterUiState.QuickLinkUiState.Loading
      }
      quickLinksUiState = getQuickLinksUseCase.invoke().fold(
        ifLeft = {
          HelpCenterUiState.QuickLinkUiState.NoQuickLinks
        },
        ifRight = { quickActionList ->
          val list = quickActionList.map { quickAction ->
            HelpCenterUiState.QuickLink(quickAction)
          }.toNonEmptyListOrNull()
          if (list == null) {
            HelpCenterUiState.QuickLinkUiState.NoQuickLinks
          } else {
            HelpCenterUiState.QuickLinkUiState.QuickLinks(list)
          }
        },
      )
    }
    return currentState.copy(
      quickLinksUiState = quickLinksUiState,
      selectedQuickAction = selectedQuickAction,
      showNavigateToInboxButton = hasAnyActiveConversation,
    )
  }
}
