package com.hedvig.android.feature.changeaddress.destination

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.UiMoney
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.error.ErrorDialog
import com.hedvig.android.feature.changeaddress.ChangeAddressUiState
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.data.Address
import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.ui.AddressInfoCard
import com.hedvig.android.feature.changeaddress.ui.QuoteCard
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.type.CurrencyCode
import com.hedvig.android.feature.changeaddress.ui.offer.OfferContent

@Composable
internal fun ChangeAddressOfferDestination(
  viewModel: ChangeAddressViewModel,
  openChat: () -> Unit,
  navigateBack: () -> Unit,
  onChangeAddressResult: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChangeAddressOfferScreen(
    uiState = uiState,
    openChat = openChat,
    navigateBack = navigateBack,
    onChangeAddressResult = onChangeAddressResult,
    onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
    onExpandQuote = viewModel::onExpandQuote,
    onConfirmMove = viewModel::onConfirmMove,
  )
}

@Composable
private fun ChangeAddressOfferScreen(
  uiState: ChangeAddressUiState,
  openChat: () -> Unit,
  navigateBack: () -> Unit,
  onChangeAddressResult: () -> Unit,
  onErrorDialogDismissed: () -> Unit,
  onExpandQuote: (MoveQuote) -> Unit,
  onConfirmMove: (MoveIntentId) -> Unit,
) {
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
      onDismiss = onErrorDialogDismissed,
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

    OfferContent(
      scrollState = scrollState,
      quotes = quotes,
      onQuoteClicked = { viewModel.onExpandQuote(it) },
      onConfirmMoveClicked = { viewModel.onConfirmMove(moveIntentId) },
      openChat = openChat,
    )
    Column(Modifier.verticalScroll(scrollState)) {
      Spacer(modifier = Modifier.padding(bottom = 58.dp))

      Text(
        text = stringResource(hedvig.resources.R.string.CHANGE_ADDRESS_ACCEPT_QUOTES_TITLE),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.padding(bottom = 64.dp))

      uiState.quotes.map { quote ->
        QuoteCard(
          movingDate = uiState.movingDate.input?.toString(),
          quote = quote,
          onExpandClicked = { onExpandQuote(quote) },
          isExpanded = quote.isExpanded,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
      }

      AddressInfoCard(modifier = Modifier.padding(horizontal = 16.dp))
      Spacer(modifier = Modifier.padding(bottom = 8.dp))
      LargeContainedButton(
        onClick = { onConfirmMove(moveIntentId) },
        shape = MaterialTheme.shapes.squircle,
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

@HedvigPreview
@Composable
private fun PreviewChangeAddressOfferScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChangeAddressOfferScreen(
        ChangeAddressUiState(
          moveIntentId = MoveIntentId(""),
          quotes = List(2) { index ->
            MoveQuote(
              MoveIntentId(index.toString()),
              Address(
                id = AddressId(index.toString()),
                apartmentNumber = "1231" + index.toString(),
                postalCode = "Postal Code",
                street = "Street",
              ),
              1,
              UiMoney(99.0 * (index + 1), CurrencyCode.SEK),
              Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
              "1",
              index == 0,
            )
          },
        ),
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
