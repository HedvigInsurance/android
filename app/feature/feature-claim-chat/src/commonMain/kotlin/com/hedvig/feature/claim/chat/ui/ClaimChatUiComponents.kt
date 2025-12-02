package com.hedvig.feature.claim.chat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DatePickerUiState
import com.hedvig.android.design.system.hedvig.DatePickerWithDialog
import com.hedvig.android.design.system.hedvig.File
import com.hedvig.android.design.system.hedvig.HedvigBigCard
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
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadFilled
import com.hedvig.android.ui.claimflow.HedvigChip
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.audio.player.data.SignedAudioUrl
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.ui.audiorecording.AudioRecordingStep
import hedvig.resources.CHAT_CONVERSATION_CLAIM_TITLE
import hedvig.resources.CHAT_UPLOAD_PRESS_SEND_LABEL
import hedvig.resources.EMBARK_SUBMIT_CLAIM
import hedvig.resources.GENERAL_NO
import hedvig.resources.GENERAL_REMOVE
import hedvig.resources.GENERAL_YES
import hedvig.resources.Res
import hedvig.resources.claims_skip_button
import hedvig.resources.general_save_button
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
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
          Animatable(if (isPreview) 1.0f else 0.0f)
        }
        LaunchedEffect(Unit) {
          delay(Random.nextDouble(0.3, 0.6).seconds)
          showChipAnimatable.animateTo(
            1.0f,
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioLowBouncy,
              stiffness = Spring.StiffnessLow,
            ),
          )
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
internal fun AudioRecorderBubble(
  recordingState: AudioRecordingStepState,
  freeText: String?,
  clock: Clock,
  onShouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (java.io.File) -> Unit,
  redoRecording: () -> Unit,
  openAppSettings: () -> Unit,
  freeTextAvailable: Boolean,
  submitFreeText: () -> Unit,
  onShowFreeText: () -> Unit,
  onShowAudioRecording: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
  canSkip: Boolean,
  onSkip: () -> Unit,
  isCurrentStep: Boolean,
  modifier: Modifier = Modifier,
) {
  AudioRecordingStep(
    uiState = recordingState,
    freeText = freeText,
    clock = clock,
    shouldShowRequestPermissionRationale = onShouldShowRequestPermissionRationale,
    startRecording = startRecording,
    stopRecording = stopRecording,
    submitAudioFile = submitAudioFile,
    redo = redoRecording,
    openAppSettings = openAppSettings,
    freeTextAvailable = freeTextAvailable,
    submitFreeText = submitFreeText,
    showFreeText = onShowFreeText,
    showAudioRecording = onShowAudioRecording,
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
            labelText = stringResource(Res.string.GENERAL_YES),
            size = HighlightLabelDefaults.HighLightSize.Medium,
            color = if (answerSelected == true) {
              HighlightLabelDefaults.HighlightColor.Green(
                HighlightLabelDefaults.HighlightShade.LIGHT,
              )
            } else {
              HighlightLabelDefaults.HighlightColor.Grey(
                HighlightLabelDefaults.HighlightShade.LIGHT,
              )
            },
            modifier = Modifier.clickable(
              enabled = canBeChanged, // todo
              onClick = {
                onSelect(true)
              },
            ),
          )
          Spacer(Modifier.width(16.dp))
          HighlightLabel(
            labelText = stringResource(Res.string.GENERAL_NO),
            size = HighlightLabelDefaults.HighLightSize.Medium,
            color = if (answerSelected != null && !answerSelected) {
              HighlightLabelDefaults.HighlightColor.Green(
                HighlightLabelDefaults.HighlightShade.LIGHT,
              )
            } else {
              HighlightLabelDefaults.HighlightColor.Grey(
                HighlightLabelDefaults.HighlightShade.MEDIUM,
              )
            },
            modifier = Modifier.clickable(
              enabled = canBeChanged, // todo
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
      buttonText = stringResource(Res.string.general_save_button),
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
private fun UploadFilesBubbleContent(
  isCurrentStep: Boolean,
  canSkip: Boolean,
  canBeChanged: Boolean,
  onSkip: () -> Unit,
  onRemoveFile: (fileId: String) -> Unit,
  onSubmitFiles: () -> Unit,
  onUploadButtonClick: () -> Unit,
  localFiles: List<UiFile>,
  uploadedFiles: List<UiFile>,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  StandardBubble(
    isPrefilledByAI = false,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = {
      onSubmitFiles()
    },
    modifier = modifier,
    selectedAnswer = if ((localFiles + uploadedFiles).isNotEmpty()) Unit else null,
    onSkip = onSkip,
    content = {
      Column {
        val allFiles = localFiles + uploadedFiles
        if (allFiles.isNotEmpty()) {
          LazyRow(
            Modifier
              .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp),
          ) {
            items(
              items = allFiles,
              key = { it.id },
            ) { uiFile ->
              File(
                id = uiFile.id,
                name = uiFile.name,
                path = uiFile.localPath ?: uiFile.url,
                mimeType = uiFile.mimeType,
                imageLoader = imageLoader,
                onRemoveFile = onRemoveFile,
                onClickFile = null,
                onNavigateToImageViewer = onNavigateToImageViewer,
              )
            }
          }
        }
        Row(
          Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.End,
        ) {
          HedvigButton(
            text = "Upload files", // TODO: Use stringResource
            onClick = onUploadButtonClick,
            enabled = isCurrentStep || canBeChanged,
            buttonSize = ButtonDefaults.ButtonSize.Medium,
          )
        }
      }
    },
  )
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
              text =
                partner.buttonTitle,
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
        startText = questionLabel ?: "", // todo
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
          text = stringResource(Res.string.EMBARK_SUBMIT_CLAIM),
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
          text = stringResource(Res.string.CHAT_CONVERSATION_CLAIM_TITLE),
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
            contentDescription = null, // todo
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
              stringResource(Res.string.claims_skip_button),
              onClick = onSkip,
              buttonSize = ButtonDefaults.ButtonSize.Medium,
            )
          }
          Spacer(Modifier.width(16.dp))
          HedvigButton(
            text = stringResource(Res.string.CHAT_UPLOAD_PRESS_SEND_LABEL),
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
