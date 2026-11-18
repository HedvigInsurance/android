package com.hedvig.android.feature.change.mileage.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.change.mileage.data.CommitMileageChangeUseCase
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageFailureKey
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageKey
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageSuccessKey
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageSummaryParameters
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ChangeMileageSummaryViewModel(
  @Assisted params: ChangeMileageSummaryParameters,
  commitMileageChangeUseCase: CommitMileageChangeUseCase,
  backstack: Backstack,
) : MoleculeViewModel<ChangeMileageSummaryEvent, ChangeMileageSummaryUiState>(
    initialState = ChangeMileageSummaryUiState(params, isSubmitting = false),
    presenter = ChangeMileageSummaryPresenter(
      params = params,
      commitMileageChangeUseCase = commitMileageChangeUseCase,
      backstack = backstack,
    ),
  )

internal class ChangeMileageSummaryPresenter(
  private val params: ChangeMileageSummaryParameters,
  private val commitMileageChangeUseCase: CommitMileageChangeUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<ChangeMileageSummaryEvent, ChangeMileageSummaryUiState> {
  @Composable
  override fun MoleculePresenterScope<ChangeMileageSummaryEvent>.present(
    lastState: ChangeMileageSummaryUiState,
  ): ChangeMileageSummaryUiState {
    var submitIteration by remember { mutableIntStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        ChangeMileageSummaryEvent.Submit -> {
          if (!isSubmitting) submitIteration++
        }
      }
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration == 0) return@LaunchedEffect
      isSubmitting = true
      commitMileageChangeUseCase.invoke(params.quote.id).fold(
        ifLeft = {
          isSubmitting = false
          backstack.add(ChangeMileageFailureKey)
        },
        ifRight = {
          backstack.navigateAndPopUpTo<ChangeMileageKey>(
            ChangeMileageSuccessKey(
              newMileageDisplayName = params.quote.displayName,
              newMonthlyCost = params.quote.totalCost.monthlyNet,
            ),
            inclusive = true,
          )
        },
      )
    }

    return ChangeMileageSummaryUiState(params, isSubmitting)
  }
}

internal data class ChangeMileageSummaryUiState(
  val params: ChangeMileageSummaryParameters,
  val isSubmitting: Boolean,
)

internal sealed interface ChangeMileageSummaryEvent {
  data object Submit : ChangeMileageSummaryEvent
}
