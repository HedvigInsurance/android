package com.hedvig.android.feature.change.tier.ui.comparison

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either.Left
import arrow.core.Either.Right
import com.hedvig.android.feature.change.tier.data.ComparisonData
import com.hedvig.android.feature.change.tier.data.GetCoverageComparisonUseCase
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonEvent.Reload
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Failure
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Loading
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Success
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class ComparisonViewModel(
  termsIds: List<String>,
  selectedTermVersion: String?,
  getCoverageComparisonUseCase: GetCoverageComparisonUseCase,
) : MoleculeViewModel<ComparisonEvent, ComparisonState>(
    initialState = Loading,
    presenter = ComparisonPresenter(
      termsIds = termsIds,
      getCoverageComparisonUseCase = getCoverageComparisonUseCase,
    ),
  )

private class ComparisonPresenter(
  private val termsIds: List<String>,
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
          Success(result.value)
        }
      }
    }

    return currentState
  }
}

internal sealed interface ComparisonState {
  data object Loading : ComparisonState

  data object Failure : ComparisonState

  data class Success(
    val comparisonData: ComparisonData,
    val selectedColumnIndex: Int?) : ComparisonState
}

internal sealed interface ComparisonEvent {
  data object Reload : ComparisonEvent
}
