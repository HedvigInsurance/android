package com.hedvig.android.feature.changeaddress.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.R
import com.hedvig.android.core.ui.UiMoney
import com.hedvig.android.feature.changeaddress.data.Address
import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

@Composable
fun QuoteCard(
  movingDate: String?,
  quote: MoveQuote,
  onExpandClicked: () -> Unit,
  isExpanded: Boolean,
) {
  HedvigCard(
    shape = SquircleShape,
    onClick = { onExpandClicked() },
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          painter = painterResource(id = R.drawable.ic_pillow),
          contentDescription = "",
          modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.padding(start = 16.dp))
        Column {
          Text(
            text = "Hemförsäkring Bostadsrätt",
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp,
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Aktiveras $movingDate",
              style = MaterialTheme.typography.titleMedium,
              color = Color(0xFF727272),
              fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.padding(start = 5.dp))
            Icon(
              painter = painterResource(id = com.hedvig.android.core.designsystem.R.drawable.ic_info),
              tint = Color(0xFF727272),
              contentDescription = "",
            )
          }
        }
      }

      Spacer(modifier = Modifier.padding(top = 16.dp))

      Divider(
        thickness = 1.dp,
        color = Color(0xFFCFCFCF),
      )

      Spacer(modifier = Modifier.padding(top = 8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 8.dp),
        ) {
          Text(
            text = "Detaljer",
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp,
          )
          Spacer(modifier = Modifier.padding(4.dp))

          val angle = animateFloatAsState(
            targetValue = if (isExpanded) { 0f } else { 180f },
            // animationSpec = spring(), // Might wanna play with the animation spec if the default spring feels odd
          )
          Icon(
            painter = painterResource(id = com.hedvig.android.core.designsystem.R.drawable.ic_drop_down_indicator),
            contentDescription = "",
            tint = Color(0xFFB4B4B4),
            modifier = Modifier.graphicsLayer {
              rotationZ = angle.value
            },
          )
        }

        Text(
          text = stringResource(
            id = hedvig.resources.R.string.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL,
            quote.premium.toString(),
          ),
          style = MaterialTheme.typography.titleMedium,
          fontSize = 18.sp,
        )
      }

      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
      ) {
        Column {
          Spacer(modifier = Modifier.padding(6.dp))
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Bostadstyp")
            Text("Bostadsrättt")
          }
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Address")
            Text(quote.address.street)
          }
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Postkod")
            Text(quote.address.postalCode)
          }
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Boarea")
            Text("63kvm")
          }
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Antal försäkrade")
            Text("${quote.numberCoInsured} personer")
          }
        }
      }
    }
  }
  Spacer(modifier = Modifier.padding(top = 16.dp))
}

@Preview
@Composable
fun PreviewQuoteCard() {
  HedvigTheme {
    QuoteCard(
      movingDate = "123",
      quote = MoveQuote(
        moveIntentId = MoveIntentId(""),
        address = Address(
          id = AddressId(""),
          apartmentNumber = "12",
          bbrId = null,
          city = null,
          floor = null,
          postalCode = "124",
          street = "Froedingsvaegen",
        ),
        numberCoInsured = 2,
        premium = UiMoney(345.0, CurrencyCode.SEK),
        startDate = LocalDate(2023, 5, 13),
        termsVersion = "",
      ),
      onExpandClicked = {},
      isExpanded = true,
    )
  }
}
