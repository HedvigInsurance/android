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
import com.eygraber.uri.Uri
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.feature.claim.chat.data.AudioRecordingManager
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState.*
import com.hedvig.feature.claim.chat.data.ClaimIntent
import com.hedvig.feature.claim.chat.data.ClaimIntentId
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.FieldId
import com.hedvig.feature.claim.chat.data.FormSubmissionData
import com.hedvig.feature.claim.chat.data.FormSubmissionData.*
import com.hedvig.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepId
import com.hedvig.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.feature.claim.chat.data.SubmitFileUploadUseCase
import com.hedvig.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.feature.claim.chat.data.SubmitSelectUseCase
import com.hedvig.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.feature.claim.chat.data.SubmitTaskUseCase
import kotlin.String
import kotlinx.coroutines.launch

internal sealed interface ClaimChatEvent {
  sealed interface AudioRecording : ClaimChatEvent {
    val id: StepId

    data class SubmitAudioFile(override val id: StepId) : AudioRecording

    data class SubmitTextInput(override val id: StepId) : AudioRecording

    data class StartRecording(override val id: StepId) : AudioRecording

    data class StopRecording(override val id: StepId) : AudioRecording

    data class RedoRecording(override val id: StepId) : AudioRecording

    data class ShowFreeText(override val id: StepId) : AudioRecording

    data class ShowAudioRecording(override val id: StepId) : AudioRecording
  }

  data class UpdateFreeText(val text: String?) : ClaimChatEvent

  data class Select(val id: StepId, val selectedId: String) : ClaimChatEvent
  data class Skip(val id: StepId) : ClaimChatEvent
  data class Form(val id: StepId, val formInputs: Map<FieldId, List<String?>>) : ClaimChatEvent

  data class FileUpload(val id: StepId, val fileUri: Uri?, val uploadUri: String) : ClaimChatEvent

  data object OpenFreeTextOverlay : ClaimChatEvent

  data object CloseFreeChatOverlay : ClaimChatEvent
}

internal sealed interface ClaimChatUiState {
  data object Initializing : ClaimChatUiState

  data object FailedToStart : ClaimChatUiState

  data class ClaimChat(
    val claimIntentId: ClaimIntentId,
    val steps: List<ClaimIntentStep>,
    val currentStep: ClaimIntentStep?,
    val showFreeTextOverlay: Boolean,
    val freeText: String?,
    val outcome: ClaimIntentOutcome?,
  ) : ClaimChatUiState
}

internal class ClaimChatViewModel(
  developmentFlow: Boolean,
  startClaimIntentUseCase: StartClaimIntentUseCase,
  getClaimIntentUseCase: GetClaimIntentUseCase,
  submitTaskUseCase: SubmitTaskUseCase,
  submitAudioRecordingUseCase: SubmitAudioRecordingUseCase,
  submitFileUploadUseCase: SubmitFileUploadUseCase,
  submitFormUseCase: SubmitFormUseCase,
  submitSelectUseCase: SubmitSelectUseCase,
  submitSummaryUseCase: SubmitSummaryUseCase,
  audioRecordingManager: AudioRecordingManager,
) : MoleculeViewModel<ClaimChatEvent, ClaimChatUiState>(
    ClaimChatUiState.Initializing,
    ClaimChatPresenter(
      developmentFlow,
      startClaimIntentUseCase,
      getClaimIntentUseCase,
      submitTaskUseCase,
      submitAudioRecordingUseCase,
      submitFileUploadUseCase,
      submitFormUseCase,
      submitSelectUseCase,
      submitSummaryUseCase,
      audioRecordingManager,
    ),
  )

