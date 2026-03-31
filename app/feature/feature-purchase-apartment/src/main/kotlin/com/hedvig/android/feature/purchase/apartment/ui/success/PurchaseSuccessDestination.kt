package com.hedvig.android.feature.purchase.apartment.ui.success

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.TopAppBarActionType

@Composable
internal fun PurchaseSuccessDestination(startDate: String?, close: () -> Unit) {
  HedvigScaffold(
    navigateUp = close,
    topAppBarActionType = TopAppBarActionType.CLOSE,
    itemsColumnHorizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(Modifier.weight(1f))
    HedvigText(
      text = "Din försäkring är klar!",
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    if (startDate != null) {
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = "Startdatum: $startDate",
        style = HedvigTheme.typography.bodySmall,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.weight(1f))
    HedvigButton(
      text = "Stäng",
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      buttonStyle = Primary,
      buttonSize = Large,
      enabled = true,
      onClick = close,
    )
    Spacer(Modifier.height(16.dp))
  }
}
