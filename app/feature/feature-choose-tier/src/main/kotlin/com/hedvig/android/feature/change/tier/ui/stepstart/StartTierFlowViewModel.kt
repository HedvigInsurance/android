package com.hedvig.android.feature.change.tier.ui.stepstart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.feature.change.tier.navigation.ChooseTierKey
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowKey
import com.hedvig.android.feature.change.tier.ui.stepstart.FailureReason.GENERAL
import com.hedvig.android.feature.change.tier.ui.stepstart.FailureReason.QUOTES_ARE_EMPTY
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeEvent.Reload
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Failure
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Loading
import com.hedvig.android.logger.LogPriority.WARN
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey

@AssistedInject
internal class StartTierFlowViewModel(
  @Assisted insuranceID: String,
  tierRepository: ChangeTierRepository,
  backstack: Backstack,
) : MoleculeViewModel<StartTierChangeEvent, StartTierChangeState>(
    initialState = Loading,
    presenter = StartTierChangePresenter(
      insuranceID = insuranceID,
      tierRepository = tierRepository,
      backstack = backstack,
    ),
  ) {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(ActivityRetainedScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted insuranceID: String,
    ): StartTierFlowViewModel
  }
}

internal class StartTierChangePresenter(
  private val insuranceID: String,
  private val tierRepository: ChangeTierRepository,
  private val backstack: Backstack,
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
          currentState = Failure(GENERAL)
        },
        ifRight = { result ->
          val deflect = result.deflectOutput
          if (deflect != null) {
            currentState = StartTierChangeState.Deflect(
              title = deflect.title,
              message = deflect.message,
            )
            return@LaunchedEffect
          }
          val intent = result.intentOutput
          if (intent != null) {
            if (intent.quotes.isEmpty()) {
              currentState = Failure(QUOTES_ARE_EMPTY)
            } else {
              val parameters = InsuranceCustomizationParameters(
                insuranceId = insuranceID,
                activationDate = intent.activationDate,
                quoteIds = intent.quotes.map { it.id },
              )
              backstack.navigateAndPopUpTo<StartTierFlowKey>(ChooseTierKey(parameters), inclusive = true)
            }
          }
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

  data class Failure(val reason: FailureReason) : StartTierChangeState

  data class Deflect(
    val title: String,
    val message: String,
  ) : StartTierChangeState
}

internal sealed interface StartTierChangeEvent {
  data object Reload : StartTierChangeEvent
}

internal enum class FailureReason {
  GENERAL,
  QUOTES_ARE_EMPTY,
}
