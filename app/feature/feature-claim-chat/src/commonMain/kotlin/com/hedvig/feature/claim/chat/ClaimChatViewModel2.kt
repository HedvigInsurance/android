package com.hedvig.feature.claim.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.feature.claim.chat.data.SubmitSelectUseCase
import com.hedvig.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.feature.claim.chat.data.SubmitTaskUseCase
import kotlin.String
import kotlinx.coroutines.launch

internal sealed interface ClaimChatEvent {
  data class Select(val stepId: String, val selectedId: String) : ClaimChatEvent
}

internal sealed interface ClaimChatUiState {
  data object Initializing : ClaimChatUiState
  data object FailedToStart : ClaimChatUiState
  data class ClaimChat(
    val claimIntentId: String,
    val steps: List<ClaimIntentStep>,
  ) : ClaimChatUiState
}

internal class ClaimChatViewModel2(
  sourceMessageId: String?,
  developmentFlow: Boolean,
  startClaimIntentUseCase: StartClaimIntentUseCase,
  getClaimIntentUseCase: GetClaimIntentUseCase,
  submitTaskUseCase: SubmitTaskUseCase,
  submitAudioRecordingUseCase: SubmitAudioRecordingUseCase,
  submitFormUseCase: SubmitFormUseCase,
  submitSelectUseCase: SubmitSelectUseCase,
  submitSummaryUseCase: SubmitSummaryUseCase,
) : MoleculeViewModel<ClaimChatEvent, ClaimChatUiState>(
  ClaimChatUiState.Initializing,
  ClaimChatPresenter2(
    sourceMessageId,
    developmentFlow,
    startClaimIntentUseCase,
    getClaimIntentUseCase,
    submitTaskUseCase,
    submitAudioRecordingUseCase,
    submitFormUseCase,
    submitSelectUseCase,
    submitSummaryUseCase,
  ),
)

internal class ClaimChatPresenter2(
  private val sourceMessageId: String?,
  private val developmentFlow: Boolean,
  private val startClaimIntentUseCase: StartClaimIntentUseCase,
  private val getClaimIntentUseCase: GetClaimIntentUseCase,
  private val submitTaskUseCase: SubmitTaskUseCase,
  private val submitAudioRecordingUseCase: SubmitAudioRecordingUseCase,
  private val submitFormUseCase: SubmitFormUseCase,
  private val submitSelectUseCase: SubmitSelectUseCase,
  private val submitSummaryUseCase: SubmitSummaryUseCase,
) : MoleculePresenter<ClaimChatEvent, ClaimChatUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimChatEvent>.present(
    lastState: ClaimChatUiState,
  ): ClaimChatUiState {
    var initializing by remember { mutableStateOf(lastState !is ClaimChatUiState.ClaimChat) }
    var failedToStart by remember { mutableStateOf(lastState is ClaimChatUiState.FailedToStart) }
    val steps = remember {
      mutableStateListOf(*((lastState as? ClaimChatUiState.ClaimChat)?.steps ?: emptyList()).toTypedArray())
    }
    var claimIntentId by remember { mutableStateOf((lastState as? ClaimChatUiState.ClaimChat)?.claimIntentId) }
    var submittingStep by remember { mutableStateOf(false) }

    val currentStep by remember {
      derivedStateOf { steps.lastOrNull() }
    }

    if (initializing) {
      LaunchedEffect(Unit) {
        startClaimIntentUseCase
          .invoke(sourceMessageId, developmentFlow)
          .fold(
            ifLeft = { failedToStart = true },
            ifRight = { claimIntent ->
              Snapshot.withMutableSnapshot {
                initializing = false
                failedToStart = false
                claimIntentId = claimIntent.id
                steps.clear()
                steps.add(claimIntent.step)
              }
            },
          )
      }
    }

    ObserveIncompleteTaskEffect(getClaimIntentUseCase, currentStep, { claimIntentId }, steps)
    SubmitCompleteTaskEffect(submitTaskUseCase, currentStep, steps)

    CollectEvents { event ->
      logcat { "ClaimChatPresenter2 received event: $event" }
      when (event) {
        is ClaimChatEvent.Select -> {
          launch {
            submitSelectUseCase
              .invoke(event.stepId, event.selectedId)
              .fold(
                ifLeft = { error("todo left submitSelectUseCase") },
                ifRight = { claimIntent ->
                  Snapshot.withMutableSnapshot {
                    steps.removeLastIf { it.stepContent is StepContent.Task }
                    steps.add(claimIntent.step)
                  }
                },
              )
          }
        }
      }
    }

    return when {
      initializing -> ClaimChatUiState.Initializing
      failedToStart -> ClaimChatUiState.FailedToStart
      claimIntentId != null -> ClaimChatUiState.ClaimChat(claimIntentId!!, steps)
      else -> error("")
    }
  }
}

private fun <T> MutableList<T>.removeLastIf(predicate: (T) -> Boolean) {
  val last = lastOrNull() ?: return
  if (predicate(last)) {
    removeLast()
  }
}

@Composable
private fun ObserveIncompleteTaskEffect(
  getClaimIntentUseCase: GetClaimIntentUseCase,
  currentStep: ClaimIntentStep?,
  claimIntentId: () -> String?,
  steps: SnapshotStateList<ClaimIntentStep>,
) {
  val isIncompleteTask = (currentStep?.stepContent as? StepContent.Task)?.isCompleted?.not() == true
  LaunchedEffect(isIncompleteTask) {
    if (!isIncompleteTask) return@LaunchedEffect
    getClaimIntentUseCase
      .invoke(claimIntentId()!!)
      .collect { result ->
        result.fold(
          ifLeft = { error("handle getClaimIntent error") },
          ifRight = { claimIntent ->
            if (claimIntent.step.stepContent !is StepContent.Task) return@collect
            Snapshot.withMutableSnapshot {
              val previousTask = steps.find { it.id == claimIntent.step.id }
              steps.remove(previousTask)
              steps.add(
                claimIntent.step.copy(
                  stepContent = claimIntent.step.stepContent.copy(
                    descriptions = buildList {
                      addAll((previousTask?.stepContent as? StepContent.Task)?.descriptions.orEmpty())
                      addAll(claimIntent.step.stepContent.descriptions)
                    },
                  ),
                ),
              )
            }
          },
        )
      }
  }
}

@Composable
private fun SubmitCompleteTaskEffect(
  submitTaskUseCase: SubmitTaskUseCase,
  currentStep: ClaimIntentStep?,
  steps: SnapshotStateList<ClaimIntentStep>,
) {
  val isCompleteTask = (currentStep?.stepContent as? StepContent.Task)?.isCompleted == true
  LaunchedEffect(isCompleteTask) {
    if (!isCompleteTask) return@LaunchedEffect
    submitTaskUseCase
      .invoke(currentStep.id)
      .fold(
        ifLeft = { error("todo left submitTaskUseCase") },
        ifRight = { claimIntent ->
          Snapshot.withMutableSnapshot {
            steps.remove(currentStep)
            steps.add(claimIntent.step)
          }
        },
      )
  }
}
