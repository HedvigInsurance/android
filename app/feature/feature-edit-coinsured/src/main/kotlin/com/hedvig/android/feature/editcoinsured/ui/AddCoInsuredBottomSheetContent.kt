package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.SelectableHedvigCard
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.infocard.VectorWarningCard
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.ui.text.WarningTextWithIconForInput
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.AddBottomSheetState
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun AddCoInsuredBottomSheetContent(
  bottomSheetState: AddBottomSheetState,
  onSsnChanged: (String) -> Unit,
  onContinue: () -> Unit,
  onManualInputSwitchChanged: (Boolean) -> Unit,
  onDismiss: () -> Unit,
  onBirthDateChanged: (LocalDate) -> Unit,
  onFirstNameChanged: (String) -> Unit,
  onLastNameChanged: (String) -> Unit,
  onAddNewCoInsured: () -> Unit,
  onCoInsuredSelected: (CoInsured) -> Unit,
) {
  Column(
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(id = R.string.CONTRACT_ADD_COINSURED),
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    if (bottomSheetState.canPickExistingCoInsured() && bottomSheetState.selectableCoInsured != null) {
      SelectableCoInsuredList(
        bottomSheetState.selectableCoInsured,
        bottomSheetState.selectedCoInsured,
        bottomSheetState.errorMessage,
        onAddNewCoInsured,
        onCoInsuredSelected,
      )
    } else {
      AnimatedVisibility(visible = bottomSheetState.showManualInput) {
        ManualInputFields(
          birthDate = bottomSheetState.birthDate,
          errorMessage = bottomSheetState.errorMessage,
          onBirthDateChanged = onBirthDateChanged,
          onFirstNameChanged = onFirstNameChanged,
          onLastNameChanged = onLastNameChanged,
        )
      }
      AnimatedVisibility(visible = !bottomSheetState.showManualInput) {
        FetchFromSsnFields(
          displayName = bottomSheetState.displayName,
          errorMessage = bottomSheetState.errorMessage,
          onSsnChanged = onSsnChanged,
          onContinue = onContinue,
        )
      }
      Spacer(Modifier.height(4.dp))

      HedvigCard(
        onClick = {
          onManualInputSwitchChanged(!bottomSheetState.showManualInput)
        },
        modifier = Modifier.fillMaxWidth(),
      ) {
        HorizontalItemsWithMaximumSpaceTaken(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          startSlot = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = stringResource(id = R.string.CONTRACT_ADD_COINSURED_NO_SSN),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          },
          endSlot = {
            Switch(
              checked = bottomSheetState.showManualInput,
              onCheckedChange = onManualInputSwitchChanged,
              modifier = Modifier.wrapContentSize(Alignment.CenterEnd),
            )
          },
          spaceBetween = 4.dp,
        )
      }
      AnimatedVisibility(bottomSheetState.showUnderAgedInfo) {
        Column {
          Spacer(Modifier.height(4.dp))
          VectorWarningCard(
            text = stringResource(
              id = R.string.COINSURED_WITHOUT_SSN_INFO,
            ),
          )
        }
      }
    }
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(id = bottomSheetState.getSaveLabel().stringRes()),
      enabled = bottomSheetState.canContinue(),
      onClick = onContinue,
      isLoading = bottomSheetState.isLoading,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      onClick = onDismiss,
      text = stringResource(id = R.string.general_cancel_button),
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
internal fun SelectableCoInsuredList(
  selectableCoInsured: ImmutableList<CoInsured>,
  selectedCoInsured: CoInsured?,
  errorMessage: String?,
  onAddNewCoInsured: () -> Unit,
  onCoInsuredSelected: (CoInsured) -> Unit,
) {
  selectableCoInsured.forEach {
    SelectableHedvigCard(
      text = it.displayName,
      isSelected = it == selectedCoInsured,
      onClick = { onCoInsuredSelected(it) },
    )
    Spacer(Modifier.height(4.dp))
  }
  AnimatedVisibility(visible = errorMessage != null) {
    WarningTextWithIconForInput(text = errorMessage ?: "")
  }
  HedvigTextButton(
    text = stringResource(id = R.string.GENERAL_ADD_NEW),
    onClick = { onAddNewCoInsured() },
  )
}

@Composable
private fun FetchFromSsnFields(
  displayName: String,
  errorMessage: String?,
  onSsnChanged: (String) -> Unit,
  onContinue: () -> Unit,
) {
  var ssnInput by remember { mutableStateOf("") }
  val mask = stringResource(id = R.string.edit_coinsured_ssn_placeholder)
  val maskColor = MaterialTheme.colorScheme.onSurfaceVariant
  Column {
    HedvigTextField(
      value = ssnInput,
      label = {
        Text(stringResource(id = R.string.CONTRACT_PERSONAL_IDENTITY))
      },
      onValueChange = {
        if (it.length <= 12) {
          onSsnChanged(it)
          ssnInput = it
        }
      },
      errorText = errorMessage,
      visualTransformation = PersonalNumberVisualTransformation(mask, maskColor),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
      ),
      keyboardActions = KeyboardActions(
        onDone = {
          onContinue()
        },
      ),
      modifier = Modifier.fillMaxWidth(),
    )
    AnimatedVisibility(
      visible = displayName.isNotBlank(),
      modifier = Modifier.padding(top = 4.dp),
    ) {
      HedvigTextField(
        value = displayName,
        onValueChange = {},
        label = {
          Text(stringResource(id = R.string.FULL_NAME_TEXT))
        },
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

private class PersonalNumberVisualTransformation(
  private val mask: String,
  private val maskColor: Color,
) : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 12) text.text.substring(0..11) else text.text

    val annotatedString = buildAnnotatedString {
      for (i in trimmed.indices) {
        append(trimmed[i])
        if (i == 7) {
          append("-")
        }
      }
      withStyle(SpanStyle(color = maskColor)) {
        append(mask.takeLast(mask.length - length))
      }
    }

    val personalNumberOffsetTranslator = object : OffsetMapping {
      override fun originalToTransformed(offset: Int): Int {
        return when {
          offset < 8 -> offset
          offset <= 12 -> offset + 1
          else -> 13
        }
      }

      override fun transformedToOriginal(offset: Int): Int {
        return when {
          offset <= 8 -> offset
          else -> offset - 1
        }.coerceAtMost(text.length)
      }
    }
    return TransformedText(annotatedString, personalNumberOffsetTranslator)
  }
}

@Composable
private fun ManualInputFields(
  birthDate: LocalDate?,
  onBirthDateChanged: (LocalDate) -> Unit,
  onFirstNameChanged: (String) -> Unit,
  onLastNameChanged: (String) -> Unit,
  errorMessage: String?,
) {
  var firstNameInput by remember { mutableStateOf("") }
  var lastNameInput by remember { mutableStateOf("") }

  Column {
    DatePickerWithDialog(
      onSave = onBirthDateChanged,
      birthDate = birthDate,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(4.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigTextField(
          value = firstNameInput,
          label = {
            Text(stringResource(id = R.string.CONTRACT_FIRST_NAME))
          },
          onValueChange = {
            onFirstNameChanged(it)
            firstNameInput = it
          },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            keyboardType = KeyboardType.Text,
          ),
          modifier = Modifier.fillMaxWidth(),
        )
      },
      endSlot = {
        HedvigTextField(
          value = lastNameInput,
          label = {
            Text(stringResource(id = R.string.CONTRACT_LAST_NAME))
          },
          onValueChange = {
            onLastNameChanged(it)
            lastNameInput = it
          },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            keyboardType = KeyboardType.Text,
          ),
          modifier = Modifier.fillMaxWidth(),
        )
      },
      spaceBetween = 4.dp,
    )

    Spacer(Modifier.height(4.dp))
    AnimatedVisibility(
      visible = errorMessage != null,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      WarningTextWithIconForInput(text = errorMessage ?: "")
    }
  }
}

