package com.hedvig.feature.claim.chat.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.compose.photo.capture.state.rememberPhotoCaptureState
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DatePickerUiState
import com.hedvig.android.design.system.hedvig.DatePickerWithDialog
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.MultiSelectDialog
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.SingleSelectDialog
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ThreeDotsLoading
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.Camera
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.Document
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadFilled
import com.hedvig.android.design.system.hedvig.icon.Image
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.audio.player.data.SignedAudioUrl
import com.hedvig.feature.claim.chat.ui.audiorecording.AudioRecordingStep
import com.hedvig.feature.claim.chat.ui.audiorecording.AudioRecordingStepState
import com.hedvig.feature.claim.chat.ui.audiorecording.AudioUrl
import hedvig.resources.R
import java.io.File
import kotlin.time.Clock
import kotlinx.datetime.LocalDate

//todo: if we want a a fullscreen free text overlay,
// the claim chat screen should be wrapped in this:
//@Composable
//internal fun ClaimChatScreen(
//  updateFreeText: (String?) -> Unit,
//  onCloseFullScreenEditText: () -> Unit,
//) {
//  FreeTextOverlay(
//    freeTextMaxLength = 2000,
//    freeTextValue = if (uiState is AudioRecordingStepState.FreeTextDescription) uiState.freeText else null,
//    freeTextHint = stringResource(R.string.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER),
//    freeTextTitle = stringResource(R.string.CLAIMS_TEXT_INPUT_PLACEHOLDER),
//    freeTextOnCancelClick = {
//      onCloseFullScreenEditText()
//    },
//    freeTextOnSaveClick = { feedback ->
//      updateFreeText(feedback)
//      onCloseFullScreenEditText()
//    },
//    shouldShowOverlay = if (uiState is AudioRecordingStepState.FreeTextDescription) uiState.showOverlay else false,
//    overlaidContent = {
//      // all chat content
//    })
//}


@Composable
internal fun AudioRecorderBubble(
  uiState: AudioRecordingStepState,
  clock: Clock,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  submitAudioUrl: (AudioUrl) -> Unit,
  redo: () -> Unit,
  openAppSettings: () -> Unit,
  freeTextAvailable: Boolean,
  submitFreeText: () -> Unit,
  showFreeText: () -> Unit,
  showAudioRecording: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
  canSkip: Boolean,
  onSkip: () -> Unit,
  isCurrentStep: Boolean,
  modifier: Modifier = Modifier,
) {
  AudioRecordingStep(
    uiState = uiState,
    clock = clock,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    startRecording = startRecording,
    stopRecording = stopRecording,
    submitAudioFile = submitAudioFile,
    submitAudioUrl = submitAudioUrl,
    redo = redo,
    openAppSettings = openAppSettings,
    freeTextAvailable = freeTextAvailable,
    submitFreeText = submitFreeText,
    showFreeText = showFreeText,
    showAudioRecording = showAudioRecording,
    onLaunchFullScreenEditText = onLaunchFullScreenEditText,
    canSkip = canSkip,
    onSkip = onSkip,
    isCurrentStep = isCurrentStep,
    modifier = modifier,
  )
}


