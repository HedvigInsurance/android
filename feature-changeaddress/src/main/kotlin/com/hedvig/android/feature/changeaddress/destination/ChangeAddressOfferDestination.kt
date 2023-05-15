package com.hedvig.android.feature.changeaddress.destination

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.UiMoney
import com.hedvig.android.core.ui.error.ErrorDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.changeaddress.ChangeAddressUiState
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.Address
import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.ui.QuoteCard
import hedvig.resources.R
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.type.CurrencyCode

@Composable
internal fun ChangeAddressOfferDestination(
  viewModel: ChangeAddressViewModel,
  openChat: () -> Unit,
  navigateBack: () -> Unit,
  onChangeAddressResult: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val moveResult = uiState.successfulMoveResult

  LaunchedEffect(moveResult) {
    if (moveResult != null) {
      onChangeAddressResult()
    }
  }
  ChangeAddressOfferScreen(
    uiState = uiState,
    openChat = openChat,
    navigateBack = navigateBack,
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
  onErrorDialogDismissed: () -> Unit,
  onExpandQuote: (MoveQuote) -> Unit,
  onConfirmMove: (MoveIntentId) -> Unit,
) {
  val moveIntentId = uiState.moveIntentId ?: throw IllegalArgumentException("No moveIntentId found!")

  if (uiState.errorMessage != null) {
    ErrorDialog(
      message = uiState.errorMessage,
      onDismiss = onErrorDialogDismissed,
    )
  }

  HedvigScaffold(navigateUp = navigateBack) {
    Spacer(Modifier.height(48.dp))
    Text(
      text = stringResource(R.string.CHANGE_ADDRESS_ACCEPT_QUOTES_TITLE),
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(64.dp))
    for (quote in uiState.quotes) {
      QuoteCard(
        movingDate = uiState.movingDate.input?.toString(),
        quote = quote,
        onExpandClicked = { onExpandQuote(quote) },
        isExpanded = quote.isExpanded,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
//      if (quote.isMovingFlow) { // check if is moving flow or something?
//        AddressInfoCard(
//          text = stringResource(hedvig.resources.R.string.CHANGE_ADDRESS_COVERAGE_INFO_TEXT),
//          modifier = Modifier.padding(horizontal = 16.dp),
//        )
//        Spacer(Modifier.height(16.dp))
//      }
    }
    if (uiState.quotes.size > 1) {
      QuotesPriceSum(
        uiState.quotes,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
    LargeContainedButton(
      onClick = { onConfirmMove(moveIntentId) },
      shape = MaterialTheme.shapes.squircle,
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      Text(text = stringResource(R.string.CHANGE_ADDRESS_ACCEPT_OFFER))
    }

    Text(
      text = "Se vad som ingår",
      modifier = Modifier
        .clip(SquircleShape)
        .clickable { }
        .padding(
          horizontal = 16.dp,
          vertical = 8.dp,
        ),
    )

    Spacer(modifier = Modifier.padding(top = 80.dp))

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
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
          text = stringResource(id = R.string.open_chat),
          color = MaterialTheme.colorScheme.onPrimary,
        )
      }
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun QuotesPriceSum(
  quotes: List<MoveQuote>,
  modifier: Modifier = Modifier,
) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      Text(
        text = "Total",
        fontSize = 18.sp,
      )
    },
    endSlot = {
      val summedPrice = quotes.map(MoveQuote::premium).reduce(UiMoney::plus)
      Text(
        text = stringResource(hedvig.resources.R.string.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL, summedPrice.toString()),
        fontSize = 18.sp,
        textAlign = TextAlign.End,
      )
    },
    modifier = modifier,
  )
}

@HedvigPreview
@Composable
private fun PreviewChangeAddressOfferScreen() {
  HedvigTheme(flipBackgroundAndSurface = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChangeAddressOfferScreen(
        ChangeAddressUiState(
          moveIntentId = MoveIntentId(""),
          quotes = List(2) { index ->
            MoveQuote(
              moveIntentId = MoveIntentId(index.toString()),
              address = Address(
                id = AddressId(index.toString()),
                apartmentNumber = "1" + index.toString(),
                postalCode = "122" + index.toString(),
                street = "Street",
              ),
              numberCoInsured = index + 1,
              premium = UiMoney(99.0 * (index + 1), CurrencyCode.SEK),
              startDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
              termsVersion = (index + 1).toString(),
              index == 0,
            )
          },
          movingDate = ValidatedInput(Clock.System.now().toLocalDateTime(TimeZone.UTC).date),
        ),
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
