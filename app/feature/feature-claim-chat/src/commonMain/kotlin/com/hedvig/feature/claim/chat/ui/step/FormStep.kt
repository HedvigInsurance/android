package com.hedvig.feature.claim.chat.ui.step

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DatePickerUiState
import com.hedvig.android.design.system.hedvig.DatePickerWithDialog
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.MultiSelectDialog
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.SingleSelectDialog
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.data.FieldId
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepContent.Form.*
import com.hedvig.feature.claim.chat.data.StepId
import com.hedvig.feature.claim.chat.ui.common.EditButton
import com.hedvig.feature.claim.chat.ui.common.RoundCornersPill
import com.hedvig.feature.claim.chat.ui.common.SkippedLabel
import com.hedvig.feature.claim.chat.ui.common.YesNoBubble
import hedvig.resources.CLAIM_CHAT_FORM_NUMBER_MAX_CHAR
import hedvig.resources.CLAIM_CHAT_FORM_NUMBER_MIN_CHAR
import hedvig.resources.CLAIM_CHAT_FORM_REQUIRED_FIELD
import hedvig.resources.GENERAL_REMOVE
import hedvig.resources.Res
import hedvig.resources.claims_skip_button
import hedvig.resources.general_continue_button
import hedvig.resources.general_save_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FormStep(
  itemId: StepId,
  content: StepContent.Form,
  onEvent: (ClaimChatEvent) -> Unit,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  canBeChanged: Boolean,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  firstFieldWithError: Field?,
  modifier: Modifier = Modifier,
) {
  FormContent(
    content = content,
    onSkip = {
      onEvent(ClaimChatEvent.Skip(itemId))
    },
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    canBeChanged = canBeChanged,
    onRegret = {
      onEvent(ClaimChatEvent.ShowConfirmEditDialog(itemId))
    },
    onSelectFieldAnswer = { fieldId, answer ->
      onEvent(ClaimChatEvent.UpdateFieldAnswer(itemId, fieldId, answer))
    },
    onSubmit = {
      onEvent(ClaimChatEvent.SubmitForm(itemId))
    },
    continueButtonLoading = continueButtonLoading,
    skipButtonLoading = skipButtonLoading,
    firstFieldWithError = firstFieldWithError,
    modifier = modifier,
  )
}

@Composable
private fun getErrorText(field: Field): String? {
  return when (field.hasError) {
    FieldError.Missing -> stringResource(Res.string.CLAIM_CHAT_FORM_REQUIRED_FIELD)
    FieldError.LessThanMinValue -> stringResource(Res.string.CLAIM_CHAT_FORM_NUMBER_MIN_CHAR)
    FieldError.BiggerThanMaxValue -> stringResource(Res.string.CLAIM_CHAT_FORM_NUMBER_MAX_CHAR)
    null -> null
  }
}

