package com.hedvig.android.feature.payoutaccount.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.PaymentMethodPillow
import com.hedvig.android.design.system.hedvig.PaymentMethodPillowMarkSize
import com.hedvig.android.design.system.hedvig.PaymentMethodPlusMark
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Card
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Lock
import com.hedvig.android.design.system.hedvig.icon.Trustly
import com.hedvig.android.design.system.hedvig.icon.colored.Swish

/** How far the brand mark fades on a locked row, where it reads as a label rather than an action. */
private const val LockedMarkAlpha = 0.5f

/**
 * The connected payout method as the overview shows it: a lock instead of a chevron, because the
 * method is changed from the flow behind "change payout method" rather than by tapping the row.
 */
@Composable
internal fun LockedPayoutMethodRow(
  provider: PaymentProvider,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .heightIn(min = 64.dp)
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      LockedPayoutProviderPillow(provider)
      Spacer(Modifier.width(10.dp))
      Column(Modifier.padding(vertical = 8.dp)) {
        HedvigText(
          text = title,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        )
        HedvigText(
          text = subtitle,
          color = HedvigTheme.colorScheme.textDisabledTranslucent,
        )
      }
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.width(4.dp))
      Icon(
        HedvigIcons.Lock,
        EmptyContentDescription,
        tint = HedvigTheme.colorScheme.fillDisabledTransparent,
        // todo: get icon from design!
        modifier = Modifier.size(28.dp),
      )
    }
  }
}

/**
 * The provider's brand mark on the tile it is drawn on: white behind the full-colour marks, black
 * behind the monochrome ones, which pick up the tile's content colour.
 */
@Composable
internal fun PayoutProviderPillow(provider: PaymentProvider?, modifier: Modifier = Modifier) {
  val onDarkTile = provider.onDarkTile
  PaymentMethodPillow(
    modifier = modifier,
    containerColor = if (onDarkTile) HedvigTheme.colorScheme.fillBlack else HedvigTheme.colorScheme.fillWhite,
    contentColor = if (onDarkTile) HedvigTheme.colorScheme.fillWhite else HedvigTheme.colorScheme.fillBlack,
    mark = { PayoutProviderMark(provider, Modifier.size(PaymentMethodPillowMarkSize)) },
  )
}

@Composable
private fun LockedPayoutProviderPillow(provider: PaymentProvider, modifier: Modifier = Modifier) {
  val onDarkTile = provider.onDarkTile
  PaymentMethodPillow(
    modifier = modifier,
    containerColor = if (onDarkTile) HedvigTheme.colorScheme.fillDisabled else HedvigTheme.colorScheme.fillWhite,
    contentColor = if (onDarkTile) HedvigTheme.colorScheme.fillWhite else HedvigTheme.colorScheme.fillBlack,
    mark = {
      PayoutProviderMark(
        provider,
        Modifier
          .size(PaymentMethodPillowMarkSize)
          .alpha(LockedMarkAlpha),
      )
    },
  )
}

/**
 * The provider's brand mark. The monochrome ones pick up the surrounding content colour; the
 * full-colour ones ignore it. A provider with no mark of its own falls back to the plus, so one the
 * backend adds later still renders.
 */
@Composable
internal fun PayoutProviderMark(provider: PaymentProvider?, modifier: Modifier = Modifier) {
  when (provider) {
    PaymentProvider.Trustly -> Icon(HedvigIcons.Trustly, EmptyContentDescription, modifier)

    // todo: get icon from design!
    PaymentProvider.Nordea -> Icon(HedvigIcons.Card, EmptyContentDescription, modifier)

    PaymentProvider.Swish -> Image(HedvigIcons.Swish, EmptyContentDescription, modifier)

    else -> PaymentMethodPlusMark(modifier)
  }
}

/** Trustly's mark is monochrome, so it reads better inverted on a dark tile than on the white one. */
private val PaymentProvider?.onDarkTile: Boolean
  get() = this == PaymentProvider.Trustly

@Composable
@HedvigPreview
private fun PreviewLockedPayoutMethodRow(
  @PreviewParameter(PayoutProviderPreviewProvider::class) provider: PaymentProvider,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(
        Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        LockedPayoutMethodRow(
          provider = provider,
          title = "Payout method",
          subtitle = "123456789",
        )
        PayoutProviderPillow(provider)
      }
    }
  }
}

private class PayoutProviderPreviewProvider :
  CollectionPreviewParameterProvider<PaymentProvider>(
    listOf(
      PaymentProvider.Swish,
      PaymentProvider.Trustly,
      PaymentProvider.Nordea,
    ),
  )
