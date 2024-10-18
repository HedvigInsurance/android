package com.hedvig.android.feature.movingflow.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.feature.movingflow.MovingFlowDestinations.Summary
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveMtaQuote
import com.hedvig.android.feature.movingflow.storage.MovingFlowStorage
import com.hedvig.android.feature.movingflow.ui.summary.SummaryEvent.ConfirmChanges
import com.hedvig.android.feature.movingflow.ui.summary.SummaryEvent.DismissSubmissionError
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.SubmitError
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Loading
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.MissingOngoingMovingFlow
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.NoMatchingQuoteFound
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.MoveIntentV2CommitMutation

internal class SummaryViewModel(
  savedStateHandle: SavedStateHandle,
  movingFlowStorage: MovingFlowStorage,
  apolloClient: ApolloClient,
) : MoleculeViewModel<SummaryEvent, SummaryUiState>(
    Loading,
    SummaryPresenter(
      summaryRoute = savedStateHandle.toRoute<Summary>(),
      movingFlowStorage = movingFlowStorage,
      apolloClient = apolloClient,
    ),
  )

internal class SummaryPresenter(
  private val summaryRoute: Summary,
  private val movingFlowStorage: MovingFlowStorage,
  private val apolloClient: ApolloClient,
) : MoleculePresenter<SummaryEvent, SummaryUiState> {
  @Composable
  override fun MoleculePresenterScope<SummaryEvent>.present(lastState: SummaryUiState): SummaryUiState {
    var summaryInfo: SummaryInfoState by remember { mutableStateOf(SummaryInfoState.Loading) }
    var submitChangesError: SubmitError? by remember { mutableStateOf(null) }
    var submittingChangesForDate: LocalDate? by remember { mutableStateOf(null) }
    var navigateToFinishedScreenWithDate: LocalDate? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        ConfirmChanges -> {
          val submitForDate =
            (summaryInfo as? SummaryInfoState.Content)?.summaryInfo?.moveHomeQuote?.startDate ?: return@CollectEvents
          submittingChangesForDate = submitForDate
        }

        DismissSubmissionError -> {
          submittingChangesForDate = null
        }
      }
    }

    LaunchedEffect(Unit) {
      movingFlowStorage.movingFlowState().collect { movingFlowState ->
        val movingFlowQuotes = movingFlowState?.movingFlowQuotes
        if (movingFlowQuotes == null) {
          summaryInfo = SummaryInfoState.MissingOngoingMovingFlow
        } else {
          val matchingMoveHomeQuote = movingFlowQuotes.homeQuotes.firstOrNull { it.id == summaryRoute.homeQuoteId }
          if (matchingMoveHomeQuote == null) {
            summaryInfo = SummaryInfoState.NoMatchingQuoteFound
            return@collect
          }
          val moveMtaQuotes = movingFlowQuotes.mtaQuotes
          summaryInfo = SummaryInfoState.Content(
            SummaryInfo(
              moveHomeQuote = matchingMoveHomeQuote,
              moveMtaQuotes = moveMtaQuotes,
            ),
          )
        }
      }
    }

    val submittingChangesForDateValue = submittingChangesForDate
    if (submittingChangesForDateValue != null) {
      LaunchedEffect(submittingChangesForDateValue) {
        apolloClient
          .mutation(MoveIntentV2CommitMutation(summaryRoute.moveIntentId, summaryRoute.homeQuoteId))
          .safeExecute()
          .map { it.moveIntentCommit }
          .fold(
            ifLeft = {
              Snapshot.withMutableSnapshot {
                submittingChangesForDate = null
                submitChangesError = SubmitError.Generic
              }
            },
            ifRight = { moveIntentCommit ->
              val didSucceed = moveIntentCommit.moveIntent?.id != null
              if (!didSucceed) {
                val message = moveIntentCommit.userError?.message
                Snapshot.withMutableSnapshot {
                  submittingChangesForDate = null
                  submitChangesError = if (message != null) {
                    SubmitError.WithMessage(message)
                  } else {
                    SubmitError.Generic
                  }
                }
              } else {
                Snapshot.withMutableSnapshot {
                  submittingChangesForDate = null
                  navigateToFinishedScreenWithDate = submittingChangesForDateValue
                }
              }
            },
          )
      }
    }

    return when (val summaryInfoValue = summaryInfo) {
      SummaryInfoState.Loading -> Loading
      SummaryInfoState.MissingOngoingMovingFlow -> MissingOngoingMovingFlow
      SummaryInfoState.NoMatchingQuoteFound -> NoMatchingQuoteFound
      is SummaryInfoState.Content -> Content(
        summaryInfo = summaryInfoValue.summaryInfo,
        isSubmitting = false,
        submitError = submitChangesError,
        navigateToFinishedScreenWithDate = navigateToFinishedScreenWithDate,
      )
    }
  }
}

private sealed interface SummaryInfoState {
  data object Loading : SummaryInfoState

  data object MissingOngoingMovingFlow : SummaryInfoState

  data object NoMatchingQuoteFound : SummaryInfoState

  data class Content(val summaryInfo: SummaryInfo) : SummaryInfoState
}

internal sealed interface SummaryUiState {
  data object Loading : SummaryUiState

  data object MissingOngoingMovingFlow : SummaryUiState

  data object NoMatchingQuoteFound : SummaryUiState

  data class Content(
    val summaryInfo: SummaryInfo,
    val isSubmitting: Boolean,
    val submitError: SubmitError?,
    val navigateToFinishedScreenWithDate: LocalDate?,
  ) : SummaryUiState {
    sealed interface SubmitError {
      data object Generic : SubmitError

      data class WithMessage(val message: String) : SubmitError
    }
  }
}

internal sealed interface SummaryEvent {
  data object ConfirmChanges : SummaryEvent

  data object DismissSubmissionError : SummaryEvent
}

internal data class SummaryInfo(
  val moveHomeQuote: MoveHomeQuote,
  val moveMtaQuotes: List<MoveMtaQuote>,
)
