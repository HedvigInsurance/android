package com.hedvig.feature.claim.chat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.eygraber.uri.Uri
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.compose.photo.capture.state.rememberGetMultipleContentsResultLauncher
import com.hedvig.android.compose.photo.capture.state.rememberPhotoCaptureState
import com.hedvig.android.compose.photo.capture.state.rememberPickMultipleVisualMediaResultLauncher
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DatePickerUiState
import com.hedvig.android.design.system.hedvig.DatePickerWithDialog
import com.hedvig.android.design.system.hedvig.File
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
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
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.api.previewCommonLocale
import com.hedvig.android.design.system.hedvig.icon.Camera
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.Document
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Image
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.ui.claimflow.HedvigChip
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.audio.player.data.SignedAudioUrl
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.ui.audiorecording.AudioRecorderBubble

import hedvig.resources.A11Y_AUDIO_RECORDING
import hedvig.resources.EMBARK_SUBMIT_CLAIM
import hedvig.resources.GENERAL_NO
import hedvig.resources.GENERAL_REMOVE
import hedvig.resources.GENERAL_YES
import hedvig.resources.Res
import hedvig.resources.claim_status_claim_details_title
import hedvig.resources.claim_status_detail_add_files
import hedvig.resources.claim_status_detail_add_more_files
import hedvig.resources.file_upload_choose_files
import hedvig.resources.file_upload_photo_library
import hedvig.resources.file_upload_take_photo
import hedvig.resources.general_save_button
import kotlin.time.Clock
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContentSelectChips(
  options: List<StepContent.ContentSelect.Option>,
  selectedOption: StepContent.ContentSelect.Option?,
  onOptionClick: (StepContent.ContentSelect.Option) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier.semantics {
      selectableGroup()
    },
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    for (item in options) {
      key(item) {
        val isPreview = LocalInspectionMode.current
        val showChipAnimatable = remember {
          Animatable(1.0f)
        }
        HedvigChip(
          item = item,
          showChipAnimatable = showChipAnimatable,
          itemDisplayName = {
            item.title
          },
          isSelected = item == selectedOption,
          onItemClick = onOptionClick,
        )
      }
    }
  }
}

@Composable
internal fun MemberSentAnswer(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)?, // expecting it to be instead edit button
  content: @Composable () -> Unit,
) {
  Surface(
    modifier
      .then(
        if (onClick != null) Modifier.clickable(
          onClick = onClick,
        ) else Modifier,
      ),
    shape = HedvigTheme.shapes.cornerLarge,
    color = HedvigTheme.colorScheme.buttonSecondaryResting,
  ) {
    Column(
      Modifier.padding(
        top = 7.dp, start = 14.dp, end = 14.dp, bottom = 9.dp,
      ),
    ) {
      content()
    }
  }
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
          "Hedvig AI Assistant", // todo
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        )
      }
    }
  }
}

@Composable
internal fun YesNoBubble(
  questionText: String,
  answerSelected: String?,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val options = listOf(
    StepContent.ContentSelect.Option(
      "true", stringResource(Res.string.GENERAL_YES),
    ),
    StepContent.ContentSelect.Option(
      "false", stringResource(Res.string.GENERAL_NO),
    ),
  )
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.End,
  ) {
    HedvigText(
      style = HedvigTheme.typography.label,
      text = questionText,
    )
    Spacer(Modifier.width(16.dp))
    ContentSelectChips(
      options = options,
      selectedOption = answerSelected?.let {
        if (it == options[0].title) options[0] else options[1]
      },
      onOptionClick = { option ->
        onSelect(option.title)
      },
    )
  }
}


@Composable
internal fun SingleSelectBubbleWithDialog(
  questionLabel: String,
  options: List<RadioOption>,
  selectedOptionId: RadioOptionId?,
  onSelect: (RadioOptionId) -> Unit,
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
  HedvigBigCard(
    onClick = { showDialog = true },
    labelText = questionLabel,
    inputText = options.firstOrNull {
      it.id == selectedOptionId
    }?.text,
    modifier = modifier,
    enabled = true,
  )
}

