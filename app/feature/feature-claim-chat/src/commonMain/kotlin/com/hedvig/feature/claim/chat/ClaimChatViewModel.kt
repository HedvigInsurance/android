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
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.feature.claim.chat.data.AudioRecordingManager
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState.AudioRecording
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState.FreeTextDescription
import com.hedvig.feature.claim.chat.data.ClaimChatErrorMessage
import com.hedvig.feature.claim.chat.data.ClaimIntent
import com.hedvig.feature.claim.chat.data.ClaimIntentId
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.FieldId
import com.hedvig.feature.claim.chat.data.FormFieldSearchUseCase
import com.hedvig.feature.claim.chat.data.FormSubmissionData
import com.hedvig.feature.claim.chat.data.FormSubmissionData.FieldToSubmit
import com.hedvig.feature.claim.chat.data.FreeTextErrorType.TooShort
import com.hedvig.feature.claim.chat.data.GetClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.RegretStepUseCase
import com.hedvig.feature.claim.chat.data.SkipStepUseCase
import com.hedvig.feature.claim.chat.data.StartClaimIntentUseCase
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepContent.Form.Field
import com.hedvig.feature.claim.chat.data.StepContent.Form.FieldError
import com.hedvig.feature.claim.chat.data.StepContent.Form.FieldOption
import com.hedvig.feature.claim.chat.data.StepContent.Form.FieldType
import com.hedvig.feature.claim.chat.data.StepId
import com.hedvig.feature.claim.chat.data.SubmitAudioRecordingUseCase
import com.hedvig.feature.claim.chat.data.SubmitFileUploadUseCase
import com.hedvig.feature.claim.chat.data.SubmitFormUseCase
import com.hedvig.feature.claim.chat.data.SubmitSelectUseCase
import com.hedvig.feature.claim.chat.data.SubmitSummaryUseCase
import com.hedvig.feature.claim.chat.data.SubmitTaskUseCase
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

    data class SwitchToFreeText(override val id: StepId) : AudioRecording

    data class SwitchToAudioRecording(override val id: StepId) : AudioRecording
  }

  data object RetryInitializing : ClaimChatEvent

  data class UpdateFreeText(val text: String?) : ClaimChatEvent

  data class Select(val id: StepId, val selectedId: String) : ClaimChatEvent

  data class SubmitSelect(val id: StepId) : ClaimChatEvent

  data class UpdateFieldAnswer(
    val stepId: StepId,
    val fieldId: FieldId,
    val answer: FieldOption?,
  ) : ClaimChatEvent

  data class Skip(val id: StepId) : ClaimChatEvent

  data class Regret(val id: StepId) : ClaimChatEvent

  data class ShowConfirmEditDialog(val id: StepId) : ClaimChatEvent

  data object DismissConfirmEditDialog : ClaimChatEvent

  data class SubmitForm(
    val stepId: StepId,
  ) : ClaimChatEvent

  data class AddFile(val id: StepId, val uri: String) : ClaimChatEvent

  data class RemoveFile(val id: StepId, val fileId: String) : ClaimChatEvent

  data class SubmitFile(val id: StepId) : ClaimChatEvent

  data class OpenFreeTextOverlay(
    val restrictions: FreeTextRestrictions,
  ) : ClaimChatEvent

  data object CloseFreeChatOverlay : ClaimChatEvent

  data object DismissErrorDialog : ClaimChatEvent

  data class SubmitClaim(val id: StepId) : ClaimChatEvent

  data object HandledOutcomeNavigation : ClaimChatEvent

  data class HandledDeflectNavigation(val stepId: StepId) : ClaimChatEvent

  data class AddToShownAnimations(val stepId: StepId) : ClaimChatEvent

  data class RetrySubmittingTaskStep(val stepId: StepId) : ClaimChatEvent

  data class UpdateFormFieldSearchQuery(
    val query: String,
    val stepId: StepId,
    val fieldId: FieldId,
  ) : ClaimChatEvent

  data class ClearQuery(
    val stepId: StepId,
    val fieldId: FieldId,
  ) : ClaimChatEvent
}

