package com.example.feature.changeaddress.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feature.changeaddress.ChangeAddressUiState
import com.feature.changeaddress.ChangeAddressViewModel
import com.feature.changeaddress.data.MoveQuote
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack

@Composable
internal fun ChangeAddressEnterNewDestination(
  viewModel: ChangeAddressViewModel,
  navigateBack: () -> Unit,
  onQuotes: (List<MoveQuote>) -> Unit,
  onClickHousingType: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()

  val quotes = uiState.quotes
  LaunchedEffect(quotes) {
    if (quotes.isNotEmpty()) {
      viewModel.onContinueClicked()
      onQuotes(uiState.quotes)
    }
  }

  Surface(Modifier.fillMaxSize()) {
    Box {
      Column {
        val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        TopAppBarWithBack(
          onClick = navigateBack,
          title = "Ny address",
          scrollBehavior = topAppBarScrollBehavior,
        )
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
            text = "Fyll i din nya address",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(modifier = Modifier.padding(bottom = 154.dp))
          Text(
            text = "Bostadstyp",
            modifier = Modifier.clickable {
              onClickHousingType()
            },
          )
          TextField(
            value = uiState.street ?: "",
            label = {
              Text(text = "Address")
            },
            onValueChange = { viewModel.onStreetChanged(it) },
          )

          TextField(
            value = uiState.postalCode ?: "",
            label = {
              Text(text = "Postkod")
            },
            onValueChange = { viewModel.onPostalCodeChanged(it) },
          )

          TextField(
            value = uiState.squareMeters ?: "",
            label = {
              Text(text = "Boyta")
            },
            onValueChange = { viewModel.onSquareMetersChanged(it) },
          )

          TextField(
            value = uiState.numberCoInsured.toString(),
            label = {
              Text(text = "Antal personer")
            },
            onValueChange = { viewModel.onCoInsuredChanged(it.toInt()) },
          )

        }
      }

      LargeContainedButton(
        onClick = { viewModel.onSaveNewAddress() },
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(16.dp),
      ) {
        Text(text = "Spara och fortsätt")
      }
    }
  }
}