@Composable
internal fun DatePickerWithDialog(birthDate: LocalDate?, onSave: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
  val datePickerState = rememberDatePickerState()

  val selectedDateMillis: Long? = datePickerState.selectedDateMillis
  val locale = getLocale()
  val hedvigDateTimeFormatter = rememberHedvigDateTimeFormatter()
  val selectedDate = remember(locale, selectedDateMillis) {
    if (selectedDateMillis == null) {
      null
    } else {
      Instant.fromEpochMilliseconds(selectedDateMillis)
        .toLocalDateTime(TimeZone.UTC)
        .date
    }
  }

  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            showDatePicker = false
            selectedDate?.let {
              onSave(selectedDate)
            }
          },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.general_save_button))
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showDatePicker = false
          },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.general_cancel_button))
        }
      },
    ) {
      HedvigDatePicker(datePickerState = datePickerState)
    }
  }
  HedvigCard(
    onClick = { showDatePicker = true },
    modifier = modifier,
  ) {
    Column(Modifier.padding(top = 4.dp, bottom = 8.dp)) {
      val paddingForBirthDate = if (birthDate == null) {
        PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
      } else {
        PaddingValues(horizontal = 16.dp)
      }
      if (birthDate != null) {
        Text(
          text = stringResource(id = R.string.CONTRACT_BIRTH_DATE),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = MaterialTheme.typography.labelMedium.fontSize,
          modifier = Modifier.padding(
            horizontal = 16.dp,
          ),
        )
      }
      Text(
        text = if (birthDate != null) {
          hedvigDateTimeFormatter.format(birthDate.toJavaLocalDate())
        } else {
          stringResource(id = R.string.CONTRACT_BIRTH_DATE)
        },
        color = if (birthDate != null) {
          Color.Unspecified
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.padding(
          paddingForBirthDate,
        ),
      )
    }
  }
}

