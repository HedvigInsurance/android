package com.feature.changeaddress.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feature.changeaddress.ChangeAddressUiState
import com.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.error.ErrorDialog
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
    Box {
      Column {
        val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        TopAppBarWithBack(
          onClick = navigateBack,
          title = "",
          scrollBehavior = topAppBarScrollBehavior,
          colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )
        Spacer(modifier = Modifier.padding(top = 48.dp))
        Text(
          text = "Fyll i din nya address",
          style = MaterialTheme.typography.headlineSmall,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.padding(bottom = 114.dp))
        Column(
          Modifier
            .fillMaxSize()
            .padding(16.dp)
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) {
          if (uiState.isLoading) {
            CircularProgressIndicator()
          }
          Text(
            text = uiState.apartmentOwnerType.input?.toDisplayName() ?: "Bostadstyp",
            modifier = Modifier
              .clickable {
                onClickHousingType()
              }
              .fillMaxWidth(),
          )
          TextField(
            value = uiState.street.input ?: "",
            isError = uiState.street.errorMessage != null,
            supportingText = {
              if (uiState.street.errorMessage != null) {
                Text(text = uiState.street.errorMessage ?: "")
              }
            },
            label = {
              Text(text = "Address")
            },
            onValueChange = { viewModel.onStreetChanged(it) },
            modifier = Modifier.fillMaxWidth(),
          )

          TextField(
            value = uiState.postalCode.input ?: "",
            isError = uiState.postalCode.errorMessage != null,
            supportingText = {
              if (uiState.postalCode.errorMessage != null) {
                Text(text = uiState.postalCode.errorMessage ?: "")
              }
            },
            label = {
              Text(text = "Postkod")
            },
            onValueChange = { viewModel.onPostalCodeChanged(it) },
            modifier = Modifier.fillMaxWidth(),
          )

          TextField(
            value = uiState.squareMeters.input ?: "",
            isError = uiState.squareMeters.errorMessage != null,
            supportingText = {
              if (uiState.squareMeters.errorMessage != null) {
                Text(text = uiState.squareMeters.errorMessage ?: "")
              }
            },
            label = {
              Text(text = "Boyta")
            },
            onValueChange = { viewModel.onSquareMetersChanged(it) },
            modifier = Modifier.fillMaxWidth(),
          )

          TextField(
            value = uiState.numberCoInsured.input.toString(),
            isError = uiState.numberCoInsured.errorMessage != null,
            supportingText = {
              if (uiState.numberCoInsured.errorMessage != null) {
                Text(text = uiState.numberCoInsured.errorMessage ?: "")
              }
            },
            label = {
              Text(text = "Antal personer")
            },
            onValueChange = { viewModel.onCoInsuredChanged(it.toInt()) },
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(modifier = Modifier.padding(top = 6.dp))
          AddressInfoCard()
          Spacer(modifier = Modifier.padding(top = 6.dp))
          LargeContainedButton(
            onClick = { viewModel.onSaveNewAddress() },
            modifier = Modifier,
          ) {
            Text(text = "Spara och fortsätt")
          }
        }
      }
    }
  }
}
