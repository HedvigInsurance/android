package com.hedvig.android.feature.changeaddress.destination

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.error.ErrorDialog
import com.hedvig.android.feature.changeaddress.ChangeAddressUiState
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.ui.AddressInfoCard
import com.hedvig.android.feature.changeaddress.ui.QuoteCard

@Composable
internal fun ChangeAddressOfferDestination(
  viewModel: ChangeAddressViewModel,
  openChat: () -> Unit,
  navigateBack: () -> Unit,
  onChangeAddressResult: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val quotes = uiState.quotes
  val moveIntentId = uiState.moveIntentId ?: throw IllegalArgumentException("No moveIntentId found!")
  val moveResult = uiState.successfulMoveResult

  LaunchedEffect(moveResult) {
    if (moveResult != null) {
      onChangeAddressResult()
    }
  }

  if (uiState.errorMessage != null) {
    ErrorDialog(
      message = uiState.errorMessage,
      onDismiss = { viewModel.onErrorDialogDismissed() },
    )
  }

  val scrollState = rememberScrollState()

  Column(Modifier.fillMaxSize()) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    TopAppBarWithBack(
      onClick = navigateBack,
      title = "",
      scrollBehavior = topAppBarScrollBehavior,
    )

    Column(Modifier.verticalScroll(scrollState)) {
      Spacer(modifier = Modifier.padding(bottom = 58.dp))

      Text(
        text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_ACCEPT_QUOTES_TITLE),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.padding(bottom = 64.dp))

      quotes.map { quote ->
        QuoteCard(
          movingDate = uiState.movingDate.input?.toString(),
          quote = quote,
          onExpandClicked = { viewModel.onExpandQuote(quote) },
          isExpanded = quote.isExpanded,
        )
      }

      Spacer(modifier = Modifier.padding(top = 14.dp))
      AddressInfoCard(modifier = Modifier.padding(horizontal = 16.dp))
      Spacer(modifier = Modifier.padding(bottom = 6.dp))
      LargeContainedButton(
        onClick = { viewModel.onConfirmMove(moveIntentId) },
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        Text(text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_ACCEPT_OFFER))
      }

      Spacer(modifier = Modifier.padding(top = 80.dp))

      Column(
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "Hittar du inte det du söker?",
        )

        Spacer(modifier = Modifier.padding(top = 24.dp))

        TextButton(
          shape = SquircleShape,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          onClick = { openChat() },
        ) {
          Text(
            text = stringResource(id = hedvig.resources.R.string.open_chat),
            color = MaterialTheme.colorScheme.onPrimary,
          )
        }
      }

      Spacer(modifier = Modifier.padding(top = 32.dp))
    }
  }
}
