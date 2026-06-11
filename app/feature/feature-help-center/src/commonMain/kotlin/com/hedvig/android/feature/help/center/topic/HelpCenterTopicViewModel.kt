package com.hedvig.android.feature.help.center.topic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.feature.help.center.data.FAQTopic
import com.hedvig.android.feature.help.center.data.GetHelpCenterTopicUseCase
import com.hedvig.android.feature.help.center.data.HelpCenterTopicError.GenericError
import com.hedvig.android.feature.help.center.data.HelpCenterTopicError.NoTopicFound
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
internal class HelpCenterTopicViewModel(
  @Assisted topicId: String?,
  getHelpCenterTopicUseCase: GetHelpCenterTopicUseCase,
) : MoleculeViewModel<HelpCenterTopicEvent, HelpCenterTopicUiState>(
    presenter = HelpCenterTopicPresenter(topicId, getHelpCenterTopicUseCase),
    initialState = HelpCenterTopicUiState.Loading,
  ) {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(ActivityRetainedScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted topicId: String?,
    ): HelpCenterTopicViewModel
  }
}

private class HelpCenterTopicPresenter(
  private val topicId: String?,
  private val getHelpCenterTopicUseCase: GetHelpCenterTopicUseCase,
) : MoleculePresenter<HelpCenterTopicEvent, HelpCenterTopicUiState> {
  @Composable
  override fun MoleculePresenterScope<HelpCenterTopicEvent>.present(
    lastState: HelpCenterTopicUiState,
  ): HelpCenterTopicUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        HelpCenterTopicEvent.Reload -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      if (topicId == null) {
        currentState = HelpCenterTopicUiState.NoTopicFound
      } else {
        getHelpCenterTopicUseCase.invoke(topicId).fold(
          ifLeft = { error ->
            when (error) {
              is GenericError -> currentState = HelpCenterTopicUiState.Failure
              NoTopicFound -> currentState = HelpCenterTopicUiState.NoTopicFound
            }
          },
          ifRight = { topic ->
            currentState = HelpCenterTopicUiState.Success(
              topic,
            )
          },
        )
      }
    }

    return currentState
  }
}

internal sealed interface HelpCenterTopicEvent {
  data object Reload : HelpCenterTopicEvent
}

internal sealed interface HelpCenterTopicUiState {
  data class Success(val topic: FAQTopic) : HelpCenterTopicUiState

  data object Loading : HelpCenterTopicUiState

  data object Failure : HelpCenterTopicUiState

  data object NoTopicFound : HelpCenterTopicUiState
}
