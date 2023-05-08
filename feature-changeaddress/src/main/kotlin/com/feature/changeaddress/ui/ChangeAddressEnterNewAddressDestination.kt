package com.feature.changeaddress.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feature.changeaddress.ChangeAddressUiState
import com.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.error.ErrorDialog
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import toDisplayName

@Composable
internal fun ChangeAddressEnterNewDestination(
  viewModel: ChangeAddressViewModel,
  navigateBack: () -> Unit,
  onQuotes: () -> Unit,
  onClickHousingType: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()

  val quotes = uiState.quotes
  LaunchedEffect(quotes) {
    if (quotes.isNotEmpty()) {
      onQuotes()
    }
  }

  if (uiState.errorMessage != null) {
    ErrorDialog(
      message = uiState.errorMessage,
      onDismiss = { viewModel.onErrorDialogDismissed() },
    )
  }

  Surface(Modifier.fillMaxSize()) {
    Column {
      val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
      TopAppBarWithBack(
        onClick = navigateBack,
        title = "",
        scrollBehavior = topAppBarScrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
      )
      Column(
        Modifier
          .fillMaxSize()
          .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {

        Spacer(modifier = Modifier.padding(top = 48.dp))
        Text(
          text = stringResource(id = R.string.CHANGE_ADDRESS_ENTER_NEW_ADDRESS_TITLE),
          style = MaterialTheme.typography.headlineSmall,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.padding(bottom = 72.dp))

        if (uiState.isLoading) {
          CircularProgressIndicator()
        }

        HousingTypeButton(onClickHousingType, uiState)

        Spacer(modifier = Modifier.padding(top = 6.dp))

        TextField(
          value = uiState.street.input ?: "",
          isError = uiState.street.errorMessageRes != null,
          supportingText = {
            if (uiState.street.errorMessageRes != null) {
              Text(
                text = uiState.street.errorMessageRes
                  ?.let { stringResource(id = it) }
                  ?: "",
              )
            }
          },
          label = {
            Text(text = stringResource(id = R.string.CHANGE_ADDRESS_NEW_ADDRESS_LABEL))
          },
          onValueChange = { viewModel.onStreetChanged(it) },
          modifier = Modifier.fillMaxWidth(),
        )

        TextField(
          value = uiState.postalCode.input ?: "",
          isError = uiState.postalCode.errorMessageRes != null,
          supportingText = {
            if (uiState.postalCode.errorMessageRes != null) {
              Text(
                text = uiState.postalCode.errorMessageRes
                  ?.let { stringResource(id = it) }
                  ?: "",
              )
            }
          },
          label = {
            Text(text = stringResource(id = R.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL))

          },
          onValueChange = { viewModel.onPostalCodeChanged(it) },
          modifier = Modifier.fillMaxWidth(),
        )

        TextField(
          value = uiState.squareMeters.input ?: "",
          isError = uiState.squareMeters.errorMessageRes != null,
          supportingText = {
            if (uiState.squareMeters.errorMessageRes != null) {
              Text(
                text = uiState.squareMeters.errorMessageRes
                  ?.let { stringResource(id = it) }
                  ?: "",
              )
            }
          },
          label = {
            Text(text = stringResource(id = R.string.CHANGE_ADDRESS_NEW_LIVING_SPACE_LABEL))
          },
          onValueChange = { viewModel.onSquareMetersChanged(it) },
          modifier = Modifier.fillMaxWidth(),
        )

        TextField(
          value = uiState.numberCoInsured.input.toString(),
          isError = uiState.numberCoInsured.errorMessageRes != null,
          supportingText = {
            if (uiState.numberCoInsured.errorMessageRes != null) {
              Text(
                text = uiState.numberCoInsured.errorMessageRes
                  ?.let { stringResource(id = it) }
                  ?: "",
              )
            }
          },
          label = {
            Text(text = stringResource(id = R.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL))
          },
          onValueChange = { viewModel.onCoInsuredChanged(it.toInt()) },
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.padding(top = 4.dp))
        MovingDateButton(
          onDateSelected = { viewModel.onMoveDateSelected(it) },
          uiState = uiState,
        )
        Spacer(modifier = Modifier.padding(top = 6.dp))
        AddressInfoCard(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.padding(bottom = 6.dp))
        LargeContainedButton(
          onClick = { viewModel.onSaveNewAddress() },
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 22.dp),
        ) {
          Text(text = stringResource(id = R.string.SAVE_AND_CONTINUE_BUTTON_LABEL))
        }
      }
    }
  }
}

@Composable
private fun HousingTypeButton(
  onClickHousingType: () -> Unit,
  uiState: ChangeAddressUiState,
) {
  Row(
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .fillMaxWidth()
      .background(
        color = Color(0xFFF0F0F0),
        shape = SquircleShape,
      )
      .clickable { onClickHousingType() }
      .padding(vertical = 20.dp, horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = stringResource(
        id = uiState.apartmentOwnerType
          .input
          ?.toDisplayName()
          ?: R.string.CHANGE_ADDRESS_HOUSING_TYPE_LABEL,
      ),
      color = if (uiState.apartmentOwnerType.input == null) {
        Color(0xFF727272)
      } else {
        MaterialTheme.colorScheme.primary
      },
      style = MaterialTheme.typography.headlineSmall,
    )
    Icon(
      painter = painterResource(
        id = com.hedvig.android.core.designsystem.R.drawable.ic_drop_down_indicator,
      ),
      contentDescription = "",
    )
  }
}

@Composable
private fun MovingDateButton(
  onDateSelected: (LocalDate) -> Unit,
  uiState: ChangeAddressUiState,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            uiState.datePickerState.selectedDateMillis?.let {
              val selectedDate = Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
                .date
              uiState.datePickerState.setSelection(it)
              onDateSelected(selectedDate)
            }

            showDatePicker = false
          },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.ALERT_OK))
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showDatePicker = false
          },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.general_close_button))
        }
      },
    ) {
      HedvigDatePicker(
        datePickerState = uiState.datePickerState,
        dateValidator = { true },
      )
    }
  }

  Row(
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .fillMaxWidth()
      .background(
        color = Color(0xFFF0F0F0),
        shape = SquircleShape,
      )
      .clickable {
        showDatePicker = true
      }
      .padding(vertical = 20.dp, horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column {
      Text(
        text = stringResource(
          id = R.string.CHANGE_ADDRESS_MOVING_DATE_LABEL,
        ),
        color = Color(0xFF727272),
        style = MaterialTheme.typography.bodyMedium,
      )
      Text(
        text = uiState.movingDate.input?.toString()
          ?: stringResource(id = R.string.CHANGE_ADDRESS_SELECT_MOVING_DATE_LABEL),
        color = if (uiState.movingDate.input == null) {
          Color(0xFF727272)
        } else {
          MaterialTheme.colorScheme.primary
        },
        style = MaterialTheme.typography.headlineSmall,
      )
    }
    Icon(
      painter = painterResource(
        id = com.hedvig.android.core.designsystem.R.drawable.ic_drop_down_indicator,
      ),
      contentDescription = "",
    )
  }
}
