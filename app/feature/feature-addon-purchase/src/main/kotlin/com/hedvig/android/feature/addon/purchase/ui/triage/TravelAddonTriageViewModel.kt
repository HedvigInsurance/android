package com.hedvig.android.feature.addon.purchase.ui.triage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.TravelAddonBannerInfo
import com.hedvig.android.data.addons.data.TravelAddonBannerSource
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.first

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
      val result = getTravelAddonBannerInfoUseCase.invoke(TravelAddonBannerSource.DEEPLINK)
      result.first().fold(
        ifLeft = { left: ErrorMessage ->
          currentState = TravelAddonTriageState.Failure(FailureReason.GENERAL)
        },
        ifRight = { travelBannerInfo: TravelAddonBannerInfo? ->
          currentState = if (travelBannerInfo == null || travelBannerInfo.eligibleInsurancesIds.isEmpty()) {
            TravelAddonTriageState.Failure(FailureReason.NO_TRAVEL_ADDON_AVAILABLE)
          } else {
            TravelAddonTriageState.Success(travelBannerInfo.eligibleInsurancesIds)
          }
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        TravelAddonTriageEvent.Reload -> loadIteration++
      }
    }

    return currentState
  }
}

internal sealed interface TravelAddonTriageState {
  data object Loading : TravelAddonTriageState

  data class Success(
    val insuranceIds: List<String>,
  ) : TravelAddonTriageState

  data class Failure(val reason: FailureReason) : TravelAddonTriageState
}

internal sealed interface TravelAddonTriageEvent {
  data object Reload : TravelAddonTriageEvent
}

internal enum class FailureReason {
  GENERAL,
  NO_TRAVEL_ADDON_AVAILABLE,
}
