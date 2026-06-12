package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.help.center.data.GetPuppyGuideUseCase
import com.hedvig.android.feature.help.center.data.PuppyGuideStory
import com.hedvig.android.feature.help.center.data.SetArticleRatingUseCase
import com.hedvig.android.feature.help.center.data.SetArticleReadUseCase
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel
internal class PuppyArticleViewModel(
  getPuppyGuideUseCase: GetPuppyGuideUseCase,
  setArticleRatingUseCase: SetArticleRatingUseCase,
  setArticleReadUseCase: SetArticleReadUseCase,
  @Assisted storyName: String,
) : MoleculeViewModel<PuppyArticleEvent, PuppyArticleUiState>(
    presenter = PuppyArticlePresenter(
      getPuppyGuideUseCase,
      storyName,
      setArticleRatingUseCase,
      setArticleReadUseCase,
    ),
    initialState = PuppyArticleUiState.Loading,
  )

private class PuppyArticlePresenter(
  private val getPuppyGuideUseCase: GetPuppyGuideUseCase,
  private val storyName: String,
  private val setArticleRatingUseCase: SetArticleRatingUseCase,
  private val setArticleReadUseCase: SetArticleReadUseCase,
) : MoleculePresenter<PuppyArticleEvent, PuppyArticleUiState> {
  @Composable
  override fun MoleculePresenterScope<PuppyArticleEvent>.present(lastState: PuppyArticleUiState): PuppyArticleUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var rating by remember { mutableStateOf<Int?>(null) }
    var reachedBottom by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        PuppyArticleEvent.Reload -> {
          loadIteration++
        }

        is PuppyArticleEvent.RatingClick -> {
          rating = event.rating
        }

        PuppyArticleEvent.ReachedBottom -> {
          reachedBottom = true
        }
      }
    }

    LaunchedEffect(loadIteration) {
      getPuppyGuideUseCase.invoke().collect { response ->
        response.fold(
          ifLeft = {
            currentState = PuppyArticleUiState.Failure
          },
          ifRight = { puppyGuide ->
            val matchingStory = puppyGuide.stories.firstOrNull { it.name == storyName }
            currentState = if (matchingStory == null) {
              PuppyArticleUiState.Failure
            } else {
              rating = matchingStory.rating
              PuppyArticleUiState.Success(matchingStory)
            }
          },
        )
      }
    }

    LaunchedEffect(rating) {
      val state = currentState as? PuppyArticleUiState.Success ?: return@LaunchedEffect
      val currentRating = rating ?: return@LaunchedEffect
      val articleName = state.story.name
      setArticleRatingUseCase.invoke(
        articleName = articleName,
        rating = currentRating,
      ).fold(
        ifLeft = { logcat { "setArticleRatingUseCase rating failed!" } },
        ifRight = { logcat { "setArticleRatingUseCase rating set!" } },
      )
    }

    LaunchedEffect(reachedBottom) {
      if (!reachedBottom) return@LaunchedEffect
      val state = currentState as? PuppyArticleUiState.Success ?: return@LaunchedEffect
      if (state.story.isRead) return@LaunchedEffect
      setArticleReadUseCase.invoke(state.story.name)
    }

    return when (val state = currentState) {
      PuppyArticleUiState.Failure -> {
        state
      }

      PuppyArticleUiState.Loading -> {
        state
      }

      is PuppyArticleUiState.Success -> {
        state.copy(
          story = state.story.copy(rating = rating),
        )
      }
    }
  }
}

internal sealed interface PuppyArticleEvent {
  data object Reload : PuppyArticleEvent

  data class RatingClick(val rating: Int) : PuppyArticleEvent

  data object ReachedBottom : PuppyArticleEvent
}

internal sealed interface PuppyArticleUiState {
  data class Success(val story: PuppyGuideStory) : PuppyArticleUiState

  data object Loading : PuppyArticleUiState

  data object Failure : PuppyArticleUiState
}