@Composable
private fun FormContent(
  content: StepContent.Form,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  canBeChanged: Boolean,
  onRegret: () -> Unit,
  onSubmit: () -> Unit,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  onSelectFieldAnswer: (fieldId: FieldId, answer: FieldOption?) -> Unit,
  firstFieldWithError: Field?,
  modifier: Modifier = Modifier,
) {
  val errorDescription = firstFieldWithError?.let { "${getErrorText(it)}: ${it.title}" }
  Column(modifier) {
    if (isCurrentStep) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        content.fields.forEach { field ->
          val errorText = getErrorText(field)
          when (field.type) {
            FieldType.TEXT -> {
              TextInputBubble(
                questionLabel = field.title,
                text = field.selectedOptions.getOrNull(0)?.text,
                suffix = field.suffix,
                onInput = { answer ->
                  onSelectFieldAnswer(
                    field.id,
                    answer?.let { FieldOption(it, it, null) },
                  )
                },
                errorText = errorText,
              )
            }

            FieldType.DATE -> {
              LaunchedEffect(field.datePickerUiState?.datePickerState?.selectedDateMillis) {
                onSelectFieldAnswer(
                  field.id,
                  field.datePickerUiState?.datePickerState?.selectedDateMillis?.let {
                    FieldOption(it.toString(), it.toString(), null)
                  },
                )
              }
              DateSelectBubble(
                questionLabel = field.title,
                datePickerState = field.datePickerUiState!!, // todo - check "!!"
                modifier = Modifier.fillMaxWidth(),
                errorText = errorText,
              )
            }

            FieldType.NUMBER -> {
              TextInputBubble(
                questionLabel = field.title,
                text = field.selectedOptions.getOrNull(0)?.text,
                suffix = field.suffix,
                onInput = { answer ->
                  onSelectFieldAnswer(
                    field.id,
                    answer?.let { FieldOption(it, it, null) },
                  )
                },
                keyboardType = KeyboardType.Number,
                errorText = errorText,
              )
            }

            FieldType.SINGLE_SELECT -> {
              SingleSelectBubbleWithDialog(
                questionLabel = field.title,
                options = field.options.map {
                  RadioOption(
                    id = RadioOptionId(it.value),
                    text = it.text,
                    label = it.subtitle,
                    iconResource = null,
                  )
                },
                selectedOptionId = field.selectedOptions.getOrNull(0)
                  ?.let { selected ->
                    val option =
                      field.options.firstOrNull { it.value == selected.value }
                    if (option != null) {
                      RadioOptionId(option.value)
                    } else {
                      null
                    }
                  },
                onSelect = { optionId ->
                  onSelectFieldAnswer(
                    field.id,
                    field.options.firstOrNull { it.value == optionId.id },
                  )
                },
                modifier = Modifier.fillMaxWidth(),
                errorText = errorText,
              )
            }

            FieldType.MULTI_SELECT -> {
              MultiSelectBubbleWithDialog(
                questionLabel = field.title,
                options = field.options.map {
                  RadioOption(
                    id = RadioOptionId(it.value),
                    text = it.text,
                    label = it.subtitle,
                    iconResource = null,
                  )
                },
                selectedOptionIds = field.selectedOptions.mapNotNull { selected ->
                  field.options.firstOrNull { it.value == selected.value }
                    ?.let { RadioOptionId(it.value) }
                },
                onSelect = { option ->
                  onSelectFieldAnswer(
                    field.id,
                    field.options.firstOrNull {
                      it.value == option.id
                    },
                  )
                },
                modifier = Modifier.fillMaxWidth(),
                errorText = errorText,
              )
            }

            FieldType.BINARY -> {
              YesNoBubble(
                answerSelected = field.selectedOptions.firstOrNull()?.text,
                onSelect = {
                  onSelectFieldAnswer(
                    field.id,
                    FieldOption(it, it, null),
                  )
                },
                questionText = field.title,
                errorText = errorText,
              )
            }

            null -> {
              if (canSkip) {
                onSkip()
              }
            }

            FieldType.SEARCH -> {
              SearchForm(
                suggestedQuery = field.suggestedQuery,
                onQueryChange = {
                  TODO()
                },
                queryResult = TODO(),
                searchNoResult = TODO(),
                selectedOption = TODO(),
                onOptionSelected = TODO(),
                modifier = TODO()
              )
            }
          }
        }
      }
      Spacer(Modifier.height(16.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HedvigButton(
          text = stringResource(Res.string.general_continue_button),
          enabled = !continueButtonLoading,
          isLoading = continueButtonLoading,
          onClick = onSubmit,
          modifier = Modifier.fillMaxWidth().semantics {
            if (errorDescription != null) {
              contentDescription = errorDescription
            }
          },
        )
        if (canSkip) {
          HedvigButton(
            text = stringResource(Res.string.claims_skip_button),
            enabled = !skipButtonLoading,
            onClick = onSkip,
            isLoading = skipButtonLoading,
            modifier = Modifier.fillMaxWidth(),
            buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
          )
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (content.fields.flatMap { it.selectedOptions }.isNotEmpty()) {
          content.fields.forEach { field ->
            val textValue = field.selectedOptions.joinToString { it.text }
            Column(
              Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.End,
            ) {
              if (textValue.isNotEmpty()) {
                RoundCornersPill(
                  onClick = null,
                ) {
                  HedvigText(textValue)
                }
              } else {
                SkippedLabel()
              }
            }
          }
        } else {
          SkippedLabel()
        }
        EditButton(canBeChanged, onRegret)
      }
    }
  }
}

