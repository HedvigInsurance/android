package com.hedvig.android.feature.greencard.ui.dateinput

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.a11y.accessibilityForDatePickerField
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.api.HedvigDisplayMode
import com.hedvig.android.design.system.hedvig.clearFocusOnControlTap
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.greencard.data.GreenCardContractSpecification
import hedvig.resources.CHANGE_ADDRESS_NEW_ADDRESS_LABEL
import hedvig.resources.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL
import hedvig.resources.FULL_NAME_TEXT
import hedvig.resources.GENERAL_REMOVE
import hedvig.resources.Res
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import hedvig.resources.general_error
import hedvig.resources.something_went_wrong
import hedvig.resources.travel_certificate_start_date_title
import java.util.Locale
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GreenCardDateInputDestination(viewModel: GreenCardDateInputViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  GreenCardDateInputScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    reload = { viewModel.emit(GreenCardDateInputEvent.RetryLoadData) },
    onAddCoDriver = { viewModel.emit(GreenCardDateInputEvent.AddCoDriver) },
    onRemoveCoDriver = { viewModel.emit(GreenCardDateInputEvent.RemoveCoDriver(it)) },
    onDateChanged = { viewModel.emit(GreenCardDateInputEvent.ClearDateError) },
    onDismissCreateError = { viewModel.emit(GreenCardDateInputEvent.DismissCreateError) },
    submit = { viewModel.emit(GreenCardDateInputEvent.Submit) },
  )
}

