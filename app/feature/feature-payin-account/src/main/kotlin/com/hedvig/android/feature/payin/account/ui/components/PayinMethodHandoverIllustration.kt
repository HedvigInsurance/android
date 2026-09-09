package com.hedvig.android.feature.payin.account.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import octopus.type.MemberPaymentProvider

@Composable
internal fun PayinMethodHandoverIllustration(provider: MemberPaymentProvider?, modifier: Modifier = Modifier) {
  Column(modifier) {
    Spacer(Modifier.height(48.dp))
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
          when (provider) {
            MemberPaymentProvider.TRUSTLY -> {
              Icon(HedvigIcons.Trustly, EmptyContentDescription, Modifier.size(39.dp))
            }

            MemberPaymentProvider.SWISH -> {
              Image(
                imageVector = HedvigIcons.Swish,
                contentDescription = EmptyContentDescription,
                modifier = Modifier
                  .size((39.0).dp),
              )
            }

            MemberPaymentProvider.INVOICE -> {
              Image(
                imageVector = HedvigIcons.Kivra,
                contentDescription = EmptyContentDescription,
                modifier = Modifier
                  .size((39.0).dp),
              )
            }

            null,
            MemberPaymentProvider.NORDEA,
            MemberPaymentProvider.UNKNOWN__,
            -> {
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
    Spacer(Modifier.height(48.dp))
  }
}

@Composable
@HedvigPreview
private fun PreviewSelectPrimaryPayinMethodScreen(
  @PreviewParameter(SelectedMethodIndexProvider::class) provider: MemberPaymentProvider?,
) {
  HedvigTheme {
    Surface(color = colorScheme.backgroundPrimary) {
      PayinMethodHandoverIllustration(
        provider,
        Modifier.padding(16.dp),
      )
    }
  }
}

private class SelectedMethodIndexProvider :
  CollectionPreviewParameterProvider<MemberPaymentProvider?>(
    listOf(
      null,
      MemberPaymentProvider.TRUSTLY,
      MemberPaymentProvider.SWISH,
      MemberPaymentProvider.INVOICE,
    ),
  )
