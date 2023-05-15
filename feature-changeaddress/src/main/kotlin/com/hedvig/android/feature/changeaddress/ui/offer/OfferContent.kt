package com.hedvig.android.feature.changeaddress.ui.offer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.UiMoney
import com.hedvig.android.feature.changeaddress.data.Address
import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.ui.AddressInfoCard
import com.hedvig.android.feature.changeaddress.ui.QuoteCard
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

@Composable
fun OfferContent(
  scrollState: ScrollState,
  quotes: List<MoveQuote>,
  onQuoteClicked: (MoveQuote) -> Unit,
  onConfirmMoveClicked: () -> Unit,
  openChat: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.verticalScroll(scrollState),
  ) {
    Spacer(modifier = Modifier.padding(bottom = 58.dp))

    Text(
      text = stringResource(id = R.string.CHANGE_ADDRESS_ACCEPT_QUOTES_TITLE),
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.padding(bottom = 64.dp))

    quotes.map { quote ->
      QuoteCard(
        movingDate = quote.startDate.toString(),
        quote = quote,
        onExpandClicked = { onQuoteClicked(quote) },
        isExpanded = quote.isExpanded,
      )
    }

    Spacer(modifier = Modifier.padding(top = 14.dp))
    AddressInfoCard(
      text = "Din Olycksfallsförsäkring påverkas när du byter till en ny adress. Ditt pris kan ha ändrats men du behåller samma skydd som tidigare.",
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.padding(bottom = 6.dp))
    LargeContainedButton(
      onClick = { onConfirmMoveClicked() },
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      Text(text = stringResource(id = R.string.CHANGE_ADDRESS_ACCEPT_OFFER))
    }

    Spacer(modifier = Modifier.padding(bottom = 14.dp))

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

    Spacer(modifier = Modifier.padding(top = 32.dp))

  }
}

@Preview
@Composable
fun PreviewOfferContent() {
  HedvigTheme {
    Surface {
      OfferContent(
        scrollState = rememberScrollState(),
        quotes = listOf(
          MoveQuote(
            moveIntentId = MoveIntentId("1"),
            address = Address(
              id = AddressId("Address 1"),
              apartmentNumber = "12",
              bbrId = null,
              city = null,
              floor = null,
              postalCode = "1224",
              street = "Froedingsvaegen",
            ),
            numberCoInsured = 2,
            premium = UiMoney(345.0, CurrencyCode.SEK),
            startDate = LocalDate(2023, 5, 13),
            termsVersion = "",
          ),
          MoveQuote(
            moveIntentId = MoveIntentId("2"),
            address = Address(
              id = AddressId("Address 2"),
              apartmentNumber = "14",
              bbrId = null,
              city = null,
              floor = null,
              postalCode = "1224",
              street = "Froedingsvaegen",
            ),
            numberCoInsured = 2,
            premium = UiMoney(445.0, CurrencyCode.SEK),
            startDate = LocalDate(2023, 5, 13),
            termsVersion = "",
          ),
        ),
        onQuoteClicked = {},
        onConfirmMoveClicked = {},
        openChat = {},
      )
    }
  }
}