internal class ClaimChatPresenter(
  private val developmentFlow: Boolean,
  private val startClaimIntentUseCase: StartClaimIntentUseCase,
  private val getClaimIntentUseCase: GetClaimIntentUseCase,
  private val submitTaskUseCase: SubmitTaskUseCase,
  private val submitAudioRecordingUseCase: SubmitAudioRecordingUseCase,
  private val submitFileUploadUseCase: SubmitFileUploadUseCase,
  private val submitFormUseCase: SubmitFormUseCase,
  private val submitSelectUseCase: SubmitSelectUseCase,
  private val submitSummaryUseCase: SubmitSummaryUseCase,
  private val audioRecordingManager: AudioRecordingManager,
) : MoleculePresenter<ClaimChatEvent, ClaimChatUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimChatEvent>.present(lastState: ClaimChatUiState): ClaimChatUiState {
    var initializing by remember { mutableStateOf(lastState !is ClaimChatUiState.ClaimChat) }
    var failedToStart by remember { mutableStateOf(lastState is ClaimChatUiState.FailedToStart) }
    val steps = remember {
      mutableStateListOf(*((lastState as? ClaimChatUiState.ClaimChat)?.steps ?: emptyList()).toTypedArray())
    }
    var outcome by remember {
      mutableStateOf((lastState as? ClaimChatUiState.ClaimChat)?.outcome)
    }
    var claimIntentId by remember { mutableStateOf((lastState as? ClaimChatUiState.ClaimChat)?.claimIntentId) }
    var submittingStep by remember { mutableStateOf(false) }
    val currentStep by remember {
      derivedStateOf { steps.lastOrNull() }
    }
    var showFreeTextOverlay by remember { mutableStateOf(false) }
    var freeText by remember { mutableStateOf<String?>(null) }
    val setOutcome: (ClaimIntentOutcome) -> Unit = { outcome = it }

    if (initializing) {
      LaunchedEffect(Unit) {
        startClaimIntentUseCase
          .invoke(developmentFlow)
          .fold(
            ifLeft = { failedToStart = true },
            ifRight = { claimIntent ->
              Snapshot.withMutableSnapshot {
                initializing = false
                failedToStart = false
                claimIntentId = claimIntent.id
                steps.clear()
                when (val next = claimIntent.next) {
                  is ClaimIntent.Next.Outcome -> setOutcome(next.claimIntentOutcome)
                  is ClaimIntent.Next.Step -> steps.add(next.claimIntentStep)
                }
              }
            },
          )
      }
    }

    ObserveIncompleteTaskEffect(getClaimIntentUseCase, currentStep, { claimIntentId }, steps)
    SubmitCompleteTaskEffect(submitTaskUseCase, currentStep) { claimIntent ->
      handleNext(steps, setOutcome, claimIntent.next)
    }

    CollectEvents { event ->
      logcat { "ClaimChatPresenter received event: $event" }
      when (event) {
        is ClaimChatEvent.Select -> {
          val currentStepContent = currentStep?.stepContent as? StepContent.ContentSelect ?: return@CollectEvents
          val currentStepState = currentStep ?: return@CollectEvents
          launch {
            steps.remove(currentStepState)
            steps.add(currentStepState.copy(stepContent = currentStepContent.copy(
              selectedOptionId = event.selectedId
            ))) //todo: check
            submitSelectUseCase
              .invoke(event.id, event.selectedId)
              .fold(
                ifLeft = { error("todo left submitSelectUseCase") },
                ifRight = { claimIntent ->
                  handleNext(steps, setOutcome, claimIntent.next)
                },
              )
          }
        }

        is ClaimChatEvent.AudioRecording -> {
          when (event) {
            is ClaimChatEvent.AudioRecording.SubmitAudioFile -> {
              val recordedFile = audioRecordingManager.getRecordedFile()
              if (recordedFile == null) {
                logcat { "No recorded file available" }
                return@CollectEvents
              }
              val stepContent = steps.find { it.id == event.id }?.stepContent as? StepContent.AudioRecording
              if (stepContent == null) {
                logcat { "Step content not found or not AudioRecording" }
                return@CollectEvents
              }
              launch {
                submitAudioRecordingUseCase
                  .invoke(event.id, recordedFile, stepContent.uploadUri)
                  .fold(
                    ifLeft = { error("todo left submitAudioRecordingUseCase audio") },
                    ifRight = { claimIntent ->
                      audioRecordingManager.cleanup()
                      handleNext(steps, setOutcome, claimIntent.next)
                    },
                  )
              }
            }

            is ClaimChatEvent.AudioRecording.SubmitTextInput -> {
              val freeTextInput = freeText ?: return@CollectEvents
              launch {
                submitAudioRecordingUseCase
                  .invoke(event.id, freeTextInput)
                  .fold(
                    ifLeft = { error("todo left submitAudioRecordingUseCase text: $it") },
                    ifRight = { claimIntent ->
                      handleNext(steps, setOutcome, claimIntent.next)
                    },
                  )
              }
            }

            is ClaimChatEvent.AudioRecording.StartRecording -> {
              audioRecordingManager.startRecording { recordingState ->
                Snapshot.withMutableSnapshot {
                  val stepToUpdate = steps.find { it.id == event.id } ?: return@withMutableSnapshot
                  val stepContent = stepToUpdate.stepContent as? StepContent.AudioRecording ?: return@withMutableSnapshot
                  val index = steps.indexOf(stepToUpdate)
                  if (index >= 0) {
                    steps[index] = stepToUpdate.copy(
                      stepContent = stepContent.copy(recordingState = recordingState),
                    )
                  }
                }
              }
            }

            is ClaimChatEvent.AudioRecording.StopRecording -> {
              audioRecordingManager.stopRecording { playbackState ->
                Snapshot.withMutableSnapshot {
                  val stepToUpdate = steps.find { it.id == event.id } ?: return@withMutableSnapshot
                  val stepContent = stepToUpdate.stepContent as? StepContent.AudioRecording ?: return@withMutableSnapshot
                  val index = steps.indexOf(stepToUpdate)
                  if (index >= 0) {
                    steps[index] = stepToUpdate.copy(
                      stepContent = stepContent.copy(recordingState = playbackState),
                    )
                  }
                }
              }
            }

            is ClaimChatEvent.AudioRecording.RedoRecording -> {
              audioRecordingManager.reset()
              Snapshot.withMutableSnapshot {
                val currentStepState = steps.find { it.id == event.id } ?: return@withMutableSnapshot
                val currentStepContent = currentStepState.stepContent as? StepContent.AudioRecording
                  ?: return@withMutableSnapshot
                val index = steps.indexOf(currentStepState)
                if (index >= 0) {
                  steps[index] = currentStepState.copy(
                    stepContent = currentStepContent.copy(
                      recordingState = AudioRecording.NotRecording,
                    ),
                  )
                }
              }
            }

            is ClaimChatEvent.AudioRecording.ShowFreeText -> {
              Snapshot.withMutableSnapshot {
                val currentStepState = steps.find { it.id == event.id } ?: return@withMutableSnapshot
                val currentStepContent = currentStepState.stepContent as? StepContent.AudioRecording ?: return@withMutableSnapshot
                val index = steps.indexOf(currentStepState)
                if (index >= 0) {
                  steps[index] = currentStepState.copy(
                    stepContent = currentStepContent.copy(
                      recordingState = FreeTextDescription(
                        showOverlay = showFreeTextOverlay,
                        errorType = null,
                      ),
                    ),
                  )
                }
              }
            }

            is ClaimChatEvent.AudioRecording.ShowAudioRecording -> {
              Snapshot.withMutableSnapshot {
                val currentStepState = steps.find { it.id == event.id } ?: return@withMutableSnapshot
                val currentStepContent = currentStepState.stepContent as? StepContent.AudioRecording ?: return@withMutableSnapshot
                val index = steps.indexOf(currentStepState)
                if (index >= 0) {
                  steps[index] = currentStepState.copy(
                    stepContent = currentStepContent.copy(
                      recordingState = AudioRecording.NotRecording,
                    ),
                  )
                }
              }
            }
          }
        }

        is ClaimChatEvent.Form -> {
          launch {
            submitFormUseCase
              .invoke(
                FormSubmissionData(
                  event.id,
                  event.formInputs.map { (fieldId, values) ->
                    Field(fieldId, values)
                  },
                ),
              )
              .fold(
                ifLeft = { error("submitFormUseCase error: $it") },
                ifRight = { claimIntent ->
                  handleNext(steps, setOutcome, claimIntent.next)
                },
              )
          }
        }

        is ClaimChatEvent.FileUpload -> {
          val fileUri = event.fileUri ?: return@CollectEvents
          launch {
            submitFileUploadUseCase
              .invoke(
                stepId = event.id,
                fileUri = fileUri,
                uploadUrl = event.uploadUri,
              )
              .fold(
                ifLeft = { error("todo left submitFileUploadUseCase $it") },
                ifRight = { claimIntent ->
                  handleNext(steps, setOutcome, claimIntent.next)
                },
              )
          }
        }

        ClaimChatEvent.CloseFreeChatOverlay -> showFreeTextOverlay = false
        ClaimChatEvent.OpenFreeTextOverlay -> showFreeTextOverlay = true
        is ClaimChatEvent.Skip -> {
          // TODO: Implement skip logic
        }

        is ClaimChatEvent.UpdateFreeText -> {
          freeText = event.text
        }
      }
    }

    return when {
      initializing -> ClaimChatUiState.Initializing
      failedToStart -> ClaimChatUiState.FailedToStart
      claimIntentId != null -> ClaimChatUiState.ClaimChat(
        claimIntentId = claimIntentId!!,
        steps = steps,
        currentStep = currentStep,
        outcome = outcome,
        showFreeTextOverlay = showFreeTextOverlay,
        freeText = freeText
      )
      else -> error("")
    }
  }
}