@Composable
internal fun MultiSelectBubbleWithDialog(
  questionLabel: String,
  options: List<RadioOption>,
  selectedOptionIds: List<RadioOptionId>,
  onSelect: (RadioOptionId) -> Unit,
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
      buttonText = stringResource(Res.string.general_save_button),
    )
  }
  HedvigBigCard(
    onClick = { showDialog = true },
    labelText = questionLabel,
    inputText = when {
      selectedOptionIds.isEmpty() -> null
      else -> options.filter { it.id in selectedOptionIds }
        .joinToString(transform = RadioOption::text)
    },
    modifier = modifier,
    enabled = true,
  )
}

@Composable
internal fun UploadFilesBubble(
  addLocalFile: (uri: Uri) -> Unit,
  onRemoveFile: (fileId: String) -> Unit,
  appPackageId: String,
  localFiles: List<UiFile>,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (
    imageUrl: String,
    cacheKey: String,
  ) -> Unit,
  modifier: Modifier = Modifier,
) {
  val fileTypeSelectBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  val photoCaptureState = rememberPhotoCaptureState(appPackageId = appPackageId) { uri ->
    addLocalFile(uri)
  }
  val photoPicker = rememberPickMultipleVisualMediaResultLauncher { resultingUriList ->
    for (resultingUri in resultingUriList) {
      addLocalFile(resultingUri)
    }
  }
  val filePicker = rememberGetMultipleContentsResultLauncher { resultingUriList ->
    for (resultingUri in resultingUriList) {
      addLocalFile(resultingUri)
    }
  }
  FilePickerBottomSheet(
    sheetState = fileTypeSelectBottomSheetState,
    onPickPhoto = {
      photoPicker.launch()
      fileTypeSelectBottomSheetState.dismiss()
    },
    onPickFile = {
      filePicker.launch()
      fileTypeSelectBottomSheetState.dismiss()
    },
    onTakePhoto = {
      photoCaptureState.launchTakePhotoRequest()
      fileTypeSelectBottomSheetState.dismiss()
    },
  )
  UploadFilesBubbleContent(
    onAddFilesButtonClick = {
      fileTypeSelectBottomSheetState.show()
    },
    onRemoveFile = onRemoveFile,
    localFiles = localFiles,
    imageLoader = imageLoader,
    onNavigateToImageViewer = onNavigateToImageViewer,
    modifier = modifier,
  )
}

