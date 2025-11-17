package com.hedvig.feature.claim.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.hedvig.android.design.system.hedvig.ButtonDefaults
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
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.SingleSelectDialog
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadFilled
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun AudioRecorderBubble(
  isCurrentStep: Boolean,
  recordingUrl: String?,
  onSubmit: (String) -> Unit,
  canSkip: Boolean,
  canBeChanged: Boolean,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier,
) {
  StandardBubble(
    isPrefilledByAI = false,
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSubmit = onSubmit,
    modifier = modifier,
    onSkip = onSkip,
    selectedAnswer = recordingUrl,
    content = {
      // TODO()
    },
  )
}


@Composable
internal fun AssistantMessageBubble(
  text: String,
  comment: String?,
  isLoading: Boolean,
  isCurrentStep: Boolean,
  modifier: Modifier = Modifier,
) {

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
      Column(Modifier.padding(16.dp)) {
        HedvigText(
          questionLabel,
          style = HedvigTheme.typography.label,
        )
        Spacer(Modifier.height(16.dp))
        Row(
          horizontalArrangement = Arrangement.Start,
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
//      TODO()
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
      var textValue by rememberSaveable { mutableStateOf(text ?: "") }
      val focusManager = LocalFocusManager.current
      HedvigTextField(
        text = textValue,
        trailingContent = {},
        onValueChange = onValueChange@{ newValue ->
          if (newValue.length > 10) return@onValueChange
          if (!newValue.isDigitsOnly()) return@onValueChange
          textValue = newValue
          onInput(newValue.ifBlank { null })
        },
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        labelText = questionLabel,
        modifier = modifier.focusRequester(focusRequester),
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
                    Icon(HedvigIcons.Close, stringResource(R.string.GENERAL_REMOVE))
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
  recordingUrl: String?,
  displayItems: List<Pair<String, String>>,
  onSubmit: (Unit) -> Unit,
  modifier: Modifier = Modifier,
) {

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
  Column(
    modifier,
    horizontalAlignment = Alignment.End,
  ) {
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
    if (canSkip || isCurrentStep) {
      Spacer(Modifier.height(8.dp))
      Row(
        horizontalArrangement = Arrangement.End,
      ) {
        if (canSkip && isCurrentStep) {
          HedvigTextButton(
            stringResource(R.string.claims_skip_button),
            onClick = onSkip,
            buttonSize = ButtonDefaults.ButtonSize.Medium,
          )
        }
        if (isCurrentStep) {
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
private fun PreviewAssistantBubble() {
  HedvigTheme {
    Surface {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        AssistantMessageBubble(
          text = "Specify when and where it happened",
          comment = null,
          isLoading = false,
          isCurrentStep = false,
        )
        AssistantMessageBubble(
          text = "Processing audio information",
          comment = null,
          isLoading = true,
          isCurrentStep = false,
        )
        AssistantMessageBubble(
          text = "Processing audio information",
          comment = "Done!",
          isLoading = false,
          isCurrentStep = false,
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
//        AudioRecorderBubble(
//          isCurrentStep = false,
//          onSubmit = {},
//          canSkip = false,
//          canBeChanged = true,
//          onSkip = {},
//          recordingUrl = ""
//        )
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
//        DateSelectBubble(
//          questionLabel = null,
//          date = LocalDate(2025, 11, 10),
//          isPrefilled = true,
//          isCurrentStep = false,
//          canSkip = false,
//          canBeChanged = true,
//          onSubmit = {},
//          onSkip = {}
//        )
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
          onInput = {}
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
        )
      }
    }
  }
}
