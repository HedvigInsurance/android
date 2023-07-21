package com.hedvig.app.feature.profile.ui.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronRight
import com.hedvig.android.core.icons.hedvig.normal.Payments
import com.hedvig.android.core.icons.hedvig.normal.Waiting
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import hedvig.resources.R
import java.util.*
import kotlinx.datetime.LocalDate

@Composable
fun PaymentDestination(navigateUp: () -> Unit) {

}

@Composable
fun PaymentScreen(
  uiState: PaymentUiState,
  locale: Locale,
  navigateUp: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    HedvigScaffold(
      topAppBarText = stringResource(R.string.PROFILE_PAYMENT_TITLE),
      navigateUp = navigateUp,
      modifier = Modifier.clearFocusOnTap(),
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))
        NextPayment(uiState, locale)
        Spacer(Modifier.height(4.dp))
        Divider()
        Spacer(Modifier.height(4.dp))
        InsuranceCosts(uiState, locale)
        Spacer(Modifier.height(4.dp))
        TotalDiscount(uiState)
        Spacer(Modifier.height(4.dp))
        Divider()
        Spacer(Modifier.height(16.dp))
        AddDiscount(uiState)
        Spacer(Modifier.height(32.dp))
        PaymentDetails(uiState)
        Spacer(Modifier.height(4.dp))
        Divider()
        PaymentHistory(onClick = {})
        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          text = "Change bank account",
          onClick = {},
        )
      }
    }
  }
}

@Composable
private fun NextPayment(uiState: PaymentUiState, locale: Locale) {
  HedvigCard(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = uiState.nextChargeAmount.format(locale),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.displayMedium,
      modifier = Modifier.padding(vertical = 6.dp),
    )
  }
  Spacer(Modifier.height(12.dp))
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text("Next payment") // TODO
    Text(
      text = uiState.nextChargeDate.toString(),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun InsuranceCosts(
  uiState: PaymentUiState,
  locale: Locale,
) {
  uiState.insuranceCosts.forEach { insuranceCost ->
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Image(
          painter = painterResource(id = com.hedvig.android.core.ui.R.drawable.ic_pillow),
          contentDescription = null,
          modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(insuranceCost.displayName)
      }
      Text(
        text = insuranceCost.cost.format(locale),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Divider()
  }
}

@Composable
private fun TotalDiscount(uiState: PaymentUiState) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text("Discounts")
    Text(
      text = uiState.totalDiscount,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun AddDiscount(uiState: PaymentUiState) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text("Add code")
    Switch(
      checked = true,
      onCheckedChange = {},
      colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.typeElement),
    )
  }
  Spacer(Modifier.height(12.dp))
  uiState.activeDiscounts.forEach {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(it.code)
      Text(
        text = it.displayName,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Spacer(Modifier.height(4.dp))
  }
  Spacer(Modifier.height(12.dp))
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    HedvigTextField(
      value = uiState.discountCode ?: "",
      onValueChange = {},
      errorText = uiState.discountError,
      label = {
        Text(stringResource(id = R.string.REFERRAL_ADDCOUPON_INPUTPLACEHOLDER))
      },
      modifier = Modifier.weight(1f),
    )
    Spacer(Modifier.width(8.dp))
    HedvigContainedSmallButton(
      text = "Add", // TODO
      onClick = {},
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.typeContainer,
        contentColor = MaterialTheme.colorScheme.primary,
      ),
      modifier = Modifier.widthIn(min = 127.dp),
    )
  }
}

@Composable
fun PaymentDetails(uiState: PaymentUiState) {
  Text(stringResource(id = R.string.payment_details_navigation_bar_title))
  Spacer(Modifier.height(16.dp))
  Divider()
  Spacer(Modifier.height(4.dp))
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Image(
        imageVector = Icons.Hedvig.Payments,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
      )
      Spacer(Modifier.width(16.dp))
      Text(uiState.paymentMethod.displayName)
    }
    Text(
      text = uiState.paymentMethod.displayValue,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
fun PaymentHistory(onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp)
      .clickable { onClick() },
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Hedvig.Waiting,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
      )
      Spacer(Modifier.width(16.dp))
      Text("Payment history")
    }
    Icon(
      imageVector = Icons.Hedvig.ChevronRight,
      contentDescription = null,
      modifier = Modifier.size(16.dp),
    )
  }
}

@Composable
@HedvigPreview
fun PreviewPaymentScreen() {
  HedvigTheme(useNewColorScheme = true) {
    Surface {
      PaymentScreen(
        uiState = PaymentUiState(
          nextChargeAmount = "300 kr",
          nextChargeDate = LocalDate(2023, 4, 23),
          insuranceCosts = listOf(
            PaymentUiState.InsuranceCost(
              displayName = "Home Insurance",
              cost = "279kr/mån",
            ),
            PaymentUiState.InsuranceCost(
              displayName = "Accident Insurance",
              cost = "359kr/mån",
            ),
          ),
          totalDiscount = "-40kr/mån",
          activeDiscounts = listOf(
            PaymentUiState.Discount(code = "FREE", displayName = "Gratis i 6 mån"),
            PaymentUiState.Discount(code = "BANK", displayName = "-50kr/mån"),
          ),
          paymentMethod = PaymentUiState.PaymentMethod(
            displayName = "Nordea",
            displayValue = "31489*****",
          ),
        ),
        locale = Locale.ENGLISH,
        navigateUp = {},
      )
    }
  }
}

