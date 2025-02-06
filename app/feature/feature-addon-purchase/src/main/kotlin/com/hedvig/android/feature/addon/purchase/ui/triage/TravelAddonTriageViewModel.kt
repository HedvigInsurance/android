package com.hedvig.android.feature.addon.purchase.ui.triage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.TravelAddonBannerSource
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class TravelAddonTriageViewModel(
  getTravelAddonBannerInfoUseCase: GetTravelAddonBannerInfoUseCase,
) : MoleculeViewModel<TravelAddonTriageEvent, TravelAddonTriageState>(
  initialState = TravelAddonTriageState.Loading,
  presenter = TravelAddonTriagePresenter(getTravelAddonBannerInfoUseCase),
)

internal class TravelAddonTriagePresenter(
  private val getTravelAddonBannerInfoUseCase: GetTravelAddonBannerInfoUseCase,
) : MoleculePresenter<TravelAddonTriageEvent, TravelAddonTriageState> {
  @Composable
  override fun MoleculePresenterScope<TravelAddonTriageEvent>.present(
    lastState: TravelAddonTriageState,
  ): TravelAddonTriageState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    LaunchedEffect(loadIteration) {
      currentState = TravelAddonTriageState.Loading
      val result = getTravelAddonBannerInfoUseCase.invoke(TravelAddonBannerSource.TRAVEL_CERTIFICATES)
      val travelAddonBanner = result.getOrNull()
    // todo: here not sure what we are supposed to put as a source but the travel certificates one is broader, so.

//      tierRepository.startChangeTierIntentAndGetQuotesId(insuranceID, ChangeTierCreateSource.SELF_SERVICE).fold(
//        ifLeft = { left: ErrorMessage ->
//          logcat(WARN) { "Start TierFlow failed with: $left" }
//          currentState = TravelAddonTriageState.Failure(GENERAL)
//        },
//        ifRight = { result ->
//          if (result.quotes.isEmpty()) {
//            currentState = TravelAddonTriageState.Failure(QUOTES_ARE_EMPTY)
//          } else {
//            val parameters = InsuranceCustomizationParameters(
//              insuranceId = insuranceID,
//              activationDate = result.activationDate,
//              quoteIds = result.quotes.map { it.id },
//            )
//            currentState = TravelAddonTriageState.Success(parameters)
//          }
//        },
//      )
    }
    CollectEvents { event ->
      when (event) {
        Reload -> loadIteration++
      }
    }

    return currentState
  }
}

internal sealed interface TravelAddonTriageState {
  data object Loading : TravelAddonTriageState

  data class Success(
    val paramsToNavigate: InsuranceCustomizationParameters,
  ) : TravelAddonTriageState

  data class Failure(val reason: FailureReason) : TravelAddonTriageState
}

internal sealed interface TravelAddonTriageEvent {
  data object Reload : TravelAddonTriageEvent
}

internal enum class FailureReason {
  GENERAL,
  QUOTES_ARE_EMPTY,
}
