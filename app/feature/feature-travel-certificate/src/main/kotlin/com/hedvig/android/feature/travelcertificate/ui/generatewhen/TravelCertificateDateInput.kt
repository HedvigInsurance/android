package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.api.HedvigDisplayMode
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDestination
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputUiState.Success
import hedvig.resources.R
import java.util.Locale
import kotlinx.datetime.LocalDate

@Composable
internal fun TravelCertificateDateInputDestination(
  viewModel: TravelCertificateDateInputViewModel,
  navigateUp: () -> Unit,
  onNavigateToFellowTravellers: (
    TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput,
  ) -> Unit,
  onNavigateToOverview: (TravelCertificateUrl) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TravelCertificateDateInput(
    uiState = uiState,
    reload = { viewModel.emit(TravelCertificateDateInputEvent.RetryLoadData) },
    navigateUp = navigateUp,
    onNavigateToFellowTravellers = onNavigateToFellowTravellers,
    onNavigateToOverview = onNavigateToOverview,
    submitInput = { viewModel.emit(TravelCertificateDateInputEvent.Submit) },
    nullifyPrimaryInput = { viewModel.emit(TravelCertificateDateInputEvent.NullifyPrimaryInput) },
    onEmailChanged = { viewModel.emit(TravelCertificateDateInputEvent.ChangeEmailInput(it)) },
  )
}

@Composable
private fun TravelCertificateDateInput(
  uiState: TravelCertificateDateInputUiState,
  onEmailChanged: (String) -> Unit,
  reload: () -> Unit,
  navigateUp: () -> Unit,
  onNavigateToFellowTravellers: (
    TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput,
  ) -> Unit,
  onNavigateToOverview: (TravelCertificateUrl) -> Unit,
  submitInput: () -> Unit,
  nullifyPrimaryInput: () -> Unit,
) {
  when (uiState) {
    TravelCertificateDateInputUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    TravelCertificateDateInputUiState.Loading -> HedvigFullScreenCenterAlignedProgress()

    is TravelCertificateDateInputUiState.UrlFetched -> {
      LaunchedEffect(Unit) {
        onNavigateToOverview(uiState.travelCertificateUrl)
      }
    }

    is TravelCertificateDateInputUiState.Success -> {
      LaunchedEffect(uiState.primaryInput) {
        if (uiState.errorMessageRes == null) {
          if (uiState.primaryInput != null) {
            onNavigateToFellowTravellers(uiState.primaryInput)
            nullifyPrimaryInput()
          }
        }
      }

      var emailInput by remember {
        mutableStateOf(uiState.email ?: "")
      }

      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        Spacer(Modifier.height(8.dp))
        FlowHeading(
          stringResource(R.string.travel_certificate_when_is_your_trip),
          null,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        MovingDateButton(
          datePickerState = uiState.datePickerState,
          travelDate = uiState.travelDate,
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
          errorText = uiState.errorMessageRes?.let { stringResource(id = it) },
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          stringResource(id = R.string.general_continue_button),
          onClick = submitInput,
          enabled = true,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
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
    labelText = stringResource(R.string.PROFILE_MY_INFO_EMAIL_LABEL),
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
  if (showDatePicker) {
    HedvigDatePicker(
      datePickerState = datePickerState,
      onDismissRequest = { showDatePicker = false },
      onConfirmRequest = { showDatePicker = false },
    )
  }

  // Workaround to get the layout of the hedvigTextField, without the functionality of it. Perhaps room for another
  //  component here
  Box(modifier) {
    HedvigTextField(
      text = travelDate.toString(),
      onValueChange = {},
      readOnly = true,
      enabled = true,
      labelText = stringResource(R.string.travel_certificate_start_date_title),
      textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
      trailingContent = {},
    )
    Box(
      Modifier
        .matchParentSize()
        .clip(HedvigTheme.shapes.cornerLarge)
        .clickable { showDatePicker = true },
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateDateInput() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateDateInput(
        Success(
          "id",
          "emaild",
          hasCoInsured = false,
          datePickerState = HedvigDatePickerState(Locale.ENGLISH, null, null, 2020..2024, HedvigDisplayMode.Picker),
          travelDate = LocalDate(2023, 1, 1),
          daysValid = 40,
          errorMessageRes = null,
          primaryInput = null,
        ),
        {},
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
