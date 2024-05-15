package com.hedvig.android.feature.help.center

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
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

  data class SearchForQuery(val query: String) : HelpCenterEvent

  data object ClearSearchQuery : HelpCenterEvent
}

internal data class HelpCenterUiState(
  val topics: ImmutableList<Topic>,
  val questions: ImmutableList<Question>,
  val quickLinksUiState: QuickLinkUiState,
  val selectedQuickAction: QuickAction?,
  val search: Search?,
) {
  data class QuickLink(val quickAction: QuickAction)

  sealed interface QuickLinkUiState {
    data object Loading : QuickLinkUiState

    data object NoQuickLinks : QuickLinkUiState

    data class QuickLinks(val quickLinks: ImmutableList<QuickLink>) : QuickLinkUiState
  }

  data class Search(
    val query: String,
    val activeSearchState: ActiveSearchState,
  )

  sealed interface ActiveSearchState {
    data object Loading : ActiveSearchState
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
  private val context: Context,
) : MoleculePresenter<HelpCenterEvent, HelpCenterUiState> {
  @Composable
  override fun MoleculePresenterScope<HelpCenterEvent>.present(lastState: HelpCenterUiState): HelpCenterUiState {
    var selectedQuickAction by remember { mutableStateOf<QuickAction?>(null) }
    var quickLinksUiState by remember { mutableStateOf(lastState.quickLinksUiState) }
    var searchQuery by remember { mutableStateOf<String?>(null) }
    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        is HelpCenterEvent.OnQuickActionSelected -> selectedQuickAction = event.quickAction
        is HelpCenterEvent.OnDismissQuickActionDialog -> selectedQuickAction = null
        HelpCenterEvent.ClearSearchQuery -> {
          currentState = currentState.copy(search = null)
        }

        is HelpCenterEvent.SearchForQuery -> {
          searchQuery = event.query
        }
      }
    }

    LaunchedEffect(searchQuery) {
      val query = searchQuery
      if (query != null) {
        currentState = currentState.copy(
            search = HelpCenterUiState.Search(query, HelpCenterUiState.ActiveSearchState.Loading),
        )
        val results = searchForQuery(
          query.lowercase(),
          currentState.quickLinksUiState,
          context,
        )
        currentState = if (results == null) {
          currentState.copy(
              search = HelpCenterUiState.Search(query, HelpCenterUiState.ActiveSearchState.Empty),
          )
        } else
          currentState.copy(
              search = HelpCenterUiState.Search(query, HelpCenterUiState.ActiveSearchState.Success(results)),
          )
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
        ifRight = {
          val list = it.map { action ->
            HelpCenterUiState.QuickLink(action)
          }.toImmutableList()
          HelpCenterUiState.QuickLinkUiState.QuickLinks(list)
        },
      )
    }

    return currentState.copy(
      quickLinksUiState = quickLinksUiState,
      selectedQuickAction = selectedQuickAction,
    )
  }
}


private fun searchForQuery(
  query: String,
  quickLinksUiState: HelpCenterUiState.QuickLinkUiState,
  context: Context,
): HelpCenterUiState.HelpSearchResults? {
  val resultsInQuickLinks = when (quickLinksUiState) {
    HelpCenterUiState.QuickLinkUiState.Loading -> listOf()
    HelpCenterUiState.QuickLinkUiState.NoQuickLinks -> listOf()
    is HelpCenterUiState.QuickLinkUiState.QuickLinks -> {
      buildList {
        for (link in quickLinksUiState.quickLinks) {
          val title = context.getString(link.quickAction.titleRes).lowercase()
          val hint = context.getString(link.quickAction.hintTextRes).lowercase()
          if (title.contains(query) || hint.contains(query)) {
            add(link)
          }
        }
      }

    }
  }.toNonEmptyListOrNull()
  val resultsInQuestions = buildList {
    Question.entries.forEach {
      val answer = context.getString(it.answerRes).lowercase()
      val question = context.getString(it.questionRes).lowercase()
      if (answer.contains(query) || question.contains(query)) {
        add(it)
      }
    }
  }.toNonEmptyListOrNull()
  if (resultsInQuestions == null && resultsInQuickLinks == null) return null else {
    return HelpCenterUiState.HelpSearchResults(resultsInQuickLinks, resultsInQuestions)
  }
}
