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
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.feature.claim.chat.data.AudioRecordingManager
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState.AudioRecording
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState.FreeTextDescription
import com.hedvig.feature.claim.chat.data.ClaimIntent
import com.hedvig.feature.claim.chat.data.ClaimIntentId
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.FieldId
import com.hedvig.feature.claim.chat.data.FormSubmissionData
import com.hedvig.feature.claim.chat.data.FormSubmissionData.Field
import com.hedvig.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.SkipStepUseCase
import com.hedvig.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepId
import com.hedvig.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.feature.claim.chat.data.SubmitFileUploadUseCase
import com.hedvig.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.feature.claim.chat.data.SubmitSelectUseCase
import com.hedvig.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.feature.claim.chat.data.SubmitTaskUseCase
import com.hedvig.feature.claim.chat.data.file.FileService
import kotlin.collections.emptyList
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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

  data class UpdateFieldAnswer(
    val stepId: StepId,
    val fieldId: FieldId,
    val answer: StepContent.Form.FieldOption?,
  ) : ClaimChatEvent

  data class Skip(val id: StepId) : ClaimChatEvent

  data class Regret(val id: StepId) : ClaimChatEvent
  data class FormSubmit(
    val stepId: StepId,
  ) : ClaimChatEvent

  data class AddFile(val id: StepId, val uri: String) : ClaimChatEvent

  data class RemoveFile(val id: StepId, val fileId: String) : ClaimChatEvent

  data class FileSubmit(val id: StepId) : ClaimChatEvent

  data object OpenFreeTextOverlay : ClaimChatEvent

  data object CloseFreeChatOverlay : ClaimChatEvent

  data object DismissErrorDialog : ClaimChatEvent

  data class SubmitClaim(val id: StepId) : ClaimChatEvent
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
    val errorSubmittingStep: ErrorMessage?,
    val currentContinueButtonLoading: Boolean = false,
    val currentSkipButtonLoading: Boolean = false,
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
  skipStepUseCase: SkipStepUseCase,
  fileService: FileService,
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
    skipStepUseCase,
    audioRecordingManager,
    fileService,
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
  private val skipStepUseCase: SkipStepUseCase,
  private val audioRecordingManager: AudioRecordingManager,
  private val fileService: FileService,
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
    var claimIntentId by remember {
      mutableStateOf(
        (lastState as? ClaimChatUiState.ClaimChat)?.claimIntentId,
      )
    }
    val currentStep by remember {
      derivedStateOf { steps.lastOrNull() }
    }
    var showFreeTextOverlay by remember { mutableStateOf(false) }
    var currentContinueButtonLoading by remember { mutableStateOf(false) }
    var currentSkipButtonLoading by remember { mutableStateOf(false) }
    var errorSubmittingStep by remember { mutableStateOf<ErrorMessage?>(null) }
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
      when (event) {
        is ClaimChatEvent.Select -> {
          currentContinueButtonLoading = true
          launch {
            submitSelectUseCase
              .invoke(
                id = event.id,
                selectedId = event.selectedId,
              )
              .fold(
                ifLeft = {
                  errorSubmittingStep = it
                  currentContinueButtonLoading = false
                  logcat { "ClaimChatEvent.Select error: $it" }
                },
                ifRight = { claimIntent ->
                  steps.updateStep<StepContent.ContentSelect>(event.id) { step, content ->
                    step.copy(
                      stepContent = content.copy(
                        selectedOptionId = event.selectedId,
                      ),
                    )
                  }
                  currentContinueButtonLoading = false
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
              currentContinueButtonLoading = true
              launch {
                submitAudioRecordingUseCase
                  .invoke(event.id, recordedFile, stepContent.uploadUri)
                  .fold(
                    ifLeft = {
                      errorSubmittingStep = it
                      currentContinueButtonLoading = false
                      logcat { "ClaimChatEvent.AudioRecording.SubmitAudioFile error: $it" }
                    },
                    ifRight = { claimIntent ->
                      audioRecordingManager.cleanup()
                      currentContinueButtonLoading = false
                      handleNext(steps, setOutcome, claimIntent.next)
                    },
                  )
              }
            }

            is ClaimChatEvent.AudioRecording.SubmitTextInput -> {
              val freeTextInput = freeText ?: return@CollectEvents
              currentContinueButtonLoading = true
              launch {
                submitAudioRecordingUseCase
                  .invoke(event.id, freeTextInput)
                  .fold(
                    ifLeft = {
                      errorSubmittingStep = it
                      currentContinueButtonLoading = false
                      logcat { "ClaimChatEvent.AudioRecording.SubmitTextInput error: $it" }
                    },
                    ifRight = { claimIntent ->
                      currentContinueButtonLoading = false
                      handleNext(steps, setOutcome, claimIntent.next)
                    },
                  )
              }
            }

            is ClaimChatEvent.AudioRecording.StartRecording -> {
              audioRecordingManager.startRecording { recordingState ->
                steps.updateStep<StepContent.AudioRecording>(event.id) { step, content ->
                  step.copy(stepContent = content.copy(recordingState = recordingState))
                }
              }
            }

            is ClaimChatEvent.AudioRecording.StopRecording -> {
              audioRecordingManager.stopRecording { playbackState ->
                steps.updateStep<StepContent.AudioRecording>(event.id) { step, content ->
                  step.copy(stepContent = content.copy(recordingState = playbackState))
                }
              }
            }

            is ClaimChatEvent.AudioRecording.RedoRecording -> {
              audioRecordingManager.reset()
              steps.updateStep<StepContent.AudioRecording>(event.id) { step, content ->
                step.copy(stepContent = content.copy(recordingState = AudioRecording.NotRecording))
              }
            }

            is ClaimChatEvent.AudioRecording.ShowFreeText -> {
              steps.updateStep<StepContent.AudioRecording>(event.id) { step, content ->
                step.copy(
                  stepContent = content.copy(
                    recordingState = FreeTextDescription(
                      showOverlay = showFreeTextOverlay,
                      errorType = null,
                    ),
                  ),
                )
              }
            }

            is ClaimChatEvent.AudioRecording.ShowAudioRecording -> {
              steps.updateStep<StepContent.AudioRecording>(event.id) { step, content ->
                step.copy(stepContent = content.copy(recordingState = AudioRecording.NotRecording))
              }
            }
          }
        }

        is ClaimChatEvent.FormSubmit -> {
          Snapshot.withMutableSnapshot {
            val currentContent = steps.firstOrNull { it.id == event.stepId }?.stepContent as? StepContent.Form
              ?: return@CollectEvents
            val fieldsToSubmit = currentContent.fields.map { field ->
              when (field.type) {
                StepContent.Form.FieldType.DATE -> {
                  val selectedDateString =
                    field.datePickerUiState?.datePickerState?.selectedDateMillis?.let { selectedDateMillis ->
                      Instant.fromEpochMilliseconds(selectedDateMillis).toLocalDateTime(TimeZone.UTC).date
                    }?.toString()
                  val stepToUpdate = steps.find { it.id == event.stepId } ?: return@withMutableSnapshot
                  val index = steps.indexOf(stepToUpdate)
                  if (index >= 0 && selectedDateString!=null) {
                    steps[index] = stepToUpdate.copy(
                      stepContent = currentContent.copy(
                        fields = currentContent.fields.map { existingField ->
                          if (existingField.id==field.id) existingField.copy(
                            selectedOptions = listOf(
                              StepContent.Form.FieldOption(
                                selectedDateString,
                                selectedDateString))
                          ) else existingField
                        }
                      ),
                    )
                  }
                  //TODO()
                  Field(
                    field.id,
                    listOf(selectedDateString)

                  )
                }
                else -> Field(
                  field.id,
                  field.selectedOptions
                    .map { selectedOption ->
                      selectedOption.value
                    },
                )

              }
            }
            currentContinueButtonLoading = true
            launch {
              submitFormUseCase
                .invoke(
                  FormSubmissionData(
                    event.stepId,
                    fieldsToSubmit,
                  ),
                )
                .fold(
                  ifLeft = {
                    currentContinueButtonLoading = false
                    errorSubmittingStep = it
                    logcat { "FormSubmit error: $it" }
                  },
                  ifRight = { claimIntent ->
                    currentContinueButtonLoading = false
                    handleNext(steps, setOutcome, claimIntent.next)
                  },
                )
            }
          }
        }

        is ClaimChatEvent.AddFile -> {
          Snapshot.withMutableSnapshot {
            val stepToUpdate = steps.find { it.id == event.id } ?: return@withMutableSnapshot
            val stepContent = stepToUpdate.stepContent as? StepContent.FileUpload ?: return@withMutableSnapshot
            if (event.uri in stepContent.localFiles.map { it.id }) {
              return@CollectEvents
            }
            try {
              val mimeType = fileService.getMimeType(event.uri)
              val name = fileService.getFileName(event.uri) ?: event.uri
              val localFile = UiFile(
                name = name,
                localPath = event.uri,
                mimeType = mimeType,
                id = event.uri,
                url = null,
              )
              val index = steps.indexOf(stepToUpdate)
              if (index >= 0) {
                steps[index] = stepToUpdate.copy(
                  stepContent = stepContent.copy(
                    localFiles = stepContent.localFiles + localFile,
                  ),
                )
              }
            } catch (e: Exception) {
              logcat { "ClaimChatEvent.AddFile error: $e" }
              errorSubmittingStep = ErrorMessage()
            }
          }
        }

        ClaimChatEvent.CloseFreeChatOverlay -> showFreeTextOverlay = false
        ClaimChatEvent.OpenFreeTextOverlay -> showFreeTextOverlay = true
        is ClaimChatEvent.Skip -> {
          val claimChatState = claimIntentId != null
          if (!claimChatState) return@CollectEvents
          currentSkipButtonLoading = true
          launch {
            skipStepUseCase.invoke(event.id)
              .fold(
                ifLeft = {
                  errorSubmittingStep = it
                  currentSkipButtonLoading = false
                  logcat { "ClaimChatEvent.Skip $it" }
                },
                ifRight = { claimIntent ->
                  steps.updateStep(event.id) { step -> step.clearContent() }
                  currentSkipButtonLoading = false
                  handleNext(steps, setOutcome, claimIntent.next)
                },
              )
          }
        }

        is ClaimChatEvent.UpdateFreeText -> {
          freeText = event.text
        }

        is ClaimChatEvent.SubmitClaim -> {
          currentContinueButtonLoading = true
          launch {
            submitSummaryUseCase
              .invoke(event.id)
              .fold(
                ifLeft = {
                  currentContinueButtonLoading = false
                  errorSubmittingStep = it
                  logcat { "SubmitClaim error: $it" }
                },
                ifRight = { claimIntent ->
                  currentContinueButtonLoading = false
                  handleNext(steps, setOutcome, claimIntent.next)
                },
              )
          }
        }

        is ClaimChatEvent.Regret -> {
          // TODO: Implement regret logic
        }

        is ClaimChatEvent.UpdateFieldAnswer -> {
          Snapshot.withMutableSnapshot {
            val stepToUpdate = steps.find { it.id == event.stepId } ?: return@withMutableSnapshot
            val stepContent = stepToUpdate.stepContent as? StepContent.Form ?: return@withMutableSnapshot

            val newFields = stepContent.fields.map { field ->
              if (field.id == event.fieldId) {
                when (field.type) {
                  StepContent.Form.FieldType.TEXT,
                  StepContent.Form.FieldType.NUMBER,
                  StepContent.Form.FieldType.BINARY,
                  StepContent.Form.FieldType.SINGLE_SELECT,
                  null,
                    -> field.copy(
                    selectedOptions = event.answer?.let {
                      listOf(it)
                    } ?: emptyList(),
                  )

                  StepContent.Form.FieldType.DATE -> field
                  //Date gets selected date from DatePickerState, not from Event


                  StepContent.Form.FieldType.MULTI_SELECT -> {
                    field.copy(
                      selectedOptions = event.answer?.let {
                        val oldSelected = field.selectedOptions
                        val newSelected = if (event.answer in oldSelected) {
                          oldSelected.minus(event.answer)
                        } else {
                          oldSelected.plus(event.answer)
                        }
                        newSelected
                      }
                        ?: field.selectedOptions,
                    )
                  }
                }
              } else {
                field
              }
            }

            val index = steps.indexOf(stepToUpdate)
            if (index >= 0) {
              steps[index] = stepToUpdate.copy(
                stepContent = stepContent.copy(
                  fields = newFields,
                ),
              )
            }
          }
        }

        ClaimChatEvent.DismissErrorDialog -> errorSubmittingStep = null
        is ClaimChatEvent.FileSubmit -> {
          val stepContent = currentStep?.stepContent as? StepContent.FileUpload ?: return@CollectEvents
          val fileUris = stepContent.localFiles
            .filter {
              it.localPath != null
            }
            .map {
              Uri.parse(it.localPath!!)
            }
          currentContinueButtonLoading = true
          launch {
            submitFileUploadUseCase
              .invoke(
                stepId = event.id,
                fileUris = fileUris,
                uploadUrl = stepContent.uploadUri,
              )
              .fold(
                ifLeft = {
                  errorSubmittingStep = it
                  currentContinueButtonLoading = false
                  logcat { "ClaimChatEvent.FileUpload $it" }
                },
                ifRight = { claimIntent ->
                  currentContinueButtonLoading = false
                  handleNext(steps, setOutcome, claimIntent.next)
                },
              )
          }
        }

        is ClaimChatEvent.RemoveFile -> {
          Snapshot.withMutableSnapshot {
            val stepState = currentStep ?: return@CollectEvents
            val stepContent = stepState.stepContent as? StepContent.FileUpload ?: return@CollectEvents
            val index = steps.indexOf(currentStep)
            if (index >= 0) {
              steps[index] = stepState.copy(
                stepContent = stepContent.copy(
                  localFiles = stepContent.localFiles.filterNot { it.id == event.fileId },
                ),
              )
            }
          }
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
        freeText = freeText,
        errorSubmittingStep = errorSubmittingStep,
        currentContinueButtonLoading = currentContinueButtonLoading,
        currentSkipButtonLoading = currentSkipButtonLoading
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

private inline fun <reified T : StepContent> SnapshotStateList<ClaimIntentStep>.updateStep(
  stepId: StepId,
  transform: (ClaimIntentStep, T) -> ClaimIntentStep
) {
  Snapshot.withMutableSnapshot {
    val stepToUpdate = find { it.id == stepId } ?: return@withMutableSnapshot
    val stepContent = stepToUpdate.stepContent as? T ?: return@withMutableSnapshot
    val index = indexOf(stepToUpdate)
    if (index >= 0) {
      this[index] = transform(stepToUpdate, stepContent)
    }
  }
}

private fun SnapshotStateList<ClaimIntentStep>.updateStep(
  stepId: StepId,
  transform: (ClaimIntentStep) -> ClaimIntentStep
) {
  Snapshot.withMutableSnapshot {
    val stepToUpdate = find { it.id == stepId } ?: return@withMutableSnapshot
    val index = indexOf(stepToUpdate)
    if (index >= 0) {
      this[index] = transform(stepToUpdate)
    }
  }
}

private fun ClaimIntentStep.clearContent(): ClaimIntentStep = when (val content = stepContent) {
  is StepContent.AudioRecording -> copy(
    stepContent = content.copy(recordingState = AudioRecording.NotRecording)
  )
  is StepContent.ContentSelect -> copy(
    stepContent = content.copy(selectedOptionId = null)
  )
  is StepContent.FileUpload -> copy(
    stepContent = content.copy(localFiles = emptyList())
  )
  is StepContent.Form -> copy(
    stepContent = content.copy(
      fields = content.fields.map { field ->
        field.copy(selectedOptions = emptyList())
      }
    )
  )
  is StepContent.Summary,
  is StepContent.Task,
  StepContent.Unknown -> this
}

private fun <T> MutableList<T>.removeLastIf(predicate: (T) -> Boolean) {
  val last = lastOrNull() ?: return
  if (predicate(last)) {
    removeAt(this.lastIndex)
  }
}
