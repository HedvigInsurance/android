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

internal class PuppyArticleViewModel(
  getPuppyGuideUseCase: GetPuppyGuideUseCase,
  storyName: String,
) : MoleculeViewModel<PuppyArticleEvent, PuppyArticleUiState>(
    presenter = PuppyArticlePresenter(getPuppyGuideUseCase, storyName),
    initialState = PuppyArticleUiState.Loading,
  )

private class PuppyArticlePresenter(
  private val getPuppyGuideUseCase: GetPuppyGuideUseCase,
  private val storyName: String,
) : MoleculePresenter<PuppyArticleEvent, PuppyArticleUiState> {
  @Composable
  override fun MoleculePresenterScope<PuppyArticleEvent>.present(lastState: PuppyArticleUiState): PuppyArticleUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        PuppyArticleEvent.Reload -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      getPuppyGuideUseCase.invoke().fold(
        ifLeft = {
          currentState = PuppyArticleUiState.Failure
        },
        ifRight = { stories ->
          val matchingStory = stories?.firstOrNull { it.name == storyName }
          currentState = if (matchingStory == null) {
            PuppyArticleUiState.Failure
          } else {
            PuppyArticleUiState.Success(matchingStory)
          }
        },
      )
    }

    return currentState
  }
}

internal sealed interface PuppyArticleEvent {
  data object Reload: PuppyArticleEvent
}

internal sealed interface PuppyArticleUiState {
  data class Success(val story: PuppyGuideStory) : PuppyArticleUiState
  data object Loading: PuppyArticleUiState
  data object Failure: PuppyArticleUiState
}
