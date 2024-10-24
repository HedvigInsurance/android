package com.hedvig.android.feature.change.tier.ui.stepstart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeEvent.Reload
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Failure
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Loading
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Success
import com.hedvig.android.logger.LogPriority.WARN
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class StartTierFlowViewModel(
  insuranceID: String,
  tierRepository: ChangeTierRepository,
) : MoleculeViewModel<StartTierChangeEvent, StartTierChangeState>(
    initialState = Loading,
    presenter = StartTierChangePresenter(
      insuranceID = insuranceID,
      tierRepository = tierRepository,
    ),
  )

private class StartTierChangePresenter(
  private val insuranceID: String,
  private val tierRepository: ChangeTierRepository,
) : MoleculePresenter<StartTierChangeEvent, StartTierChangeState> {
  @Composable
  override fun MoleculePresenterScope<StartTierChangeEvent>.present(
    lastState: StartTierChangeState,
  ): StartTierChangeState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    LaunchedEffect(loadIteration) {
      currentState = Loading
      tierRepository.startChangeTierIntentAndGetQuotesId(insuranceID, ChangeTierCreateSource.SELF_SERVICE).fold(
        ifLeft = { left: ErrorMessage ->
          logcat(WARN) { "Start TierFlow failed with: $left" }
          currentState = Failure
        },
        ifRight = { result ->
          val parameters = InsuranceCustomizationParameters(
            insuranceId = insuranceID,
            activationDate = result.activationDate,
            quoteIds = result.quotes.map { it.id },
          )
          currentState = Success(parameters)
        },
      )
    }
    CollectEvents { event ->
      when (event) {
        Reload -> loadIteration++
      }
    }

    return currentState
  }
}

internal sealed interface StartTierChangeState {
  data object Loading : StartTierChangeState

  data class Success(
    val paramsToNavigate: InsuranceCustomizationParameters?,
  ) : StartTierChangeState

  data object Failure : StartTierChangeState
}

internal sealed interface StartTierChangeEvent {
  data object Reload : StartTierChangeEvent
}
