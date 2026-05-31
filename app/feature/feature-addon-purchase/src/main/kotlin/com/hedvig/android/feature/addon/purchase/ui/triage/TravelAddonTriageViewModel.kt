package com.hedvig.android.feature.addon.purchase.ui.triage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.first

@AssistedInject
internal class TravelAddonTriageViewModel(
  getAddonBannerInfoUseCase: GetAddonBannerInfoUseCase,
  @Assisted addonBannerSource: AddonBannerSource,
) : MoleculeViewModel<TravelAddonTriageEvent, TravelAddonTriageState>(
    initialState = TravelAddonTriageState.Loading,
    presenter = TravelAddonTriagePresenter(getAddonBannerInfoUseCase, addonBannerSource),
  ) {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted addonBannerSource: AddonBannerSource,
    ): TravelAddonTriageViewModel
  }
}

internal class TravelAddonTriagePresenter(
  private val getAddonBannerInfoUseCase: GetAddonBannerInfoUseCase,
  private val addonBannerSource: AddonBannerSource,
) : MoleculePresenter<TravelAddonTriageEvent, TravelAddonTriageState> {
  @Composable
  override fun MoleculePresenterScope<TravelAddonTriageEvent>.present(
    lastState: TravelAddonTriageState,
  ): TravelAddonTriageState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      currentState = TravelAddonTriageState.Loading
      val result = getAddonBannerInfoUseCase.invoke(addonBannerSource)
      result.first().fold(
        ifLeft = { _ ->
          currentState = TravelAddonTriageState.Failure(FailureReason.GENERAL)
        },
        ifRight = { travelBannerInfoList ->
          val travelBannerInfo = travelBannerInfoList.firstOrNull()
          currentState = if (travelBannerInfo == null || travelBannerInfo.eligibleInsurancesIds.isEmpty()) {
            if (addonBannerSource == AddonBannerSource.TRAVEL_DEEPLINK) {
              TravelAddonTriageState.Failure(FailureReason.NO_TRAVEL_ADDON_AVAILABLE)
            } else {
              TravelAddonTriageState.Failure(FailureReason.GENERAL)
            }
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
