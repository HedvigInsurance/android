package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.ChosenState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigToggle
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDefaultStyleSize.Small
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle
import com.hedvig.android.design.system.hedvig.api.HedvigSelectableDates
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.AddBottomSheetContentState
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.InfoFromSsn
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.ManualInfo
import hedvig.resources.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun AddCoInsuredBottomSheetContent(
  bottomSheetState: AddBottomSheetContentState,
  onSsnChanged: (String) -> Unit,
  onContinue: () -> Unit,
  onDismiss: () -> Unit,
  onManualInputSwitchChanged: (Boolean) -> Unit,
  onBirthDateChanged: (LocalDate) -> Unit,
  onFirstNameChanged: (String) -> Unit,
  onLastNameChanged: (String) -> Unit,
  onAddNewCoInsured: () -> Unit,
  onCoInsuredSelected: (CoInsured) -> Unit,
) {
  Column(
    modifier = Modifier
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
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
          birthDate = bottomSheetState.manualInfo.birthDate,
          firstName = bottomSheetState.manualInfo.firstName,
          lastName = bottomSheetState.manualInfo.lastName,
          errorMessage = bottomSheetState.errorMessage,
          onBirthDateChanged = onBirthDateChanged,
          onFirstNameChanged = onFirstNameChanged,
          onLastNameChanged = onLastNameChanged,
        )
      }
      AnimatedVisibility(visible = !bottomSheetState.showManualInput) {
        FetchFromSsnFields(
          displayName = bottomSheetState.displayName,
          ssn = bottomSheetState.infoFromSsn.ssn,
          errorMessage = bottomSheetState.errorMessage,
          onSsnChanged = onSsnChanged,
          onContinue = onContinue,
        )
      }
      Spacer(Modifier.height(4.dp))
      HedvigToggle(
        turnedOn = bottomSheetState.showManualInput,
        onClick = {
          onManualInputSwitchChanged(it)
        },
        modifier = Modifier.fillMaxWidth(),
        toggleStyle = ToggleStyle.Default(Small),
        labelText = stringResource(id = R.string.CONTRACT_ADD_COINSURED_NO_SSN),
        enabled = true,
      )
      AnimatedVisibility(bottomSheetState.showUnderAgedInfo) {
        Column {
          Spacer(Modifier.height(4.dp))
          HedvigNotificationCard(
            message = stringResource(
              id = R.string.COINSURED_WITHOUT_SSN_INFO,
            ),
            priority = NotificationDefaults.NotificationPriority.Attention,
          )
        }
      }
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(id = bottomSheetState.getSaveLabel().stringRes()),
      enabled = bottomSheetState.canContinue(),
      onClick = onContinue,
      isLoading = bottomSheetState.isLoading,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      onClick = onDismiss,
      text = stringResource(R.string.general_cancel_button),
      enabled = true,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