@Composable
private fun UploadFilesBubbleContent(
  onRemoveFile: ((fileId: String) -> Unit)?,
  onAddFilesButtonClick: () -> Unit,
  localFiles: List<UiFile>,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    if (localFiles.isNotEmpty()) {
      FilesRow(
        uiFiles = localFiles,
        onRemoveFile = onRemoveFile,
        imageLoader = imageLoader,
        onNavigateToImageViewer = onNavigateToImageViewer,
        alignment = Alignment.Start,
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      buttonStyle = if (localFiles.isNotEmpty()) ButtonDefaults.ButtonStyle.Ghost else
        ButtonDefaults.ButtonStyle.Primary,
      text = if (localFiles.isNotEmpty()) stringResource(Res.string.claim_status_detail_add_more_files)
      else stringResource(Res.string.claim_status_detail_add_files),
      onClick = onAddFilesButtonClick,
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
internal fun FilesRow(
  uiFiles: List<UiFile>,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onRemoveFile: ((fileId: String) -> Unit)?,
  alignment: Alignment.Horizontal,
) {
  LazyRow(
    Modifier
      .fillMaxWidth()
      .height(120.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment),
    contentPadding = PaddingValues(top = 8.dp),
  ) {
    items(
      items = uiFiles,
      key = { it.id },
    ) { uiFile ->
      File(
        id = uiFile.id,
        name = uiFile.name,
        path = uiFile.localPath ?: uiFile.url,
        mimeType = uiFile.mimeType,
        imageLoader = imageLoader,
        onRemoveFile = onRemoveFile,
        onClickFile = {
          onNavigateToImageViewer(it, it)
        },
        onNavigateToImageViewer = onNavigateToImageViewer,
      )
    }
  }
}

@Composable
private fun FilePickerBottomSheet(
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
      HedvigButton(
        onClick = onPickPhoto,
        true,
        modifier = Modifier.fillMaxWidth(),
        buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
      ) {
        Row {
          Icon(HedvigIcons.Image, null)
          Spacer(Modifier.width(8.dp))
          HedvigText(stringResource(Res.string.file_upload_photo_library))
        }
      }
      HedvigButton(
        onClick = onTakePhoto,
        true,
        modifier = Modifier.fillMaxWidth(),
        buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
      ) {
        Row {
          Icon(HedvigIcons.Camera, null)
          Spacer(Modifier.width(8.dp))
          HedvigText(stringResource(Res.string.file_upload_take_photo))
        }
      }
      HedvigButton(
        onClick = onPickFile,
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
        buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
      ) {
        Row {
          Icon(HedvigIcons.Document, null)
          Spacer(Modifier.width(8.dp))
          HedvigText(stringResource(Res.string.file_upload_choose_files))
        }
      }
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
internal fun DeflectBubble(
  title: String,
  description: String,
  faq: List<Pair<String, String>>,
  openUrl: (String) -> Unit,
  partners: List<DeflectPartner>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.padding(16.dp),
    horizontalAlignment = Alignment.End,
  ) {
    FlowHeading(title, description)
    partners.forEach { partner ->
      Spacer(Modifier.height(16.dp))
      HedvigCard(
        Modifier.fillMaxWidth(),
      ) {
        Column(
          Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          if (partner.title != null) {
            HedvigText(partner.title)
          }
          if (partner.description != null) {
            HedvigText(partner.description)
          }
          if (partner.url != null && partner.buttonTitle != null) {
            Spacer(Modifier.height(8.dp))
            HedvigButton(
              buttonSize = ButtonDefaults.ButtonSize.Medium,
              enabled = true,
              text = partner.buttonTitle,
              onClick = {
                openUrl(partner.url)
              },
            )
          }
        }
      }
    }
    if (faq.isNotEmpty()) {
      Spacer(Modifier.height(16.dp))
      AccordionList(
        faq.map { faqItem ->
          AccordionData(title = faqItem.first, description = faqItem.second)
        },
      )
    }
  }
}

@Serializable
data class DeflectPartner(
  val id: String,
  val title: String?,
  val description: String?,
  val imageUrl: String,
  val phoneNumber: String?,
  val url: String?,
  val buttonTitle: String?,
  val info: String?,
)

@Composable
internal fun DateSelectBubble(
  datePickerState: DatePickerUiState,
  questionLabel: String?,
  modifier: Modifier = Modifier,
) {
  DatePickerWithDialog(
    datePickerState,
    canInteract = true,
    startText = questionLabel ?: "", // todo
    modifier = modifier,
  )
}

@Composable
internal fun TextInputBubble(
  questionLabel: String,
  text: String?,
  suffix: String?,
  onInput: (String?) -> Unit,
  modifier: Modifier = Modifier,
  keyboardType: KeyboardType = KeyboardType.Unspecified,
) {
  val focusRequester = remember { FocusRequester() }
  var textValue by rememberSaveable {
    mutableStateOf(
      text ?: "",
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
    modifier = modifier.focusRequester(focusRequester),
    enabled = true,
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
                stringResource(Res.string.GENERAL_REMOVE),
              )
            }
          }
        }
      }
    },
    keyboardOptions = KeyboardOptions(
      autoCorrectEnabled = false,
      imeAction = ImeAction.Done,
      keyboardType = keyboardType,
    ),
    keyboardActions = KeyboardActions(
      onDone = {
        focusManager.clearFocus()
      },
    ),
  )
}

