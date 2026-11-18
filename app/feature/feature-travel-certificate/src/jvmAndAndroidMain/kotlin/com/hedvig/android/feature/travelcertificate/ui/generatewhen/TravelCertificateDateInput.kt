package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.NavigationEffect
import com.hedvig.android.core.locale.CommonLocale
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.a11y.accessibilityForDatePickerField
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.api.HedvigSelectableDates
import com.hedvig.android.design.system.hedvig.clearFocusOnControlTap
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputUiState.Success
import hedvig.resources.PROFILE_MY_INFO_EMAIL_LABEL
import hedvig.resources.PROFILE_MY_INFO_INVALID_EMAIL
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import hedvig.resources.something_went_wrong
import hedvig.resources.travel_certificate_email_empty_error
import hedvig.resources.travel_certificate_start_date_title
import hedvig.resources.travel_certificate_when_is_your_trip
import kotlin.time.Instant
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelCertificateDateInputDestination(
  viewModel: TravelCertificateDateInputViewModel,
  navigateUp: () -> Unit,
  onNavigateToTravellers: (TravelCertificateTravellersStep) -> Unit,
  onCertificateCreated: (TravelCertificateUrl) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  DateInputNavigationEffect(
    uiState = uiState,
    onNavigateToTravellers = onNavigateToTravellers,
    onCertificateCreated = onCertificateCreated,
  )
  TravelCertificateDateInput(
    uiState = uiState,
    reload = { viewModel.emit(TravelCertificateDateInputEvent.RetryLoadData) },
    navigateUp = navigateUp,
    submitInput = { viewModel.emit(TravelCertificateDateInputEvent.Submit) },
    onEmailChanged = { viewModel.emit(TravelCertificateDateInputEvent.ChangeEmailInput(it)) },
    onTravelDateSelected = { viewModel.emit(TravelCertificateDateInputEvent.SelectTravelDate(it)) },
  )
}

/** Opens the step in [Success.next], and leaves for the certificate on [TravelCertificateDateInputUiState.Created]. */
@Composable
internal fun DateInputNavigationEffect(
  uiState: TravelCertificateDateInputUiState,
  onNavigateToTravellers: (TravelCertificateTravellersStep) -> Unit,
  onCertificateCreated: (TravelCertificateUrl) -> Unit,
) {
  NavigationEffect((uiState as? Success)?.next, onNavigateToTravellers)
  NavigationEffect((uiState as? TravelCertificateDateInputUiState.Created)?.leave, onCertificateCreated)
}

@Composable
private fun TravelCertificateDateInput(
  uiState: TravelCertificateDateInputUiState,
  onEmailChanged: (String) -> Unit,
  onTravelDateSelected: (LocalDate) -> Unit,
  reload: () -> Unit,
  navigateUp: () -> Unit,
  submitInput: () -> Unit,
) {
  when (uiState) {
    TravelCertificateDateInputUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    TravelCertificateDateInputUiState.Loading,
    is TravelCertificateDateInputUiState.Created,
    -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    is Success -> {
      if (uiState.isCreating) {
        HedvigFullScreenCenterAlignedProgress()
      } else {
        DateInputContent(uiState, onEmailChanged, onTravelDateSelected, navigateUp, submitInput)
      }
    }
  }
}