@Composable
private fun GreenCardDateInputScreen(
  uiState: GreenCardDateInputUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  onAddCoDriver: () -> Unit,
  onRemoveCoDriver: (CoDriverFieldsState) -> Unit,
  onDateChanged: () -> Unit,
  onDismissCreateError: () -> Unit,
  submit: () -> Unit,
) {
  when (uiState) {
    GreenCardDateInputUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    GreenCardDateInputUiState.Failure -> {
      HedvigScaffold(navigateUp = navigateUp) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    is GreenCardDateInputUiState.Success -> {
      if (uiState.showCreateError) {
        ErrorDialog(
          title = stringResource(Res.string.general_error),
          message = stringResource(Res.string.something_went_wrong),
          onDismiss = onDismissCreateError,
        )
      }
      val form = uiState.form
      HedvigScaffold(navigateUp = navigateUp) {
        Spacer(Modifier.height(8.dp))
        FlowHeading(
          // TODO: Add "When are you travelling?" / "När reser du?" to Lokalise
          "When are you travelling?",
          form.specification.registrationNumber,
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        DateField(
          label = stringResource(Res.string.travel_certificate_start_date_title),
          datePickerState = form.startDatePickerState,
          onDateChanged = onDateChanged,
          errorText = when (uiState.dateError) {
            // TODO: Add "Select a departure date" / "Välj ett avresedatum" to Lokalise
            DateError.MissingStartDate -> "Select a departure date"

            else -> null
          },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(4.dp))
        DateField(
          // TODO: Add "Return date" / "Hemresedatum" to Lokalise
          label = "Return date",
          datePickerState = form.endDatePickerState,
          onDateChanged = onDateChanged,
          errorText = when (uiState.dateError) {
            // TODO: Add "Select a return date" / "Välj ett hemresedatum" to Lokalise
            DateError.MissingEndDate -> "Select a return date"

            // TODO: Add "The return date can't be before the departure date" / "Hemresedatumet kan inte vara före avresedatumet" to Lokalise
            DateError.EndBeforeStart -> "The return date can't be before the departure date"

            else -> null
          },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(24.dp))
        SectionTitle(
          // TODO: Add "Where should we send the green card?" / "Vart ska vi skicka det gröna kortet?" to Lokalise
          title = "Where should we send the green card?",
          // TODO: Add "We'll post the printed card to this address." / "Vi skickar det tryckta kortet till den här adressen." to Lokalise
          subtitle = "We'll post the printed card to this address.",
        )
        Spacer(Modifier.height(16.dp))
        AddressFields(
          address = form.mailingAddress,
          showMissingFieldErrors = uiState.showMissingFieldErrors,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(24.dp))
        SectionTitle(
          // TODO: Add "Co-drivers" / "Medförare" to Lokalise
          title = "Co-drivers",
          // TODO: Add "Add everyone else who will drive the car abroad." / "Lägg till alla andra som ska köra bilen utomlands." to Lokalise
          subtitle = "Add everyone else who will drive the car abroad.",
        )
        form.coDrivers.forEachIndexed { index, coDriver ->
          key(coDriver) {
            Spacer(Modifier.height(16.dp))
            CoDriverFields(
              number = index + 1,
              coDriver = coDriver,
              onRemove = { onRemoveCoDriver(coDriver) },
              showMissingFieldErrors = uiState.showMissingFieldErrors,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }
        }
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          // TODO: Add "Add co-driver" / "Lägg till medförare" to Lokalise
          text = "Add co-driver",
          onClick = onAddCoDriver,
          enabled = true,
          buttonStyle = Secondary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(Res.string.general_continue_button),
          onClick = submit,
          enabled = true,
          isLoading = uiState.isCreating,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun SectionTitle(title: String, subtitle: String, modifier: Modifier = Modifier) {
  Column(
    modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .semantics(mergeDescendants = true) { heading() },
  ) {
    HedvigText(title)
    HedvigText(subtitle, color = HedvigTheme.colorScheme.textSecondary)
  }
}

@Composable
private fun CoDriverFields(
  number: Int,
  coDriver: CoDriverFieldsState,
  onRemove: () -> Unit,
  showMissingFieldErrors: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      HedvigText(
        // TODO: Add "Co-driver %d" / "Medförare %d" to Lokalise
        text = "Co-driver $number",
        modifier = Modifier
          .weight(1f)
          .semantics { heading() },
      )
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = onRemove,
        content = {
          Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = stringResource(Res.string.general_close_button),
          )
        },
      )
    }
    Spacer(Modifier.height(4.dp))
    RequiredTextField(
      state = coDriver.name,
      labelText = stringResource(Res.string.FULL_NAME_TEXT),
      showMissingFieldError = showMissingFieldErrors,
    )
    AddressFields(
      address = coDriver.address,
      showMissingFieldErrors = showMissingFieldErrors,
    )
  }
}

@Composable
private fun AddressFields(
  address: AddressFieldsState,
  showMissingFieldErrors: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    RequiredTextField(
      state = address.street,
      labelText = stringResource(Res.string.CHANGE_ADDRESS_NEW_ADDRESS_LABEL),
      showMissingFieldError = showMissingFieldErrors,
    )
    RequiredTextField(
      state = address.postalCode,
      labelText = stringResource(Res.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL),
      showMissingFieldError = showMissingFieldErrors,
    )
    RequiredTextField(
      state = address.city,
      // TODO: Add "City" / "Ort" to Lokalise
      labelText = "City",
      showMissingFieldError = showMissingFieldErrors,
    )
    RequiredTextField(
      state = address.country,
      // TODO: Add "Country" / "Land" to Lokalise
      labelText = "Country",
      showMissingFieldError = showMissingFieldErrors,
    )
  }
}

@Composable
private fun RequiredTextField(state: TextFieldState, labelText: String, showMissingFieldError: Boolean) {
  HedvigTextField(
    state = state,
    labelText = labelText,
    textFieldSize = Medium,
    errorState = if (showMissingFieldError && state.text.isBlank()) {
      // TODO: Add "Fill in this field" / "Fyll i det här fältet" to Lokalise
      ErrorState.Error.WithMessage("Fill in this field")
    } else {
      ErrorState.NoError
    },
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun DateField(
  label: String,
  datePickerState: HedvigDatePickerState,
  onDateChanged: () -> Unit,
  errorText: String?,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val locale = getLocale()
  val dateFormatter = remember(locale) { HedvigDateTimeFormatterDefaults.dateMonthAndYear(locale) }
  if (showDatePicker) {
    HedvigDatePicker(
      datePickerState = datePickerState,
      onDismissRequest = { showDatePicker = false },
      onConfirmRequest = {
        showDatePicker = false
        onDateChanged()
      },
    )
  }
  val text = datePickerState.selectedDate()?.let { dateFormatter.format(it) }.orEmpty()
  val onClick = {
    focusManager.clearFocus()
    showDatePicker = true
  }
  Box(
    modifier.accessibilityForDatePickerField(
      labelText = label,
      selectedDate = text,
      isEnabled = true,
      onClick = onClick,
    ),
  ) {
    HedvigTextField(
      text = text,
      onValueChange = {},
      readOnly = true,
      enabled = true,
      labelText = label,
      textFieldSize = Medium,
      errorState = if (errorText != null) ErrorState.Error.WithMessage(errorText) else ErrorState.NoError,
      trailingContent = {},
    )
    Box(
      Modifier
        .matchParentSize()
        .clip(HedvigTheme.shapes.cornerLarge)
        .clearFocusOnControlTap()
        .clickable(onClick = onClick),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewGreenCardDateInput() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      GreenCardDateInputScreen(
        uiState = GreenCardDateInputUiState.Success(
          form = GreenCardForm(
            specification = GreenCardContractSpecification(
              contractId = "id",
              registrationNumber = "ABC 123",
              minStartDate = LocalDate(2026, 11, 18),
              maxEndDate = LocalDate(2027, 2, 18),
            ),
            startDatePickerState = HedvigDatePickerState(
              Locale.ENGLISH,
              null,
              null,
              2026..2027,
              HedvigDisplayMode.Picker,
            ),
            endDatePickerState = HedvigDatePickerState(
              Locale.ENGLISH,
              null,
              null,
              2026..2027,
              HedvigDisplayMode.Picker,
            ),
            mailingAddress = AddressFieldsState("Storgatan 1", "111 22", "Stockholm", "Sverige"),
            coDrivers = listOf(CoDriverFieldsState("Anna Andersson")),
          ),
          dateError = DateError.MissingEndDate,
          showMissingFieldErrors = true,
          isCreating = false,
          showCreateError = false,
        ),
        navigateUp = {},
        reload = {},
        onAddCoDriver = {},
        onRemoveCoDriver = {},
        onDateChanged = {},
        onDismissCreateError = {},
        submit = {},
      )
    }
  }
}
