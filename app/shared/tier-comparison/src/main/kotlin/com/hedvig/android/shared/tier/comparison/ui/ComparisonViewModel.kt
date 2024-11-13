package com.hedvig.android.shared.tier.comparison.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either.Left
import arrow.core.Either.Right
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.shared.tier.comparison.data.ComparisonData
import com.hedvig.android.shared.tier.comparison.data.GetCoverageComparisonUseCase
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.shared.tier.comparison.ui.ComparisonEvent.Reload
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Failure
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Loading
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success

class ComparisonViewModel(
  comparisonParameters: ComparisonParameters,
  getCoverageComparisonUseCase: GetCoverageComparisonUseCase,
) : MoleculeViewModel<ComparisonEvent, ComparisonState>(
    initialState = Loading,
    presenter = ComparisonPresenter(
      termsIds = comparisonParameters.termsIds,
      getCoverageComparisonUseCase = getCoverageComparisonUseCase,
      selectedTermVersion = comparisonParameters.selectedTermsVersion,
    ),
  )

private class ComparisonPresenter(
  private val termsIds: List<String>,
  private val selectedTermVersion: String?,
  private val getCoverageComparisonUseCase: GetCoverageComparisonUseCase,
) : MoleculePresenter<ComparisonEvent, ComparisonState> {
  @Composable
  override fun MoleculePresenterScope<ComparisonEvent>.present(lastState: ComparisonState): ComparisonState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        Reload -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      val result = getCoverageComparisonUseCase.invoke(termsIds)
      currentState = when (result) {
        is Left -> {
          Failure
        }

        is Right -> {
          val selectedColumn = result.value.columns.firstOrNull { it.termsVersion == selectedTermVersion }
          val selectedIndex = selectedColumn?.let {
            result.value.columns.indexOf(it)
          }
          Success(result.value, selectedIndex)
        }
      }
    }

    return currentState
  }
}

sealed interface ComparisonState {
  data object Loading : ComparisonState

  data object Failure : ComparisonState

  data class Success(
    val comparisonData: ComparisonData,
    val selectedColumnIndex: Int?,
  ) : ComparisonState
}

sealed interface ComparisonEvent {
  data object Reload : ComparisonEvent
}
