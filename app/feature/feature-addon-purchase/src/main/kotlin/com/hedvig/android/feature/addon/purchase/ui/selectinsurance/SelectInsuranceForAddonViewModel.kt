package com.hedvig.android.feature.addon.purchase.ui.selectinsurance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCase
import com.hedvig.android.feature.addon.purchase.data.InsuranceForAddon
import com.hedvig.android.feature.addon.purchase.navigation.CustomizeAddonKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey

@AssistedInject
internal class SelectInsuranceForAddonViewModel(
  @Assisted ids: List<String>,
  @Assisted preselectedAddonDisplayNames: List<String>,
  getInsuranceForTravelAddonUseCase: GetInsuranceForTravelAddonUseCase,
  backstack: Backstack,
) : MoleculeViewModel<SelectInsuranceForAddonEvent, SelectInsuranceForAddonState>(
    initialState = SelectInsuranceForAddonState.Loading,
    presenter = SelectInsuranceForAddonPresenter(
      ids = ids,
      preselectedAddonDisplayNames = preselectedAddonDisplayNames,
      getInsuranceForTravelAddonUseCase = getInsuranceForTravelAddonUseCase,
      backstack = backstack,
    ),
  ) {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(ActivityRetainedScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted ids: List<String>,
      @Assisted preselectedAddonDisplayNames: List<String>,
    ): SelectInsuranceForAddonViewModel
  }
}

internal class SelectInsuranceForAddonPresenter(
  private val ids: List<String>,
  private val preselectedAddonDisplayNames: List<String>,
  private val getInsuranceForTravelAddonUseCase: GetInsuranceForTravelAddonUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<SelectInsuranceForAddonEvent, SelectInsuranceForAddonState> {
  @Composable
  override fun MoleculePresenterScope<SelectInsuranceForAddonEvent>.present(
    lastState: SelectInsuranceForAddonState,
  ): SelectInsuranceForAddonState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    CollectEvents { event ->
      when (event) {
        SelectInsuranceForAddonEvent.Reload -> {
          loadIteration++
        }

        is SelectInsuranceForAddonEvent.SelectInsurance -> {
          val state = currentState as? SelectInsuranceForAddonState.Success ?: return@CollectEvents
          currentState = state.copy(currentlySelected = event.selected)
        }

        is SelectInsuranceForAddonEvent.SubmitSelected -> {
          val state = currentState as? SelectInsuranceForAddonState.Success ?: return@CollectEvents
          currentState = state.copy(currentlySelected = event.selected)
          backstack.add(CustomizeAddonKey(event.selected.id, preselectedAddonDisplayNames))
        }
      }
    }

    LaunchedEffect(loadIteration) {
      currentState = SelectInsuranceForAddonState.Loading
      if (ids.isEmpty()) {
        // should be impossible btw
        currentState = SelectInsuranceForAddonState.Failure
      } else if (ids.size == 1) {
        // should be impossible: we reroute earlier in the navigation entries
        backstack.add(CustomizeAddonKey(ids[0], preselectedAddonDisplayNames))
      } else {
        getInsuranceForTravelAddonUseCase.invoke(ids).collect { result ->
          result.fold(
            ifLeft = {
              currentState = SelectInsuranceForAddonState.Failure
            },
            ifRight = { loadedInsurances ->
              currentState = SelectInsuranceForAddonState.Success(
                listOfInsurances = loadedInsurances,
                currentlySelected = null,
              )
            },
          )
        }
      }
    }
    return currentState
  }
}

internal sealed interface SelectInsuranceForAddonState {
  data object Loading : SelectInsuranceForAddonState

  data class Success(
    val listOfInsurances: List<InsuranceForAddon>,
    val currentlySelected: InsuranceForAddon?,
  ) : SelectInsuranceForAddonState

  data object Failure : SelectInsuranceForAddonState
}

internal sealed interface SelectInsuranceForAddonEvent {
  data object Reload : SelectInsuranceForAddonEvent

  data class SelectInsurance(val selected: InsuranceForAddon) : SelectInsuranceForAddonEvent

  data class SubmitSelected(val selected: InsuranceForAddon) : SelectInsuranceForAddonEvent
}