@Composable
private fun DateInputContent(
  uiState: Success,
  onEmailChanged: (String) -> Unit,
  onTravelDateSelected: (LocalDate) -> Unit,
  navigateUp: () -> Unit,
  submitInput: () -> Unit,
) {
  var emailInput by rememberEmailInput(uiState.email, uiState.emailRevision)
  val locale = getLocale()
  val datePickerState = remember(locale, uiState.minDate, uiState.maxDate) {
    travelDatePickerState(locale, uiState.minDate, uiState.maxDate, uiState.selectedDate)
  }
  val currentOnTravelDateSelected by rememberUpdatedState(onTravelDateSelected)
  LaunchedEffect(datePickerState) {
    snapshotFlow { datePickerState.selectedDateMillis }
      .drop(1)
      .filterNotNull()
      .collect { currentOnTravelDateSelected(utcMillisToDate(it)) }
  }

  HedvigScaffold(
    navigateUp = navigateUp,
  ) {
    Spacer(Modifier.height(8.dp))
    FlowHeading(
      stringResource(Res.string.travel_certificate_when_is_your_trip),
      null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(24.dp))
    MovingDateButton(
      datePickerState = datePickerState,
      travelDate = uiState.selectedDate,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(4.dp))
    EmailTextField(
      email = emailInput,
      onEmailChanged = {
        emailInput = it
        onEmailChanged(it)
      },
      modifier = Modifier.padding(horizontal = 16.dp),
      errorText = when (uiState.emailError) {
        EmailError.Empty -> stringResource(Res.string.travel_certificate_email_empty_error)
        EmailError.Invalid -> stringResource(Res.string.PROFILE_MY_INFO_INVALID_EMAIL)
        null -> null
      },
    )
    if (uiState.createFailed) {
      Spacer(Modifier.height(16.dp))
      HedvigNotificationCard(
        message = stringResource(Res.string.something_went_wrong),
        priority = NotificationPriority.Error,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      stringResource(Res.string.general_continue_button),
      onClick = submitInput,
      enabled = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

/** The field's text: it keeps what the member types and takes [email] only when [emailRevision] changes. */
@Composable
internal fun rememberEmailInput(email: String, emailRevision: Int): MutableState<String> = remember(emailRevision) {
  mutableStateOf(email)
}

@Composable
private fun EmailTextField(
  email: String,
  errorText: String?,
  onEmailChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigTextField(
    text = email,
    onValueChange = {
      onEmailChanged(it)
    },
    errorState = if (errorText != null) {
      ErrorState.Error.WithMessage(errorText)
    } else {
      ErrorState.NoError
    },
    textFieldSize = Medium,
    labelText = stringResource(Res.string.PROFILE_MY_INFO_EMAIL_LABEL),
    modifier = modifier.fillMaxWidth(),
  )
}

@Composable
private fun MovingDateButton(
  datePickerState: HedvigDatePickerState,
  travelDate: LocalDate,
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
      onConfirmRequest = { showDatePicker = false },
    )
  }

  // Workaround to get the layout of the hedvigTextField, without the functionality of it. Perhaps room for another
  //  component here
  val label = stringResource(Res.string.travel_certificate_start_date_title)
  val text = dateFormatter.format(travelDate)
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
private fun PreviewTravelCertificateDateInput(
  @PreviewParameter(
    ChooseInsuranceForAddonUiStateProvider::class,
  ) uiState: TravelCertificateDateInputUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateDateInput(
        uiState,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class ChooseInsuranceForAddonUiStateProvider :
  CollectionPreviewParameterProvider<TravelCertificateDateInputUiState>(
    listOf(
      previewContent,
      TravelCertificateDateInputUiState.Failure,
      TravelCertificateDateInputUiState.Loading,
      previewContent.copy(email = "", emailError = EmailError.Empty, createFailed = true),
    ),
  )

private val previewContent = Success(
  contractId = "id",
  email = "emaild",
  emailRevision = 1,
  emailError = null,
  minDate = LocalDate(2020, 1, 1),
  maxDate = LocalDate(2024, 12, 31),
  pickedDate = LocalDate(2023, 1, 1),
  selectedDate = LocalDate(2023, 1, 1),
  daysValid = 40,
  hasCoInsured = false,
  isCreating = false,
  createFailed = false,
  next = null,
)

/** The picker carries days as epoch millis at UTC midnight, so both directions convert in UTC. */
internal fun travelDatePickerState(
  locale: CommonLocale,
  minDate: LocalDate,
  maxDate: LocalDate,
  selectedDate: LocalDate,
): HedvigDatePickerState {
  val yearRange = minDate.year..maxDate.year
  val selectedDateMillis = selectedDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  return HedvigDatePickerState(
    locale = locale,
    initialSelectedDateMillis = selectedDateMillis,
    initialDisplayedMonthMillis = selectedDateMillis,
    yearRange = yearRange,
    selectableDates = object : HedvigSelectableDates {
      override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcMillisToDate(utcTimeMillis) in minDate..maxDate

      override fun isSelectableYear(year: Int): Boolean = year in yearRange
    },
  )
}

internal fun utcMillisToDate(utcTimeMillis: Long): LocalDate =
  Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC).date
