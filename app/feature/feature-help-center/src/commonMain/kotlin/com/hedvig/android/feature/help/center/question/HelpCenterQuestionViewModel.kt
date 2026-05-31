package com.hedvig.android.feature.help.center.question

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.help.center.data.FAQItem
import com.hedvig.android.feature.help.center.data.GetHelpCenterQuestionUseCase
import com.hedvig.android.feature.help.center.data.HelpCenterQuestionError.GenericError
import com.hedvig.android.feature.help.center.data.HelpCenterQuestionError.NoQuestionFound
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey

@AssistedInject
internal class HelpCenterQuestionViewModel(
  @Assisted questionId: String,
  getHelpCenterQuestionUseCase: GetHelpCenterQuestionUseCase,
) :
  MoleculeViewModel<HelpCenterQuestionEvent, HelpCenterQuestionUiState>(
      presenter = HelpCenterQuestionPresenter(questionId, getHelpCenterQuestionUseCase),
      initialState = HelpCenterQuestionUiState.Loading,
    ) {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted questionId: String,
    ): HelpCenterQuestionViewModel
  }
}

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
