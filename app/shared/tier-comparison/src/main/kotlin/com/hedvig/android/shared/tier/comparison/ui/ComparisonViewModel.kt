package com.hedvig.android.shared.tier.comparison.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.shared.tier.comparison.data.ComparisonData.CoverageLevel.CoveredItem.CoveredStatus.Covered
import com.hedvig.android.shared.tier.comparison.data.ComparisonData.CoverageLevel.CoveredItem.CoveredStatus.CoveredWithDescription
import com.hedvig.android.shared.tier.comparison.data.ComparisonData.CoverageLevel.CoveredItem.CoveredStatus.NotCovered
import com.hedvig.android.shared.tier.comparison.data.GetCoverageComparisonUseCase
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.shared.tier.comparison.ui.ComparisonEvent.Reload
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Failure
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Loading
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success.CoverageLevel
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success.CoverageLevel.ComparisonItem.CoveredStatus.Checkmark
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success.CoverageLevel.ComparisonItem.CoveredStatus.Description

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
      if (currentState == Failure) {
        currentState = Loading
      }
      val result = getCoverageComparisonUseCase.invoke(termsIds)
      currentState = result.fold(
        ifLeft = { Failure },
        ifRight = { comparisonData ->
          val initialTabIndex = comparisonData.coverageLevels.indexOfFirst { it.termsVersion == selectedTermVersion }
          Success(
            coverageLevels = comparisonData.coverageLevels.map { coverageLevel ->
              CoverageLevel(
                title = coverageLevel.displayNameTier,
                items = coverageLevel
                  .coveredItems
                  .filterNot { it.coveredStatus == NotCovered }
                  .map { coveredItem ->
                    Success.CoverageLevel.ComparisonItem(
                      coveredItem.title,
                      coveredItem.description,
                      when (coveredItem.coveredStatus) {
                        Covered -> Checkmark
                        is CoveredWithDescription -> Description(coveredItem.coveredStatus.coverageText)
                        NotCovered -> error("Must be filtered out from the UI completely")
                      },
                    )
                  },
              )
            },
            initialTabIndex = initialTabIndex,
          )
        },
      )
    }

    return currentState
  }
}

sealed interface ComparisonState {
  data object Loading : ComparisonState

  data object Failure : ComparisonState

  data class Success(
    val coverageLevels: List<CoverageLevel>,
    val initialTabIndex: Int,
  ) : ComparisonState {
    data class CoverageLevel(
      val title: String,
      val items: List<ComparisonItem>,
    ) {
      data class ComparisonItem(
        val title: String,
        val description: String,
        val coveredStatus: CoveredStatus,
      ) {
        sealed interface CoveredStatus {
          data object Checkmark : CoveredStatus

          data class Description(val description: String) : CoveredStatus
        }
      }
    }
  }
}

sealed interface ComparisonEvent {
  data object Reload : ComparisonEvent
}