internal fun SelectableCoInsuredList(
  selectableCoInsured: List<CoInsured>,
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
    HedvigNotificationCard(
      message = errorMessage ?: "",
      modifier = Modifier.fillMaxWidth(),
      priority = NotificationDefaults.NotificationPriority.Attention,
    )
  }
  HedvigTextButton(
    text = stringResource(id = R.string.GENERAL_ADD_NEW),
    onClick = { onAddNewCoInsured() },
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun SelectableHedvigCard(text: String, isSelected: Boolean, onClick: () -> Unit) {
  RadioOption(
    chosenState = if (isSelected) ChosenState.Chosen else ChosenState.NotChosen,
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
  ) {
    HedvigText(
      text = text,
    )
  }
}

@Composable
private fun FetchFromSsnFields(
  ssn: String?,
  displayName: String,
  errorMessage: String?,
  onSsnChanged: (String) -> Unit,
  onContinue: () -> Unit,
) {
  var ssnInput by remember { mutableStateOf(ssn ?: "") }
  val mask = stringResource(id = R.string.edit_coinsured_ssn_placeholder)
  val maskColor = HedvigTheme.colorScheme.textSecondary
  Column {
    HedvigTextField(
      text = ssnInput,
      labelText = stringResource(id = R.string.CONTRACT_PERSONAL_IDENTITY),
      onValueChange = { value ->
        if (value.length <= 12) {
          onSsnChanged(value)
          ssnInput = value
        }
      },
      textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
      errorState = if (errorMessage != null) {
        HedvigTextFieldDefaults.ErrorState.Error.WithMessage(stringResource(R.string.something_went_wrong))
      } else {
        HedvigTextFieldDefaults.ErrorState.NoError
      },
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
        text = displayName,
        onValueChange = {},
        labelText = stringResource(id = R.string.FULL_NAME_TEXT),
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
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
  firstName: String?,
  lastName: String?,
  onBirthDateChanged: (LocalDate) -> Unit,
  onFirstNameChanged: (String) -> Unit,
  onLastNameChanged: (String) -> Unit,
  errorMessage: String?,
) {
  var firstNameInput by remember { mutableStateOf(firstName ?: "") }
  var lastNameInput by remember { mutableStateOf(lastName ?: "") }

  Column {
    DatePickerWithDialog(
      onSave = onBirthDateChanged,
      birthDate = birthDate,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(4.dp))
    Row {
      HedvigTextField(
        text = firstNameInput,
        labelText = stringResource(id = R.string.CONTRACT_FIRST_NAME),
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        onValueChange = {
          onFirstNameChanged(it)
          firstNameInput = it
        },
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Words,
          keyboardType = KeyboardType.Text,
        ),
        modifier = Modifier.weight(1f).requiredHeight(64.dp),
      )
      Spacer(Modifier.width(4.dp))
      HedvigTextField(
        text = lastNameInput,
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        labelText = stringResource(id = R.string.CONTRACT_LAST_NAME),
        onValueChange = {
          onLastNameChanged(it)
          lastNameInput = it
        },
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Words,
          keyboardType = KeyboardType.Text,
        ),
        modifier = Modifier.weight(1f).requiredHeight(64.dp),
      )
    }
    AnimatedVisibility(
      visible = errorMessage != null,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Column {
        Spacer(Modifier.height(4.dp))
        HedvigNotificationCard(
          message = stringResource(R.string.something_went_wrong),
          priority = NotificationDefaults.NotificationPriority.Attention,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
internal fun DatePickerWithDialog(birthDate: LocalDate?, onSave: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
  val selectedDateMillis = birthDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
    ?: Clock.System.now().toEpochMilliseconds()
  val locale = getLocale()
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  val rememberHedvigDateTimeFormatter = remember(locale) {
    HedvigDateTimeFormatterDefaults.dateMonthAndYear(locale)
  }
  val datePickerState by remember {
    mutableStateOf(
      HedvigDatePickerState(
        locale = locale,
        initialSelectedDateMillis = selectedDateMillis,
        initialDisplayedMonthMillis = selectedDateMillis,
        selectableDates = object : HedvigSelectableDates {
          override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
          }

          override fun isSelectableYear(year: Int): Boolean =
            year <= Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        },
      ),
    )
  }
  if (showDatePicker) {
    HedvigDatePicker(
      datePickerState = datePickerState,
      onDismissRequest = { showDatePicker = false },
      onConfirmRequest = {
        val selected = datePickerState.selectedDateMillis
        if (selected != null) {
          val date = Instant.fromEpochMilliseconds(selected)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
          onSave(date)
        }
        showDatePicker = false
      },
    )
  }
  HedvigCard(
    onClick = { showDatePicker = true },
    modifier = modifier,
    shape = HedvigTheme.shapes.cornerLarge,
  ) {
    Column(Modifier.padding(top = 4.dp, bottom = 8.dp)) {
      val paddingForBirthDate = if (birthDate == null) {
        PaddingValues(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 13.dp)
      } else {
        PaddingValues(horizontal = 16.dp)
      }
      if (birthDate != null) {
        HedvigText(
          text = stringResource(id = R.string.CONTRACT_BIRTH_DATE),
          color = HedvigTheme.colorScheme.textSecondary,
          fontSize = HedvigTheme.typography.label.fontSize,
          modifier = Modifier.padding(
            horizontal = 16.dp,
          ),
        )
      }
      HedvigText(
        text = if (birthDate != null) {
          rememberHedvigDateTimeFormatter.format(birthDate.toJavaLocalDate())
        } else {
          stringResource(id = R.string.CONTRACT_BIRTH_DATE)
        },
        color = if (birthDate != null) {
          Color.Unspecified
        } else {
          HedvigTheme.colorScheme.textSecondaryTranslucent
        },
        modifier = Modifier.padding(
          paddingForBirthDate,
        ),
      )
    }
  }
}

private fun AddBottomSheetContentState.SaveButtonLabel.stringRes() = when (this) {
  AddBottomSheetContentState.SaveButtonLabel.FETCH_INFO -> R.string.CONTRACT_SSN_FETCH_INFO
  AddBottomSheetContentState.SaveButtonLabel.ADD -> R.string.CONTRACT_ADD_COINSURED
}

@Composable
@HedvigShortMultiScreenPreview
private fun AddCoInsuredBottomSheetContentPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddCoInsuredBottomSheetContent(
        bottomSheetState = AddBottomSheetContentState(
          errorMessage = "Error",
          manualInfo = ManualInfo(),
          infoFromSsn = InfoFromSsn(),
          selectableCoInsured = listOf(
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
        onDismiss = {},
        onManualInputSwitchChanged = {},
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
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddCoInsuredBottomSheetContent(
        bottomSheetState = AddBottomSheetContentState(
          errorMessage = "errorMessage",
          showUnderAgedInfo = true,
          showManualInput = true,
          infoFromSsn = InfoFromSsn(),
          manualInfo = ManualInfo(
            birthDate = null,
            // birthDate = LocalDate(2016, 7, 28),
          ),
        ),
        onSsnChanged = {},
        onContinue = {},
        onDismiss = {},
        onManualInputSwitchChanged = {},
        onBirthDateChanged = {},
        onFirstNameChanged = {},
        onLastNameChanged = {},
        onAddNewCoInsured = {},
        onCoInsuredSelected = {},
      )
    }
  }
}