@Composable
internal fun AssistantMessageBubble(
  text: String,
  comment: String?,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(text)
    if (comment != null) {
      HedvigText(
        comment,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      )
    }
    if (isLoading) {
      ThreeDotsLoading(Modifier.padding(vertical = 16.dp))
    } else {
      Spacer(Modifier.height(8.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val color = HedvigTheme.colorScheme.signalGreenElement
        Spacer(
          Modifier
            .wrapContentSize(Alignment.Center)
            .size(20.dp)
            .padding(1.dp)
            .background(color, CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        HedvigText(
          "Hedvig AI Assistant", //todo
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        )
      }
    }
  }
}

@Composable
internal fun YesNoBubble(
  questionLabel: String,
  answerSelected: Boolean?,
  isPrefilled: Boolean,
  isCurrentStep: Boolean,
  canBeChanged: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  onSelect: (Boolean) -> Unit,
  onSubmit: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  StandardBubble(
    isPrefilledByAI = isPrefilled,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    onSkip = onSkip,
    selectedAnswer = answerSelected,
    content = {
      Column(
        Modifier.padding(16.dp),
        horizontalAlignment = Alignment.End,
      ) {
        HedvigText(
          questionLabel,
          style = HedvigTheme.typography.label,
        )
        Spacer(Modifier.height(16.dp))
        Row(
          horizontalArrangement = Arrangement.End,
        ) {
          HighlightLabel(
            labelText = stringResource(R.string.GENERAL_YES),
            size = HighlightLabelDefaults.HighLightSize.Medium,
            color = if (answerSelected == true) HighlightLabelDefaults.HighlightColor.Green(
              HighlightLabelDefaults.HighlightShade.LIGHT,
            )
            else HighlightLabelDefaults.HighlightColor.Grey(
              HighlightLabelDefaults.HighlightShade.LIGHT,
            ),
            modifier = Modifier.clickable(
              enabled = canBeChanged, //todo
              onClick = {
                onSelect(true)
              },
            ),
          )
          Spacer(Modifier.width(16.dp))
          HighlightLabel(
            labelText = stringResource(R.string.GENERAL_NO),
            size = HighlightLabelDefaults.HighLightSize.Medium,
            color = if (answerSelected != null && !answerSelected) HighlightLabelDefaults.HighlightColor.Green(
              HighlightLabelDefaults.HighlightShade.LIGHT,
            )
            else HighlightLabelDefaults.HighlightColor.Grey(
              HighlightLabelDefaults.HighlightShade.MEDIUM,
            ),
            modifier = Modifier.clickable(
              enabled = canBeChanged, //todo
              onClick = {
                onSelect(false)
              },
            ),
          )
        }
      }
    },
  )
}

@Composable
internal fun SingleSelectBubbleWithDialog(
  questionLabel: String,
  options: List<RadioOption>,
  selectedOptionId: RadioOptionId?,
  isPrefilled: Boolean,
  isCurrentStep: Boolean,
  canBeChanged: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  onSelect: (RadioOptionId) -> Unit,
  onSubmit: (RadioOptionId) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDialog by rememberSaveable { mutableStateOf(false) }
  if (showDialog) {
    SingleSelectDialog(
      title = questionLabel,
      options = options,
      selectedOption = selectedOptionId,
      onRadioOptionSelected = onSelect,
      onDismissRequest = {
        showDialog = false
      },
    )
  }
  StandardBubble(
    isPrefilledByAI = isPrefilled,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    onSkip = onSkip,
    selectedAnswer = selectedOptionId,
    content = {
      HedvigBigCard(
        onClick = { showDialog = true },
        labelText = questionLabel,
        inputText = options.firstOrNull {
          it.id == selectedOptionId
        }?.text,
        modifier = modifier,
        enabled = canBeChanged,
      )
    },
  )
}

@Composable
internal fun MultiSelectBubbleWithDialog(
  questionLabel: String,
  options: List<RadioOption>,
  selectedOptionIds: List<RadioOptionId>,
  isPrefilled: Boolean,
  isCurrentStep: Boolean,
  canBeChanged: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  onSelect: (RadioOptionId) -> Unit,
  onSubmit: (List<RadioOptionId>) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDialog: Boolean by rememberSaveable { mutableStateOf(false) }
  if (showDialog) {
    MultiSelectDialog(
      title = questionLabel,
      options = options,
      selectedOptions = selectedOptionIds,
      onOptionSelected = onSelect,
      onDismissRequest = { showDialog = false },
      buttonText = stringResource(R.string.general_save_button),
    )
  }
  StandardBubble(
    isPrefilledByAI = isPrefilled,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    onSkip = onSkip,
    selectedAnswer = selectedOptionIds,
    content = {
      HedvigBigCard(
        onClick = { showDialog = true },
        labelText = questionLabel,
        inputText = when {
          selectedOptionIds.isEmpty() -> null
          else -> options.filter { it.id in selectedOptionIds }
            .joinToString(transform = RadioOption::text)
        },
        modifier = modifier,
        enabled = isCurrentStep || canBeChanged,
      )
    },
  )
}

@Composable
internal fun UploadFilesBubble(
  isCurrentStep: Boolean,
  canSkip: Boolean,
  canBeChanged: Boolean,
  onSkip: () -> Unit,
  addLocalFile: (uri: Uri) -> Unit,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val fileTypeSelectBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  val photoCaptureState = rememberPhotoCaptureState(appPackageId = appPackageId) { uri ->
    addLocalFile(uri)
  }
  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(),
  ) { resultingUriList: List<Uri> ->
    for (resultingUri in resultingUriList) {
      addLocalFile(resultingUri)
    }
  }
  val filePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents(),
  ) { resultingUriList: List<Uri> ->
    for (resultingUri in resultingUriList) {
      addLocalFile(resultingUri)
    }
  }
  FilePickerBottomSheet(
    sheetState = fileTypeSelectBottomSheetState,
    onPickPhoto = {
      photoPicker.launch(PickVisualMediaRequest())
      fileTypeSelectBottomSheetState.dismiss()
    },
    onPickFile = {
      filePicker.launch("*/*")
      fileTypeSelectBottomSheetState.dismiss()
    },
    onTakePhoto = {
      photoCaptureState.launchTakePhotoRequest()
      fileTypeSelectBottomSheetState.dismiss()
    },
  )

  StandardBubble(
    isPrefilledByAI = false,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    selectedAnswer = TODO(),
    onSkip = onSkip,
    content = {
      HedvigButton(
        text = stringResource(R.string.file_upload_upload_files),
        onClick = submitFiles,
        enabled = isCurrentStep || canBeChanged,
      )
    },
  )
}

