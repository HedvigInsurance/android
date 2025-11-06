package com.hedvig.android.feature.help.center.puppyguide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.help.center.data.PuppyGuideStory
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class PuppyArticleViewModel(
  story: PuppyGuideStory,
) : MoleculeViewModel<PuppyArticleEvent, PuppyArticleUiState>(
    presenter = PuppyArticlePresenter(),
    initialState = PuppyArticleUiState.Success(story),
  )

private class PuppyArticlePresenter() : MoleculePresenter<PuppyArticleEvent, PuppyArticleUiState> {
  @Composable
  override fun MoleculePresenterScope<PuppyArticleEvent>.present(lastState: PuppyArticleUiState): PuppyArticleUiState {
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        // Handle events here if needed
        else -> {
          // todo
        }
      }
    }

    return currentState
  }
}

internal sealed interface PuppyArticleEvent

internal sealed interface PuppyArticleUiState {
  data class Success(val story: PuppyGuideStory) : PuppyArticleUiState
}
