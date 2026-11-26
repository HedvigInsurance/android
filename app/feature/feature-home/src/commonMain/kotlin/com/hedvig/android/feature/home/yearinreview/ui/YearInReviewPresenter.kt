package com.hedvig.android.feature.home.yearinreview.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.feature.home.yearinreview.data.GetYearInReviewUseCase
import com.hedvig.android.feature.home.yearinreview.data.YearInReviewStory
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

internal val StoryDuration: Duration = 10.seconds
private const val ProgressSteps = 100

internal class YearInReviewPresenter(
  private val getYearInReviewUseCase: GetYearInReviewUseCase,
) : MoleculePresenter<YearInReviewEvent, YearInReviewUiState> {
  @Composable
  override fun MoleculePresenterScope<YearInReviewEvent>.present(lastState: YearInReviewUiState): YearInReviewUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var stories: List<YearInReviewStory>? by remember {
      mutableStateOf((lastState as? YearInReviewUiState.Content)?.stories)
    }
    var hasError by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableIntStateOf((lastState as? YearInReviewUiState.Content)?.currentIndex ?: 0) }
    var progress by remember { mutableFloatStateOf((lastState as? YearInReviewUiState.Content)?.progress ?: 0f) }
    // Starts paused; the screen resumes once it can show the stories.
    var isPaused by remember { mutableStateOf(true) }
    // Bumped to restart the current story's timer when the index itself does not change.
    var restartCount by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      val lastIndex = stories?.lastIndex ?: -1
      when (event) {
        YearInReviewEvent.Next -> {
          if (currentIndex < lastIndex) {
            Snapshot.withMutableSnapshot {
              currentIndex++
              progress = 0f
            }
          }
        }

        YearInReviewEvent.Previous -> {
          if (lastIndex >= 0) {
            Snapshot.withMutableSnapshot {
              if (currentIndex > 0) currentIndex--
              progress = 0f
              restartCount++
            }
          }
        }

        YearInReviewEvent.Pause -> {
          isPaused = true
        }

        YearInReviewEvent.Resume -> {
          isPaused = false
        }

        YearInReviewEvent.Retry -> {
          loadIteration++
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (stories != null) return@LaunchedEffect
      hasError = false
      getYearInReviewUseCase.invoke().fold(
        ifLeft = { hasError = true },
        ifRight = { yearInReview -> stories = yearInReview.stories },
      )
    }

    val loadedStories = stories
    LaunchedEffect(loadedStories, currentIndex, isPaused, restartCount) {
      if (loadedStories == null || isPaused) return@LaunchedEffect
      val stepDuration = StoryDuration / ProgressSteps
      var step = (progress * ProgressSteps).toInt()
      while (step < ProgressSteps) {
        delay(stepDuration)
        step++
        progress = step.toFloat() / ProgressSteps
      }
      if (currentIndex < loadedStories.lastIndex) {
        Snapshot.withMutableSnapshot {
          currentIndex++
          progress = 0f
        }
      }
    }

    return when {
      loadedStories != null -> YearInReviewUiState.Content(
        stories = loadedStories,
        currentIndex = currentIndex,
        progress = progress,
      )

      hasError -> YearInReviewUiState.Failure

      else -> YearInReviewUiState.Loading
    }
  }
}

internal sealed interface YearInReviewEvent {
  data object Next : YearInReviewEvent

  data object Previous : YearInReviewEvent

  data object Pause : YearInReviewEvent

  data object Resume : YearInReviewEvent

  data object Retry : YearInReviewEvent
}

internal sealed interface YearInReviewUiState {
  data object Loading : YearInReviewUiState

  data object Failure : YearInReviewUiState

  data class Content(
    val stories: List<YearInReviewStory>,
    val currentIndex: Int,
    /** How far the current story's timer has run, from 0 to 1. */
    val progress: Float,
  ) : YearInReviewUiState {
    val currentStory: YearInReviewStory = stories[currentIndex]
    val isLastStory: Boolean = currentIndex == stories.lastIndex
  }
}