@Composable
internal fun ChatClaimSummary(
  text: String,
  recordingUrls: List<String>,
  fileUploads: List<UiFile>,
  displayItems: List<Pair<String, String>>,
  onSubmit: () -> Unit,
  isCurrentStep: Boolean,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(text)
    Spacer(Modifier.height(8.dp))
    HedvigCard(
      color = HedvigTheme.colorScheme.fillNegative,
    ) {
      Column(Modifier.padding(16.dp)) {
        if (displayItems.isNotEmpty()) {


          HedvigText(
            stringResource(Res.string.claim_status_claim_details_title),
          )
          Spacer(Modifier.height(8.dp))
          CompositionLocalProvider(LocalContentColor provides HedvigTheme.colorScheme.textSecondary) {
            Column(modifier) {
              for (displayItem in displayItems) {
                HorizontalItemsWithMaximumSpaceTaken(
                  spaceBetween = 8.dp,
                  startSlot = {
                    HedvigText(text = displayItem.first)
                  },
                  endSlot = {
                    HedvigText(
                      text = displayItem.second,
                      textAlign = TextAlign.End,
                    )
                  },
                )
              }
            }
          }
        }
        if (recordingUrls.isNotEmpty()) {
          Spacer(Modifier.height(24.dp))
          HedvigText(
            stringResource(Res.string.A11Y_AUDIO_RECORDING),
          )
          Spacer(Modifier.height(8.dp))
          recordingUrls.forEach {
            val audioPlayer = rememberAudioPlayer(
              PlayableAudioSource.RemoteUrl(
                SignedAudioUrl.fromSignedAudioUrlString(it),
              ),
            )
            HedvigAudioPlayer(audioPlayer = audioPlayer)
            Spacer(Modifier.height(8.dp))
          }
        }
        if (fileUploads.isNotEmpty()) {
          FilesRow(
            uiFiles = fileUploads,
            imageLoader = imageLoader,
            onNavigateToImageViewer = onNavigateToImageViewer,
            onRemoveFile = null,
            alignment = Alignment.Start,
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
          text = stringResource(Res.string.EMBARK_SUBMIT_CLAIM),
          enabled = true,
          onClick = onSubmit,
          modifier = Modifier.fillMaxWidth(),
        )
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
          datePickerState = DatePickerUiState(previewCommonLocale, null),
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
          recordingState = AudioRecordingStepState.FreeTextDescription(
            showOverlay = false,
            errorType = null,
          ),
          clock = Clock.System,
          onShouldShowRequestPermissionRationale = {
            false
          },
          startRecording = {},
          stopRecording = {},
          submitAudioFile = {},
          redoRecording = {},
          openAppSettings = {},
          freeTextAvailable = true,
          submitFreeText = {},
          onShowFreeText = {},
          onShowAudioRecording = {},
          onLaunchFullScreenEditText = {},
          canSkip = true,
          onSkip = {},
          freeText = "some not really long free text",
        )
        Spacer(Modifier.height(16.dp))
        YesNoBubble(
          answerSelected = "No",
          onSelect = {},
          questionText = "Was it electric?",
        )
        Spacer(Modifier.height(16.dp))
        SingleSelectBubbleWithDialog(
          questionLabel = "Location",
          options = listOf(
            RadioOption(RadioOptionId("01"), "At home"),
            RadioOption(RadioOptionId("02"), "Outside home"),
          ),
          selectedOptionId = RadioOptionId("01"),
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
          onSelect = {},
        )
        Spacer(Modifier.height(16.dp))
        DateSelectBubble(
          questionLabel = "Date of occurence",
          datePickerState = DatePickerUiState(previewCommonLocale, null),
        )
        Spacer(Modifier.height(16.dp))
        TextInputBubble(
          questionLabel = "Re-purchase price",
          text = "15000",
          suffix = "SEK",
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
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        ChatClaimSummary(
          recordingUrls = listOf("", ""),
          displayItems = listOf(
            "Locked" to "Yes",
            "Electric bike" to "Yes",
          ),
          onSubmit = {},
          text = "Is this what you have in mind?",
          isCurrentStep = true,
          fileUploads = listOf(),
          imageLoader = rememberPreviewImageLoader(),
          onNavigateToImageViewer = { _, _ -> },
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewUploadFilesBubbleContent(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasFiles: Boolean,
) {
  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        UploadFilesBubbleContent(
          modifier = Modifier.height(400.dp),
          onRemoveFile = {},
          localFiles = if (hasFiles) {
            listOf(
              UiFile(
                name = "file",
                localPath = "path",
                mimeType = "image/jpg",
                url = null,
                id = "1",
              ),
            )
          } else {
            emptyList()
          },
          imageLoader = rememberPreviewImageLoader(),
          onNavigateToImageViewer = { _, _ -> },
          onAddFilesButtonClick = {},
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDeflect(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasFaq: Boolean,
) {
  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        DeflectBubble(
          title = "Deflect title",
          description = "Loooooooooooooooooooooong deflect description",
          faq = listOf("Question" to "Answer"),
          openUrl = {},
          partners = listOf(
            DeflectPartner(
              id = "partner1",
              title = "partner1",
              description = "description",
              imageUrl = "",
              phoneNumber = "",
              url = "",
              buttonTitle = "Contact Partner 1",
              info = "Available 24/7 for emergency assistance",
            ),
            DeflectPartner(
              id = "partner2",
              imageUrl = "",
              phoneNumber = null,
              url = "",
              buttonTitle = "Visit Website",
              info = "Online support and resources",
              title = "partner2",
              description = "description",
            ),
          ),
        )
      }
    }
  }
}
