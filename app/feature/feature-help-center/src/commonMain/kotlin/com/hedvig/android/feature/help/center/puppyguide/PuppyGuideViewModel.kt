package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.help.center.data.GetPuppyGuideUseCase
import com.hedvig.android.feature.help.center.data.PuppyGuideStory
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.coroutines.flow.SharingStarted

internal class PuppyGuideViewModel(
  getPuppyGuideUseCase: GetPuppyGuideUseCase,
  featureManager: FeatureManager,
) : MoleculeViewModel<PuppyGuideEvent, PuppyGuideUiState>(
    presenter = PuppyGuidePresenter(getPuppyGuideUseCase, featureManager),
    initialState = PuppyGuideUiState.Loading,
    sharingStarted = SharingStarted.WhileSubscribed(),
  )

private class PuppyGuidePresenter(
  private val getPuppyGuideUseCase: GetPuppyGuideUseCase,
  private val featureManager: FeatureManager,
) : MoleculePresenter<PuppyGuideEvent, PuppyGuideUiState> {
  @Composable
  override fun MoleculePresenterScope<PuppyGuideEvent>.present(lastState: PuppyGuideUiState): PuppyGuideUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    val puppyGuideEnabled by remember(featureManager) {
      featureManager.isFeatureEnabled(Feature.PUPPY_GUIDE)
    }.collectAsState(null)

    CollectEvents { event ->
      when (event) {
        PuppyGuideEvent.Reload -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration, puppyGuideEnabled) {
      when (puppyGuideEnabled) {
        // Flag not resolved yet, keep showing the loading state.
        null -> {
          currentState = PuppyGuideUiState.Loading
        }

        false -> {
          currentState = PuppyGuideUiState.Disabled
        }

        true -> {
          currentState = PuppyGuideUiState.Loading
          getPuppyGuideUseCase.invoke().collect { response ->
            currentState = response.fold(
              ifLeft = { PuppyGuideUiState.Failure },
              ifRight = { puppyGuide -> PuppyGuideUiState.Success(puppyGuide.stories) },
            )
          }
        }
      }
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

  data object Disabled : PuppyGuideUiState
}
