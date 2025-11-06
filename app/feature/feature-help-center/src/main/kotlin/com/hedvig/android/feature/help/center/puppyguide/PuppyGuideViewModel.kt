package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.help.center.data.GetPuppyGuideUseCase
import com.hedvig.android.feature.help.center.data.PuppyGuideStory
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class PuppyGuideViewModel(
  getPuppyGuideUseCase: GetPuppyGuideUseCase,
) : MoleculeViewModel<PuppyGuideEvent, PuppyGuideUiState>(
    presenter = PuppyGuidePresenter(getPuppyGuideUseCase),
    initialState = PuppyGuideUiState.Loading,
  )

private class PuppyGuidePresenter(
  private val getPuppyGuideUseCase: GetPuppyGuideUseCase,
) : MoleculePresenter<PuppyGuideEvent, PuppyGuideUiState> {
  @Composable
  override fun MoleculePresenterScope<PuppyGuideEvent>.present(
    lastState: PuppyGuideUiState,
  ): PuppyGuideUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        PuppyGuideEvent.Reload -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      getPuppyGuideUseCase.invoke().fold(
        ifLeft = {
          currentState = PuppyGuideUiState.Failure
        },
        ifRight = { stories ->
          currentState = if (stories == null) {
            PuppyGuideUiState.Failure
          } else {
            PuppyGuideUiState.Success(stories)
          }
        },
      )
    }

    return currentState
  }
}

internal sealed interface PuppyGuideEvent {
  data object Reload : PuppyGuideEvent
}

internal sealed interface PuppyGuideUiState {
  data class Success(val stories: List<PuppyGuideStory>) : PuppyGuideUiState
  data object Loading : PuppyGuideUiState
  data object Failure : PuppyGuideUiState
}
