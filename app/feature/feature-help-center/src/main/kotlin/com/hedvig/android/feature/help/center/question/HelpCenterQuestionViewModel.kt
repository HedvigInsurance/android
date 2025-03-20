package com.hedvig.android.feature.help.center.question

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.help.center.data.FAQItem
import com.hedvig.android.feature.help.center.data.GetHelpCenterQuestionUseCase
import com.hedvig.android.feature.help.center.data.HelpCenterQuestionError.GenericError
import com.hedvig.android.feature.help.center.data.HelpCenterQuestionError.NoQuestionFound
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class HelpCenterQuestionViewModel(
  questionId: String,
  getHelpCenterQuestionUseCase: GetHelpCenterQuestionUseCase,
) :
  MoleculeViewModel<HelpCenterQuestionEvent, HelpCenterQuestionUiState>(
      presenter = HelpCenterQuestionPresenter(questionId, getHelpCenterQuestionUseCase),
      initialState = HelpCenterQuestionUiState.Loading,
    )

private class HelpCenterQuestionPresenter(
  private val questionId: String?,
  private val getHelpCenterQuestionUseCase: GetHelpCenterQuestionUseCase,
) : MoleculePresenter<HelpCenterQuestionEvent, HelpCenterQuestionUiState> {
  @Composable
  override fun MoleculePresenterScope<HelpCenterQuestionEvent>.present(
    lastState: HelpCenterQuestionUiState,
  ): HelpCenterQuestionUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        HelpCenterQuestionEvent.Reload -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      if (questionId == null) {
        currentState = HelpCenterQuestionUiState.NoQuestionFound
      } else {
        getHelpCenterQuestionUseCase.invoke(questionId).fold(
          ifLeft = { error ->
            when (error) {
              is GenericError -> currentState = HelpCenterQuestionUiState.Failure
              NoQuestionFound -> currentState = HelpCenterQuestionUiState.NoQuestionFound
            }
          },
          ifRight = { item ->
            currentState = HelpCenterQuestionUiState.Success(
              item,
            )
          },
        )
      }
    }

    return currentState
  }
}

internal sealed interface HelpCenterQuestionEvent {
  data object Reload : HelpCenterQuestionEvent
}

internal sealed interface HelpCenterQuestionUiState {
  data class Success(val faqItem: FAQItem) : HelpCenterQuestionUiState

  data object Loading : HelpCenterQuestionUiState

  data object Failure : HelpCenterQuestionUiState

  data object NoQuestionFound : HelpCenterQuestionUiState
}