internal sealed interface ClaimChatUiState {
  data object Initializing : ClaimChatUiState

  data object FailedToStart : ClaimChatUiState

  data class ClaimChat(
    val claimIntentId: ClaimIntentId,
    val steps: List<ClaimIntentStep>,
    val currentStep: ClaimIntentStep?,
    val freeText: String?,
    val outcome: ClaimIntentOutcome?,
    val errorSubmittingStep: ClaimChatErrorMessage?,
    val currentContinueButtonLoading: Boolean = false,
    val currentSkipButtonLoading: Boolean = false,
    val showFreeTextOverlay: FreeTextRestrictions?,
    val showConfirmEditDialogForStep: StepId?,
    val stepsWithShownAnimations: List<StepId>,
    val progress: Float?,
    val searchQuery: SearchObject?,
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
  private val audioRecordingManager: AudioRecordingManager,
  skipStepUseCase: SkipStepUseCase,
  regretStepUseCase: RegretStepUseCase,
  formFieldSearchUseCase: FormFieldSearchUseCase,
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
      regretStepUseCase,
      formFieldSearchUseCase,
    ),
  ) {
  override fun onCleared() {
    super.onCleared()
    audioRecordingManager.reset()
  }
}

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
  private val regretStepUseCase: RegretStepUseCase,
  private val formFieldSearchUseCase: FormFieldSearchUseCase,
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
    var showFreeTextOverlay by remember { mutableStateOf<FreeTextRestrictions?>(null) }
    var currentContinueButtonLoading by remember { mutableStateOf(false) }
    var currentSkipButtonLoading by remember { mutableStateOf(false) }
    var errorSubmittingStep by remember { mutableStateOf<ClaimChatErrorMessage?>(null) }
    var freeText by remember { mutableStateOf<String?>(null) }
    var showConfirmEditDialogForStep by remember { mutableStateOf<StepId?>(null) }
    var progress by remember {
      mutableStateOf<Float?>(
        (lastState as? ClaimChatUiState.ClaimChat)?.progress
          ?: 0f,
      )
    }
    val stepsWithShownAnimations = remember { mutableStateListOf<StepId>() }

    val setOutcome: (ClaimIntentOutcome) -> Unit = { outcome = it }

    var searchQuery by remember { mutableStateOf<SearchObject?>(null) }

    if (initializing) {
      LaunchedEffect(Unit) {
        startClaimIntentUseCase
          .invoke(developmentFlow)
          .fold(
            ifLeft = {
              initializing = false
              failedToStart = true
            },
            ifRight = { claimIntent ->
              Snapshot.withMutableSnapshot {
                initializing = false
                failedToStart = false
                claimIntentId = claimIntent.id
                steps.clear()
                progress = claimIntent.progress
                when (val next = claimIntent.next) {
                  is ClaimIntent.Next.Outcome -> {
                    outcome = next.claimIntentOutcome
                  }

                  is ClaimIntent.Next.Step -> {
                    steps.add(next.claimIntentStep)
                  }
                }
              }
            },
          )
      }
    }

    ObserveIncompleteTaskEffect(
      getClaimIntentUseCase = getClaimIntentUseCase,
      currentStep = currentStep,
      claimIntentId = { claimIntentId },
      onFailure = { failedStepId, errorMessage ->
        onTaskSubmissionFailed(failedStepId, errorMessage, steps) { errorSubmittingStep = it }
      },
      steps = steps,
    )
    SubmitCompleteTaskEffect(
      submitTaskUseCase = submitTaskUseCase,
      currentStep = currentStep,
      onFailure = { failedStepId, errorMessage ->
        onTaskSubmissionFailed(failedStepId, errorMessage, steps) { errorSubmittingStep = it }
      },
      onSuccess = { claimIntent ->
        handleNext(
          steps,
          setOutcome,
          claimIntent,
        ) { progress = it }
      },
    )

    LaunchedEffect(searchQuery) {
      val query = searchQuery
      if (query != null) {
        val searchResult = formFieldSearchUseCase.invoke(
          stepId = query.stepId.value,
          fieldId = query.fieldId,
          query = query.query,
        ).getOrNull()
        steps.updateStepWithSuccess<StepContent.Form>(query.stepId) { step, content ->
          val newFields = content.fields.map { field ->
            if (field.id.value == query.fieldId) {
              when (field.type) {
                FieldType.SEARCH,
                -> {
                  field.copy(
                    foundOptionsInSearch = searchResult?.options ?: emptyList(),
                    suggestedFixedQuery = searchResult?.suggestedFixedQuery,
                  )
                }

                else -> {
                  field
                } // shouldn't happen
              }
            } else {
              field
            }
          }

          step.copy(
            stepContent = content.copy(
              fields = newFields,
            ),
          )
        }
      }
    }

    CollectEvents { event ->
      when (event) {
        is ClaimChatEvent.Select -> {
          if (!steps.updateStepWithSuccess<StepContent.ContentSelect>(event.id) { step, content ->
              step.copy(
                stepContent = content.copy(
                  selectedOptionId = event.selectedId,
                ),
              )
            }
          ) {
            return@CollectEvents
          }
        }

        is ClaimChatEvent.SubmitSelect -> {
          val selectedId = (
            steps.find {
              it.id == event.id
            }?.stepContent as? StepContent.ContentSelect
          )?.selectedOptionId
          if (selectedId == null) return@CollectEvents
          currentContinueButtonLoading = true
          launch {
            submitSelectUseCase
              .invoke(
                id = event.id,
                selectedId = selectedId,
              )
              .fold(
                ifLeft = {
                  errorSubmittingStep = it
                  currentContinueButtonLoading = false
                  logcat { "ClaimChatEvent.SubmitSelect error: $it" }
                },
                ifRight = { claimIntent ->
                  currentContinueButtonLoading = false
                  handleNext(
                    steps,
                    setOutcome,
                    claimIntent,
                  ) { progress = it }
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
                      audioRecordingManager.reset()
                      currentContinueButtonLoading = false
                      handleNext(
                        steps,
                        setOutcome,
                        claimIntent,
                      ) { progress = it }
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
                      audioRecordingManager.reset()
                      currentContinueButtonLoading = false
                      handleNext(
                        steps,
                        setOutcome,
                        claimIntent,
                      ) { progress = it }
                    },
                  )
              }
            }

            is ClaimChatEvent.AudioRecording.StartRecording -> {
              audioRecordingManager.startRecording { recordingState ->
                steps.updateStepWithSuccess<StepContent.AudioRecording>(event.id) { step, content ->
                  step.copy(stepContent = content.copy(recordingState = recordingState))
                }
              }
            }

            is ClaimChatEvent.AudioRecording.StopRecording -> {
              audioRecordingManager.stopRecording { playbackState ->
                steps.updateStepWithSuccess<StepContent.AudioRecording>(event.id) { step, content ->
                  step.copy(stepContent = content.copy(recordingState = playbackState))
                }
              }
            }

            is ClaimChatEvent.AudioRecording.RedoRecording -> {
              audioRecordingManager.reset()
              steps.updateStepWithSuccess<StepContent.AudioRecording>(event.id) { step, content ->
                step.copy(stepContent = content.copy(recordingState = AudioRecording.NotRecording))
              }
            }

            is ClaimChatEvent.AudioRecording.SwitchToFreeText -> {
              val currentContent = currentStep?.stepContent as? StepContent.AudioRecording
                ?: return@CollectEvents
              val textTooShort = freeText?.length?.let {
                currentContent.freeTextMinLength > it
              } ?: true
              steps.updateStepWithSuccess<StepContent.AudioRecording>(event.id) { step, content ->
                val canSubmit = !currentContinueButtonLoading && !freeText.isNullOrEmpty() && !textTooShort
                showFreeTextOverlay = FreeTextRestrictions(
                  content.freeTextMinLength,
                  content.freeTextMaxLength,
                )
                step.copy(
                  stepContent = content.copy(
                    recordingState = FreeTextDescription(
                      errorType = null,
                      canSubmit = canSubmit,
                    ),
                  ),
                )
              }
            }

            is ClaimChatEvent.AudioRecording.SwitchToAudioRecording -> {
              steps.updateStepWithSuccess<StepContent.AudioRecording>(event.id) { step, content ->
                step.copy(stepContent = content.copy(recordingState = AudioRecording.NotRecording))
              }
            }
          }
        }

        is ClaimChatEvent.SubmitForm -> {
          Snapshot.withMutableSnapshot {
            val currentContent = steps.firstOrNull { it.id == event.stepId }?.stepContent as? StepContent.Form
              ?: return@CollectEvents

            val validatedFields = currentContent.fields.map { field ->
              validateField(field)
            }
            val hasValidationErrors = validatedFields.any { it.hasError != null }
            if (hasValidationErrors) {
              steps.updateStepWithSuccess<StepContent.Form>(event.stepId) { step, content ->
                step.copy(
                  stepContent = content.copy(fields = validatedFields),
                )
              }
              return@CollectEvents
            }

            val fieldsToSubmit = currentContent.fields.map { field ->
              when (field.type) {
                FieldType.DATE -> {
                  val selectedDateString =
                    field.datePickerUiState?.datePickerState?.selectedDateMillis?.let { selectedDateMillis ->
                      Instant.fromEpochMilliseconds(selectedDateMillis).toLocalDateTime(TimeZone.UTC).date
                    }?.toString()
                  val stepToUpdate = steps.find { it.id == event.stepId } ?: return@withMutableSnapshot
                  val index = steps.indexOf(stepToUpdate)
                  if (index >= 0 && selectedDateString != null) {
                    steps[index] = stepToUpdate.copy(
                      stepContent = currentContent.copy(
                        fields = currentContent.fields.map { existingField ->
                          if (existingField.id == field.id) {
                            existingField.copy(
                              selectedOptions = listOf(
                                FieldOption(
                                  selectedDateString,
                                  selectedDateString,
                                  null,
                                ),
                              ),
                            )
                          } else {
                            existingField
                          }
                        },
                      ),
                    )
                  }
                  FieldToSubmit(
                    field.id,
                    listOf(selectedDateString),
                  )
                }

                else -> {
                  FieldToSubmit(
                    field.id,
                    field.selectedOptions
                      .map { selectedOption ->
                        selectedOption.value.ifEmpty { null }
                      },
                  )
                }
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
                    handleNext(
                      steps,
                      setOutcome,
                      claimIntent,
                    ) { progress = it }
                  },
                )
            }
          }
        }

        is ClaimChatEvent.AddFile -> {
          try {
            val uri = Uri.parse(event.uri)
            val mimeType = fileService.getMimeType(uri)
            val name = fileService.getFileName(uri) ?: event.uri
            val localFile = UiFile(
              name = name,
              localPath = event.uri,
              mimeType = mimeType,
              id = event.uri,
              url = null,
            )

            steps.updateStepWithSuccess<StepContent.FileUpload>(event.id) { step, content ->
              if (event.uri in content.localFiles.map { it.id }) return@updateStepWithSuccess step
              step.copy(
                stepContent = content.copy(
                  localFiles = content.localFiles + localFile,
                ),
              )
            }
          } catch (e: Exception) {
            logcat { "ClaimChatEvent.AddFile error: $e" }
            errorSubmittingStep = ClaimChatErrorMessage.GeneralError
          }
        }

        ClaimChatEvent.CloseFreeChatOverlay -> {
          showFreeTextOverlay = null
        }

        is ClaimChatEvent.OpenFreeTextOverlay -> {
          showFreeTextOverlay = event.restrictions
        }

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
                  if (!steps.updateStepWithSuccess(event.id) { step -> step.clearContent() }) return@launch
                  currentSkipButtonLoading = false
                  handleNext(
                    steps,
                    setOutcome,
                    claimIntent,
                  ) { progress = it }
                },
              )
          }
        }

        ClaimChatEvent.RetryInitializing -> {
          failedToStart = false
          initializing = true
        }

        is ClaimChatEvent.UpdateFreeText -> {
          Snapshot.withMutableSnapshot {
            val currentContent = currentStep?.stepContent as? StepContent.AudioRecording
              ?: return@CollectEvents
            val recordingState = currentContent.recordingState as? FreeTextDescription
              ?: return@CollectEvents
            val textTooShort = event.text?.length?.let {
              currentContent.freeTextMinLength > it
            } ?: true
            steps.updateStepWithSuccess<StepContent.AudioRecording>(currentStep!!.id) { step, content ->
              val canSubmit = !currentContinueButtonLoading && !event.text.isNullOrEmpty() && !textTooShort
              step.copy(
                stepContent = content.copy(
                  recordingState = recordingState.copy(
                    hasError = textTooShort,
                    errorType = if (textTooShort) {
                      TooShort(
                        currentContent.freeTextMinLength,
                      )
                    } else {
                      null
                    },
                    canSubmit = canSubmit,
                  ),
                ),
              )
            }
            freeText = event.text
          }
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
                  handleNext(
                    steps,
                    setOutcome,
                    claimIntent,
                  ) { progress = it }
                },
              )
          }
        }

        is ClaimChatEvent.Regret -> {
          Snapshot.withMutableSnapshot {
            val stepToUpdate = steps.find { it.id == event.id } ?: return@CollectEvents
            if (!stepToUpdate.isRegrettable) return@CollectEvents
            currentContinueButtonLoading = true
            currentSkipButtonLoading = true
            launch {
              regretStepUseCase.invoke(event.id)
                .fold(
                  ifLeft = {
                    currentContinueButtonLoading = false
                    currentSkipButtonLoading = false
                    errorSubmittingStep = it
                    logcat { "Regret error: $it" }
                  },
                  ifRight = { claimIntent ->
                    if (claimIntent.next is ClaimIntent.Next.Step) {
                      stepsWithShownAnimations.add(claimIntent.next.claimIntentStep.id)
                    }
                    val index = steps.indexOf(stepToUpdate)
                    if (index >= 0) {
                      steps.subList(index, steps.size).clear()
                      if (steps.none { it.stepContent is StepContent.AudioRecording }) freeText = null
                    }
                    currentContinueButtonLoading = false
                    currentSkipButtonLoading = false
                    handleNext(
                      steps,
                      setOutcome,
                      claimIntent,
                    ) { progress = it }
                  },
                )
            }
          }
        }

        is ClaimChatEvent.UpdateFieldAnswer -> {
          steps.updateStepWithSuccess<StepContent.Form>(event.stepId) { step, content ->
            val newFields = content.fields.map { field ->
              if (field.id == event.fieldId) {
                when (field.type) {
                  FieldType.TEXT,
                  FieldType.NUMBER,
                  FieldType.BINARY,
                  FieldType.SINGLE_SELECT,
                  null,
                  -> {
                    field.copy(
                      selectedOptions = event.answer?.let {
                        listOf(it)
                      } ?: emptyList(),
                      hasError = null,
                    )
                  }

                  FieldType.SEARCH -> {
                    field.copy(
                      selectedOptions = event.answer?.let {
                        listOf(it)
                      } ?: emptyList(),
                      hasError = null,
                      searchData = field.searchData?.copy(suggestedQuery = searchQuery?.query),
                      suggestedFixedQuery = null,
                    )
                  }

                  // Date gets selected date from DatePickerState, not from Event
                  FieldType.DATE -> {
                    field.copy(hasError = null)
                  }

                  FieldType.MULTI_SELECT -> {
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
                      hasError = null,
                    )
                  }
                }
              } else {
                field
              }
            }
            searchQuery = null
            step.copy(
              stepContent = content.copy(
                fields = newFields,
              ),
            )
          }
        }

        ClaimChatEvent.DismissErrorDialog -> {
          errorSubmittingStep = null
        }

        is ClaimChatEvent.SubmitFile -> {
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
                  handleNext(
                    steps,
                    setOutcome,
                    claimIntent,
                  ) { progress = it }
                },
              )
          }
        }

        is ClaimChatEvent.RemoveFile -> {
          currentStep?.let { step ->
            steps.updateStepWithSuccess<StepContent.FileUpload>(step.id) { currentStep, content ->
              currentStep.copy(
                stepContent = content.copy(
                  localFiles = content.localFiles.filterNot { it.id == event.fileId },
                ),
              )
            }
          }
        }

        ClaimChatEvent.HandledOutcomeNavigation -> {
          outcome = null
        }

        is ClaimChatEvent.HandledDeflectNavigation -> {
          // todo or remove
        }

        ClaimChatEvent.DismissConfirmEditDialog -> {
          showConfirmEditDialogForStep = null
        }

        is ClaimChatEvent.ShowConfirmEditDialog -> {
          showConfirmEditDialogForStep = event.id
        }

        is ClaimChatEvent.AddToShownAnimations -> {
          stepsWithShownAnimations.add(event.stepId)
        }

        is ClaimChatEvent.RetrySubmittingTaskStep -> {
          steps.updateStepWithSuccess<StepContent.Task>(event.stepId) { step, content ->
            step.copy(
              stepContent = content.copy(
                isCompleted = false,
                failedToSubmit = false,
              ),
            )
          }
        }

        is ClaimChatEvent.UpdateFormFieldSearchQuery -> {
          searchQuery = SearchObject(
            fieldId = event.fieldId.value,
            stepId = event.stepId,
            query = event.query,
          )
        }

        is ClaimChatEvent.ClearQuery -> {
          searchQuery = null
          steps.updateStepWithSuccess<StepContent.Form>(event.stepId) { step, content ->
            val newFields = content.fields.map { field ->
              if (field.id == event.fieldId) {
                field.copy(foundOptionsInSearch = emptyList())
              } else {
                field
              }
            }
            step.copy(
              stepContent = content.copy(fields = newFields),
            )
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
        currentSkipButtonLoading = currentSkipButtonLoading,
        showConfirmEditDialogForStep = showConfirmEditDialogForStep,
        stepsWithShownAnimations = stepsWithShownAnimations,
        progress = progress,
        searchQuery = searchQuery,
      )

      else -> error("")
    }
  }
}