@Composable
private fun ObserveIncompleteTaskEffect(
  getClaimIntentUseCase: GetClaimIntentUseCase,
  currentStep: ClaimIntentStep?,
  claimIntentId: () -> ClaimIntentId?,
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
          ifRight = { taskStepContent ->
            Snapshot.withMutableSnapshot {
              val previousTask = steps.find { it.id == taskStepContent.step.id }
              steps.remove(previousTask)
              steps.add(
                taskStepContent.step.copy(
                  stepContent = taskStepContent.task.copy(
                    descriptions = buildList {
                      addAll((previousTask?.stepContent as? StepContent.Task)?.descriptions.orEmpty())
                      addAll(taskStepContent.task.descriptions)
                    }.distinct(),
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
  onSuccess: (ClaimIntent) -> Unit,
) {
  val isCompleteTask = (currentStep?.stepContent as? StepContent.Task)?.isCompleted == true
  LaunchedEffect(isCompleteTask) {
    if (!isCompleteTask) return@LaunchedEffect
    submitTaskUseCase
      .invoke(currentStep.id.value)
      .fold(
        ifLeft = { error("todo left submitTaskUseCase") },
        ifRight = { claimIntent ->
          onSuccess(claimIntent)
        },
      )
  }
}

private fun handleNext(
  steps: SnapshotStateList<ClaimIntentStep>,
  setOutcome: (outcome: ClaimIntentOutcome) -> Unit,
  next: ClaimIntent.Next,
) {
  when (next) {
    is ClaimIntent.Next.Outcome -> {
      setOutcome(next.claimIntentOutcome)
    }
    is ClaimIntent.Next.Step -> {
      steps.replaceTaskWithNextStep(next.claimIntentStep)
    }
  }
}

private fun SnapshotStateList<ClaimIntentStep>.replaceTaskWithNextStep(step: ClaimIntentStep) {
  Snapshot.withMutableSnapshot {
    removeLastIf { it.stepContent is StepContent.Task }
    add(step)
  }
}

private fun <T> MutableList<T>.removeLastIf(predicate: (T) -> Boolean) {
  val last = lastOrNull() ?: return
  if (predicate(last)) {
    removeAt(this.lastIndex)
  }
}