@Composable
fun FilePickerBottomSheet(
  sheetState: HedvigBottomSheetState<Unit>,
  onPickPhoto: () -> Unit,
  onPickFile: () -> Unit,
  onTakePhoto: () -> Unit,
) {
  HedvigBottomSheet(
    sheetState,
    content = {
      FilePickerBottomSheetContent(
        onPickPhoto = onPickPhoto,
        onTakePhoto = onTakePhoto,
        onPickFile = onPickFile,
      )
    },
  )
}

@Composable
private fun FilePickerBottomSheetContent(
  onPickPhoto: () -> Unit,
  onTakePhoto: () -> Unit,
  onPickFile: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      ClickableOption(
        text = stringResource(R.string.file_upload_photo_library),
        icon = HedvigIcons.Image,
        onClick = onPickPhoto,
      )
      ClickableOption(
        text = stringResource(R.string.file_upload_take_photo),
        icon = HedvigIcons.Camera,
        onClick = onTakePhoto,
      )
      ClickableOption(
        text = stringResource(R.string.file_upload_choose_files),
        icon = HedvigIcons.Document,
        onClick = onPickFile,
      )
    }
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
internal fun DeflectBubble(
  isCurrentStep: Boolean,
  canSkip: Boolean,
  canBeChanged: Boolean,
  onSkip: () -> Unit,
  onSubmit: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    Modifier.padding(start = 8.dp, top = 8.dp),
  ) {

  }
}

@Composable
internal fun DateSelectBubble(
  questionLabel: String?,
  date: LocalDate?,
  isPrefilled: Boolean,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  canBeChanged: Boolean,
  onSkip: () -> Unit,
  onSubmit: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  StandardBubble(
    isPrefilledByAI = isPrefilled,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    selectedAnswer = date,
    onSkip = onSkip,
    content = {
      val datePickerState = DatePickerUiState(
        locale = getLocale(),
        initiallySelectedDate = date,
      )
      DatePickerWithDialog(
        datePickerState,
        canInteract = canBeChanged,
        startText = questionLabel ?: "", //todo
      )
    },
  )
}

@Composable
internal fun TextInputBubble(
  questionLabel: String,
  text: String?,
  suffix: String?,
  isPrefilled: Boolean,
  isCurrentStep: Boolean,
  canBeChanged: Boolean,
  canSkip: Boolean,
  onInput: (String?) -> Unit,
  onSubmit: (String) -> Unit,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier,
) {
  StandardBubble(
    isPrefilledByAI = isPrefilled,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    selectedAnswer = text,
    onSkip = onSkip,
    content = {
      val focusRequester = remember { FocusRequester() }
      var textValue by rememberSaveable {
        mutableStateOf(
          text
            ?: "",
        )
      }
      val focusManager = LocalFocusManager.current
      HedvigTextField(
        text = textValue,
        trailingContent = {},
        onValueChange = onValueChange@{ newValue ->
          textValue = newValue
          onInput(newValue.ifBlank { null })
        },
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        labelText = questionLabel,
        modifier = Modifier.focusRequester(focusRequester),
        enabled = canBeChanged,
        suffix = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (suffix != null) {
              HedvigText(suffix)
            }
            AnimatedVisibility(textValue.isNotEmpty()) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(16.dp))
                IconButton(
                  onClick = {
                    onInput("")
                    textValue = ""
                  },
                  Modifier.size(24.dp),
                ) {
                  Icon(
                    HedvigIcons.Close,
                    stringResource(R.string.GENERAL_REMOVE),
                  )
                }
              }
            }
          }
        },
        keyboardOptions = KeyboardOptions(
          autoCorrectEnabled = false,
          imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
          onDone = {
            focusManager.clearFocus()
          },
        ),
      )
    },
  )
}