@Composable
internal fun SearchForm(
  queryResult: List<FieldOption>,
  searchNoResult: Unit?,
  suggestedQuery: String?,
  onQueryChange: (String) -> Unit,
  selectedOption: FieldOption,
  onOptionSelected: (FieldOption) -> Unit,
  modifier: Modifier = Modifier
) {
  val crossSellBottomSheetState = rememberHedvigBottomSheetState<String?>()
  HedvigBottomSheet(
    hedvigBottomSheetState = crossSellBottomSheetState,
    content = { suggestedQuery: String? ->
      Column(modifier) {
        if (queryResult.isEmpty() && suggestedQuery.isNullOrEmpty()) {
          Box(Modifier.weight(1f),
            contentAlignment = Alignment.Center) {
            HedvigText("Fill in more details about your item") //todo!!
            HedvigText("Start searching for the item relevant to your claim",
              color = HedvigTheme.colorScheme.textSecondary)//todo!!
          }
        } else if (queryResult.isEmpty()) {
          HedvigText("Nothing found") //todo!!
        } else {
          val radioOptions = queryResult.map { option ->
            RadioOption(
              id = RadioOptionId(option.text),
              text = option.text,
              label = option.subtitle
            )
          }
//          RadioGroup(
//            radioOptions,
//            selectedOptionIds = TODO(),
//            onRadioOptionSelected = TODO(),
//            colors = TODO(),
//            style = TODO(),
//            selectIndicator = TODO(),
//            role = TODO(),
//            modifier = TODO(),
//            disabledOptions = TODO(),
//            enabled = TODO(),
//            textEndContent = TODO(),
//          )
        }
      }
    },
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
  errorText: String? = null,
) {
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
    modifier = modifier,
    enabled = true,
    errorState = if (errorText != null) {
      HedvigTextFieldDefaults.ErrorState.Error.WithMessage(errorText)
    } else {
      HedvigTextFieldDefaults.ErrorState.NoError
    },
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
internal fun DateSelectBubble(
  datePickerState: DatePickerUiState,
  questionLabel: String?,
  modifier: Modifier = Modifier,
  errorText: String? = null,
) {
  val focusManager = LocalFocusManager.current
  Column(modifier) {
    DatePickerWithDialog(
      datePickerState,
      canInteract = true,
      startText = questionLabel ?: "",
      Modifier.fillMaxWidth(),
      onBeforeShow = {
        focusManager.clearFocus()
      },
    )
    AnimatedVisibility(
      // Adding this since datePickerState handles update internally and it's hard to clear the error state as with
      // other fields
      errorText != null && datePickerState.datePickerState.selectedDateMillis == null,
    ) {
      Column {
        Spacer(Modifier.height(4.dp))
        if (errorText != null) {
          HedvigText(
            errorText,
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            modifier = Modifier.padding(start = 16.dp),
          )
        }
      }
    }
  }
}

@Composable
internal fun SingleSelectBubbleWithDialog(
  questionLabel: String,
  options: List<RadioOption>,
  selectedOptionId: RadioOptionId?,
  onSelect: (RadioOptionId) -> Unit,
  modifier: Modifier = Modifier,
  errorText: String? = null,
) {
  val focusManager = LocalFocusManager.current
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
  Column(modifier) {
    HedvigBigCard(
      onClick = {
        focusManager.clearFocus()
        showDialog = true
      },
      labelText = questionLabel,
      inputText = options.firstOrNull {
        it.id == selectedOptionId
      }?.text,
      modifier = Modifier.fillMaxWidth(),
      enabled = true,
    )
    AnimatedVisibility(errorText != null) {
      Column {
        if (errorText != null) {
          Spacer(Modifier.height(4.dp))
          HedvigText(
            errorText,
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            modifier = Modifier.padding(start = 16.dp),
          )
        }
      }
    }
  }
}

@Composable
internal fun MultiSelectBubbleWithDialog(
  questionLabel: String,
  options: List<RadioOption>,
  selectedOptionIds: List<RadioOptionId>,
  onSelect: (RadioOptionId) -> Unit,
  modifier: Modifier = Modifier,
  errorText: String? = null,
) {
  val focusManager = LocalFocusManager.current
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
  Column(modifier) {
    HedvigBigCard(
      onClick = {
        focusManager.clearFocus()
        showDialog = true
      },
      labelText = questionLabel,
      inputText = when {
        selectedOptionIds.isEmpty() -> null

        else -> options.filter { it.id in selectedOptionIds }
          .joinToString(transform = RadioOption::text)
      },
      modifier = Modifier.fillMaxWidth(),
      enabled = true,
    )
    AnimatedVisibility(errorText != null) {
      Column {
        Spacer(Modifier.height(4.dp))
        if (errorText != null) {
          HedvigText(
            errorText,
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            modifier = Modifier.padding(start = 16.dp),
          )
        }
      }
    }
  }
}
