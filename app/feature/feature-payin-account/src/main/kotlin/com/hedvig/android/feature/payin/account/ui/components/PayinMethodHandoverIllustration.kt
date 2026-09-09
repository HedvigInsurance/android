package com.hedvig.android.feature.payin.account.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ThreeDotsLoading
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadOutline
import com.hedvig.android.design.system.hedvig.icon.Plus
import com.hedvig.android.design.system.hedvig.icon.Trustly
import com.hedvig.android.design.system.hedvig.icon.colored.Kivra
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import com.hedvig.android.feature.payin.account.data.InvoiceDelivery
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.PayinAccount.Invoice
import com.hedvig.android.feature.payin.account.data.PayinAccount.SwishPayin
import com.hedvig.android.feature.payin.account.data.PayinAccount.Trustly

@Composable
internal fun PayinMethodHandoverIllustration(method: PayinAccount?, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Surface(
      shape = HedvigTheme.shapes.cornerXXLarge,
      color = colorScheme.backgroundPrimary,
      contentColor = colorScheme.fillPrimary,
      border = colorScheme.borderSecondary,
      modifier = Modifier.size(74.dp)
        .hedvigDropShadow(HedvigTheme.shapes.cornerXXLarge),
    ) {
      Box(Modifier.size(74.dp), contentAlignment = Alignment.Center) {
        when (method) {
          is Trustly -> {
            Icon(HedvigIcons.Trustly, EmptyContentDescription, Modifier.size(39.dp))
          }

          is SwishPayin -> {
            Image(
              imageVector = HedvigIcons.Swish,
              contentDescription = EmptyContentDescription,
              modifier = Modifier
                .size((39.0).dp),
            )
          }

          is Invoice -> {
            Image(
              imageVector = HedvigIcons.Kivra,
              contentDescription = EmptyContentDescription,
              modifier = Modifier
                .size((39.0).dp),
            )
          }

          null -> {
            Icon(
              HedvigIcons.Plus,
              EmptyContentDescription,
              Modifier.size(39.dp),
              tint = HedvigTheme.colorScheme.fillSecondary,
            )
          }
        }
      }
    }
    ThreeDotsLoading()
    Surface(
      shape = HedvigTheme.shapes.cornerXXLarge,
      color = colorScheme.fillBlack,
      contentColor = colorScheme.fillWhite,
      border = colorScheme.borderPrimary,
      modifier = Modifier.size(74.dp),
    ) {
      Box(Modifier.size(74.dp), contentAlignment = Alignment.Center) {
        Icon(
          HedvigIcons.HelipadOutline,
          EmptyContentDescription,
          Modifier.size(65.dp),
        )
      }
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewSelectPrimaryPayinMethodScreen(
  @PreviewParameter(SelectedMethodIndexProvider::class) method: PayinAccount?,
) {
  HedvigTheme {
    Surface(color = colorScheme.backgroundPrimary) {
      PayinMethodHandoverIllustration(method,
        Modifier.padding(16.dp))
    }
  }
}

private class SelectedMethodIndexProvider :
  CollectionPreviewParameterProvider<PayinAccount?>(
    listOf(
      null,
      Trustly(
        "8327",
        "91234124",
        "Swedbank",
        isPending = false,
        isDefault = false,
      ),
      SwishPayin(
        "0709901232",
        isPending = false,
        isDefault = true,
      ),
      Invoice(
        delivery = InvoiceDelivery.Kivra,
        email = null,
        isPending = false,
        isDefault = false,
      ),
    ),
  )