internal data class FreeTextRestrictions(
  val minLength: Int,
  val maxLength: Int,
)

@Composable
private fun ObserveIncompleteTaskEffect(
  getClaimIntentUseCase: GetClaimIntentUseCase,
  currentStep: ClaimIntentStep?,
  claimIntentId: () -> ClaimIntentId?,
  onFailure: (StepId, ClaimChatErrorMessage) -> Unit,
  steps: SnapshotStateList<ClaimIntentStep>,
) {
  val stepContent = currentStep?.stepContent as? StepContent.Task
  LaunchedEffect(stepContent) {
    val isIncompleteTask = stepContent?.isCompleted?.not() == true
    if (!isIncompleteTask) return@LaunchedEffect
    if (stepContent.failedToSubmit) return@LaunchedEffect
    getClaimIntentUseCase
      .invoke(claimIntentId()!!)
      .collect { result ->
        result.fold(
          ifLeft = { onFailure(currentStep.id, it) },
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
  onFailure: (StepId, ClaimChatErrorMessage) -> Unit,
  onSuccess: (ClaimIntent) -> Unit,
) {
  val isCompleteTask = (currentStep?.stepContent as? StepContent.Task)?.isCompleted == true
  LaunchedEffect(isCompleteTask) {
    if (!isCompleteTask) return@LaunchedEffect
    submitTaskUseCase
      .invoke(currentStep.id.value)
      .fold(
        ifLeft = { onFailure(currentStep.id, it) },
        ifRight = { claimIntent ->
          onSuccess(claimIntent)
        },
      )
  }
}

private fun handleNext(
  steps: SnapshotStateList<ClaimIntentStep>,
  setOutcome: (outcome: ClaimIntentOutcome) -> Unit,
  intent: ClaimIntent,
  setProgress: (Float?) -> Unit,
) {
  val next = intent.next
  setProgress(intent.progress)
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
    val isPreviousStepTask = this.lastOrNull()?.stepContent is StepContent.Task
    removeLastIf { isPreviousStepTask }
    add(step.copy(continuePreviousAnimation = isPreviousStepTask))
  }
}

private inline fun <reified T : StepContent> SnapshotStateList<ClaimIntentStep>.updateStepWithSuccess(
  stepId: StepId,
  transform: (ClaimIntentStep, T) -> ClaimIntentStep,
): Boolean {
  return Snapshot.withMutableSnapshot {
    val stepToUpdate = find { it.id == stepId } ?: return@withMutableSnapshot false
    val stepContent = stepToUpdate.stepContent as? T ?: return@withMutableSnapshot false
    val index = indexOf(stepToUpdate)
    if (index >= 0) {
      this[index] = transform(stepToUpdate, stepContent)
      return@withMutableSnapshot true
    }
    return@withMutableSnapshot false
  }
}

private fun SnapshotStateList<ClaimIntentStep>.updateStepWithSuccess(
  stepId: StepId,
  transform: (ClaimIntentStep) -> ClaimIntentStep,
): Boolean {
  return Snapshot.withMutableSnapshot {
    val stepToUpdate = find { it.id == stepId } ?: return@withMutableSnapshot false
    val index = indexOf(stepToUpdate)
    if (index >= 0) {
      this[index] = transform(stepToUpdate)
      return@withMutableSnapshot true
    }
    return@withMutableSnapshot false
  }
}

private fun ClaimIntentStep.clearContent(): ClaimIntentStep = when (val content = stepContent) {
  is StepContent.AudioRecording -> copy(
    stepContent = content.copy(recordingState = AudioRecording.NotRecording),
  )

  is StepContent.ContentSelect -> copy(
    stepContent = content.copy(selectedOptionId = null),
  )

  is StepContent.FileUpload -> copy(
    stepContent = content.copy(localFiles = emptyList()),
  )

  is StepContent.Form -> copy(
    stepContent = content.copy(
      fields = content.fields.map { field ->
        field.copy(selectedOptions = emptyList())
      },
    ),
  )

  is StepContent.Summary,
  is StepContent.Task,
  is StepContent.Deflect,
  StepContent.Unknown,
  -> this
}

private fun onTaskSubmissionFailed(
  failedStepId: StepId,
  errorMessage: ClaimChatErrorMessage,
  steps: SnapshotStateList<ClaimIntentStep>,
  setErrorMessage: (ClaimChatErrorMessage) -> Unit,
) {
  steps.updateStepWithSuccess<StepContent.Task>(failedStepId) { step, content ->
    step.copy(stepContent = content.copy(failedToSubmit = true))
  }
  setErrorMessage(errorMessage)
}

private fun <T> MutableList<T>.removeLastIf(predicate: (T) -> Boolean) {
  val last = lastOrNull() ?: return
  if (predicate(last)) {
    removeAt(this.lastIndex)
  }
}

private fun validateField(field: Field): Field {
  if (field.isRequired) {
    val isMissing = when (field.type) {
      FieldType.DATE -> field.datePickerUiState?.datePickerState?.selectedDateMillis == null
      else -> field.selectedOptions.isEmpty() || field.selectedOptions.all { it.value.isEmpty() }
    }
    if (isMissing) {
      return field.copy(hasError = FieldError.Missing)
    }
  }

  if (field.type == FieldType.NUMBER && field.selectedOptions.isNotEmpty()) {
    val selectedValue = field.selectedOptions.firstOrNull()?.value
    if (!selectedValue.isNullOrEmpty()) {
      val numericValue = selectedValue.toDoubleOrNull()
      if (numericValue != null) {
        field.maxValue?.toDoubleOrNull()?.let { maxValue ->
          if (numericValue > maxValue) {
            return field.copy(hasError = FieldError.BiggerThanMaxValue)
          }
        }
        field.minValue?.toDoubleOrNull()?.let { minValue ->
          if (numericValue < minValue) {
            return field.copy(hasError = FieldError.LessThanMinValue)
          }
        }
      }
    }
  }

  return field.copy(hasError = null)
}

internal data class SearchObject(
  val fieldId: String,
  val stepId: StepId,
  val query: String,
)