@Composable
internal fun ChatClaimSummary(
  text: String,
  recordingUrl: String?,
  displayItems: List<Pair<String, String>>,
  onSubmit: () -> Unit,
  isCurrentStep: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(text)
    HorizontalDivider()
    if (recordingUrl != null) {
      val audioPlayer = rememberAudioPlayer(
        PlayableAudioSource.RemoteUrl(
          SignedAudioUrl
            .fromSignedAudioUrlString(recordingUrl),
        ),
      )
      HedvigAudioPlayer(audioPlayer = audioPlayer)
    }
    CompositionLocalProvider(LocalContentColor provides HedvigTheme.colorScheme.textSecondary) {
      Column(modifier) {
        for (displayItem in displayItems) {
          HorizontalItemsWithMaximumSpaceTaken(
            spaceBetween = 8.dp,
            startSlot = {
              HedvigText(text = displayItem.first)
            },
            endSlot = {
              val formatter = rememberHedvigDateTimeFormatter()
              HedvigText(
                text = displayItem.second,
                textAlign = TextAlign.End,
              )
            },
          )
        }
      }
    }
    if (isCurrentStep) {
      Spacer(Modifier.height(16.dp))
      Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
      ) {
        HedvigButton(
          text = stringResource(R.string.EMBARK_SUBMIT_CLAIM),
          enabled = true,
          onClick = onSubmit,
          buttonSize = ButtonDefaults.ButtonSize.Medium,
        )
      }
    }
  }
}

@Composable
internal fun ChatClaimOutcome(
  text: String,
  claimId: String?,
  onNavigateToClaim: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    HedvigText(text)
    if (claimId != null) {
      Spacer(Modifier.height(16.dp))
      Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
      ) {
        HedvigButton(
          onClick = {
            onNavigateToClaim(claimId)
          },
          enabled = true,
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
          buttonSize = ButtonDefaults.ButtonSize.Medium,
          text = stringResource(R.string.CHAT_CONVERSATION_CLAIM_TITLE),
        )
      }
    }
  }
}