private fun AddBottomSheetState.SaveButtonLabel.stringRes() = when (this) {
  AddBottomSheetState.SaveButtonLabel.FETCH_INFO -> R.string.CONTRACT_SSN_FETCH_INFO
  AddBottomSheetState.SaveButtonLabel.ADD -> R.string.CONTRACT_ADD_COINSURED
}

@Composable
@HedvigPreview
private fun AddCoInsuredBottomSheetContentPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AddCoInsuredBottomSheetContent(
        bottomSheetState = AddBottomSheetState(
          errorMessage = "Error",
          selectableCoInsured = persistentListOf(
            CoInsured(
              "Test",
              "Testersson",
              LocalDate.fromEpochDays(300),
              "1234",
              false,
            ),
            CoInsured(
              "Test",
              "Testersson",
              LocalDate.fromEpochDays(300),
              "1234",
              false,
            ),
          ),
        ),
        onSsnChanged = {},
        onContinue = {},
        onManualInputSwitchChanged = {},
        onDismiss = {},
        onBirthDateChanged = {},
        onFirstNameChanged = {},
        onLastNameChanged = {},
        onAddNewCoInsured = {},
        onCoInsuredSelected = {},
      )
    }
  }
}

@Composable
@HedvigPreview
private fun AddCoInsuredBottomSheetContentWithCoInsuredPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AddCoInsuredBottomSheetContent(
        bottomSheetState = AddBottomSheetState(
          errorMessage = "errorMessage",
          showUnderAgedInfo = true,
          showManualInput = true,
          birthDate = LocalDate(2016, 7, 28),
        ),
        onSsnChanged = {},
        onContinue = {},
        onManualInputSwitchChanged = {},
        onDismiss = {},
        onBirthDateChanged = {},
        onFirstNameChanged = {},
        onLastNameChanged = {},
        onAddNewCoInsured = {},
        onCoInsuredSelected = {},
      )
    }
  }
}
