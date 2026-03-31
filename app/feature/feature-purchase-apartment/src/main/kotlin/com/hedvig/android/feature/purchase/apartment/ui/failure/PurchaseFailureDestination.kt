package com.hedvig.android.feature.purchase.apartment.ui.failure

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.TopAppBarActionType

@Composable
internal fun PurchaseFailureDestination(onRetry: () -> Unit, close: () -> Unit) {
  HedvigScaffold(
    navigateUp = close,
    topAppBarActionType = TopAppBarActionType.CLOSE,
  ) {
    HedvigErrorSection(
      onButtonClick = onRetry,
      modifier = Modifier.weight(1f),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewPurchaseFailure() {
  HedvigTheme {
    PurchaseFailureDestination(onRetry = {}, close = {})
  }
}