@Composable
internal fun <T> StandardBubble(
  isPrefilledByAI: Boolean,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  selectedAnswer: T?,
  onSkip: () -> Unit,
  onSubmit: (T) -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Row(
    modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End,
  ) {
    Column {
      Box {
        HedvigCard(
          Modifier.padding(start = 8.dp, top = 8.dp),
        ) {
          content()
        }
        if (isPrefilledByAI) {
          Icon(
            imageVector = HedvigIcons.HelipadFilled,
            tint = HedvigTheme.colorScheme.signalAmberElement,
            modifier = Modifier.align(Alignment.TopStart),
            contentDescription = null, //todo
          )
        }
      }
      if (isCurrentStep) {
        Spacer(Modifier.height(16.dp))
        Row(
          Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
        ) {
          if (canSkip) {
            HedvigTextButton(
              stringResource(R.string.claims_skip_button),
              onClick = onSkip,
              buttonSize = ButtonDefaults.ButtonSize.Medium,
            )
          }
          Spacer(Modifier.width(16.dp))
          HedvigButton(
            text = stringResource(R.string.CHAT_UPLOAD_PRESS_SEND_LABEL),
            enabled = selectedAnswer != null,
            onClick = {
              if (selectedAnswer != null) onSubmit(selectedAnswer)
            },
            buttonSize = ButtonDefaults.ButtonSize.Medium,
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewWithAssistantBubble() {
  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp),
      ) {
        AssistantMessageBubble(
          text = "Tell us where it happened.",
          comment = null,
          isLoading = false,
        )
        SingleSelectBubbleWithDialog(
          questionLabel = "Location",
          options = listOf(
            RadioOption(RadioOptionId("01"), "At home"),
            RadioOption(RadioOptionId("02"), "Outside home"),
          ),
          selectedOptionId = RadioOptionId("01"),
          isPrefilled = true,
          isCurrentStep = false,
          canBeChanged = true,
          canSkip = false,
          onSubmit = {},
          onSkip = {},
          onSelect = {},
        )
        AssistantMessageBubble(
          text = "Processing information",
          comment = "Done!",
          isLoading = false,
        )
        AssistantMessageBubble(
          text = "Specify when it happened.",
          comment = null,
          isLoading = false,
        )
        DateSelectBubble(
          questionLabel = "Date of occurence",
          date = LocalDate(2025, 11, 10),
          isPrefilled = true,
          isCurrentStep = false,
          canSkip = false,
          canBeChanged = true,
          onSubmit = {},
          onSkip = {},
        )
        AssistantMessageBubble(
          text = "Processing information",
          comment = null,
          isLoading = true,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimChatComponents() {
  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.End,
      ) {
        AudioRecorderBubble(
          isCurrentStep = true,
          uiState = AudioRecordingStepState.FreeTextDescription(
            "some not really long free text",
            showOverlay = false,
            errorType = null,
          ),
          clock = Clock.System,
          shouldShowRequestPermissionRationale = {
            false
          },
          startRecording = {},
          stopRecording = {},
          submitAudioFile = {},
          submitAudioUrl = {},
          redo = {},
          openAppSettings = {},
          freeTextAvailable = true,
          submitFreeText = {},
          showFreeText = {},
          showAudioRecording = {},
          onLaunchFullScreenEditText = {},
          canSkip = true,
          onSkip = {},
        )
        Spacer(Modifier.height(16.dp))
        YesNoBubble(
          questionLabel = "Was the bike electric?",
          answerSelected = true,
          isPrefilled = false,
          isCurrentStep = false,
          canBeChanged = true,
          canSkip = true,
          onSubmit = {},
          onSkip = {},
          onSelect = {},
        )
        Spacer(Modifier.height(16.dp))
        SingleSelectBubbleWithDialog(
          questionLabel = "Location",
          options = listOf(
            RadioOption(RadioOptionId("01"), "At home"),
            RadioOption(RadioOptionId("02"), "Outside home"),
          ),
          selectedOptionId = RadioOptionId("01"),
          isPrefilled = true,
          isCurrentStep = false,
          canBeChanged = true,
          canSkip = false,
          onSubmit = {},
          onSkip = {},
          onSelect = {},
        )
        Spacer(Modifier.height(16.dp))
        MultiSelectBubbleWithDialog(
          questionLabel = "Select the damage type",
          options = listOf(
            RadioOption(RadioOptionId("01"), "Wheel"),
            RadioOption(RadioOptionId("02"), "Seat"),
          ),
          selectedOptionIds = listOf(RadioOptionId("01"), RadioOptionId("02")),
          isCurrentStep = false,
          canBeChanged = true,
          canSkip = true,
          onSkip = {},
          onSelect = {},
          onSubmit = {},
          isPrefilled = false,
        )
        Spacer(Modifier.height(16.dp))
        DateSelectBubble(
          questionLabel = "Date of occurence",
          date = LocalDate(2025, 11, 10),
          isPrefilled = true,
          isCurrentStep = false,
          canSkip = false,
          canBeChanged = true,
          onSubmit = {},
          onSkip = {},
        )
        Spacer(Modifier.height(16.dp))
        TextInputBubble(
          questionLabel = "Re-purchase price",
          text = "15000",
          suffix = "SEK",
          isPrefilled = true,
          isCurrentStep = true,
          canBeChanged = true,
          canSkip = true,
          onSubmit = {},
          onSkip = {},
          onInput = {},
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSummary() {
  HedvigTheme {
    Surface {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        ChatClaimSummary(
          recordingUrl = "",
          displayItems = listOf(
            "Locked" to "Yes",
            "Electric bike" to "Yes",
          ),
          onSubmit = {},
          text = "Is this what you have in mind?",
          isCurrentStep = true,
        )
        ChatClaimOutcome(
          "All done! Here is your submitted claim.",
          onNavigateToClaim = {},
          claimId = "",
        )
      }
    }
  }
}
