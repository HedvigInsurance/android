package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class TerminationSurveyViewModel(): MoleculeViewModel<TerminationSurveyEvent, TerminationSurveyState>(
  initialState = TerminationSurveyState.Loading,
  presenter = TerminationSurveyPresenter()
)

internal class TerminationSurveyPresenter(): MoleculePresenter<TerminationSurveyEvent, TerminationSurveyState> {
  @Composable
  override fun MoleculePresenterScope<TerminationSurveyEvent>.present(lastState: TerminationSurveyState)
  : TerminationSurveyState {

    CollectEvents {
      TODO("Not yet implemented")
    }
    TODO("Not yet implemented")
  }
}

internal sealed interface TerminationSurveyEvent {
  data class ChooseOption(val id: String): TerminationSurveyEvent
  data object Retry: TerminationSurveyEvent
}

internal sealed interface TerminationSurveyState {
  data object Error: TerminationSurveyState
  data object Loading: TerminationSurveyState

  data class Success(
    val options: List<TerminationSurveyOption>
  ): TerminationSurveyState
}
