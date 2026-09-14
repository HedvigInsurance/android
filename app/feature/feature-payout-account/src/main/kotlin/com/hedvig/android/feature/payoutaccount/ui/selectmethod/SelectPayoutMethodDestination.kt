package com.hedvig.android.feature.payoutaccount.ui.selectmethod

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Card
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Lock
import com.hedvig.android.design.system.hedvig.icon.Trustly
import com.hedvig.android.design.system.hedvig.icon.colored.Kivra
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import hedvig.resources.BANK_PAYOUT_METHOD_CARD_DESCRIPTION
import hedvig.resources.BANK_PAYOUT_METHOD_CARD_TITLE
import hedvig.resources.PAYOUT_METHOD_SWISH_DESCRIPTION
import hedvig.resources.PAYOUT_METHOD_TRUSTLY_DESCRIPTION
import hedvig.resources.PAYOUT_SELECT_PAYOUT_METHOD
import hedvig.resources.Res
import hedvig.resources.Res.string
import hedvig.resources.swish
import hedvig.resources.trustly
import octopus.type.MemberPaymentProvider
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectPayoutMethodDestination(
  availableProviders: List<MemberPaymentProvider>,
  onTrustlySelected: () -> Unit,
  onNordeaSelected: () -> Unit,
  onSwishSelected: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(string.PAYOUT_SELECT_PAYOUT_METHOD),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      for (provider in availableProviders) {
        when (provider) {
          MemberPaymentProvider.TRUSTLY -> {
            PayoutMethodRow(
              title = stringResource(string.trustly),
              subtitle = stringResource(string.PAYOUT_METHOD_TRUSTLY_DESCRIPTION),
              onClick = onTrustlySelected,
              provider = provider,
            )
          }

          MemberPaymentProvider.NORDEA -> {
            PayoutMethodRow(
              title = stringResource(string.BANK_PAYOUT_METHOD_CARD_TITLE),
              subtitle = stringResource(string.BANK_PAYOUT_METHOD_CARD_DESCRIPTION),
              onClick = onNordeaSelected,
              provider = provider,
            )
          }

          MemberPaymentProvider.SWISH -> {
            PayoutMethodRow(
              title = stringResource(string.swish),
              subtitle = stringResource(string.PAYOUT_METHOD_SWISH_DESCRIPTION),
              onClick = onSwishSelected,
              provider = provider,
            )
          }

          else -> {}
        }
      }
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
internal fun PayoutMethodRow(
  provider: MemberPaymentProvider,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isLocked: Boolean = false,
) {
  HedvigCard(
    onClick = if (isLocked) null else onClick,
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .heightIn(min = 64.dp)
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      PayoutMethodPillow(provider)
      Spacer(Modifier.width(10.dp))
      Column(Modifier.padding(vertical = 8.dp)) {
        HedvigText(text = title)
        HedvigText(
          text = subtitle,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.width(4.dp))
      if (isLocked) {
        Icon(
          HedvigIcons.Lock,
          null,
          modifier = Modifier.size(28.dp), //todo: get icon from design!
        )
      }
    }
  }
}

@Composable
private fun PayoutMethodPillow(provider: MemberPaymentProvider) {
  val onDarkTile = provider == MemberPaymentProvider.TRUSTLY
  Surface(
    shape = HedvigTheme.shapes.cornerSmall,
    color = if (onDarkTile) HedvigTheme.colorScheme.fillBlack else HedvigTheme.colorScheme.fillWhite,
    contentColor = if (onDarkTile) HedvigTheme.colorScheme.fillWhite else HedvigTheme.colorScheme.fillBlack,
    modifier = Modifier.size(40.dp),
  ) {
    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
      when (provider) {
        MemberPaymentProvider.TRUSTLY -> Icon(HedvigIcons.Trustly, null, Modifier.size(28.dp))
        MemberPaymentProvider.SWISH -> Image(HedvigIcons.Swish, null, Modifier.size(28.dp))
        MemberPaymentProvider.INVOICE -> Image(HedvigIcons.Kivra, null, Modifier.size(28.dp))
        MemberPaymentProvider.NORDEA -> Icon(
          HedvigIcons.Card,
          null,
          modifier = Modifier.size(28.dp), //todo: get icon from design!
        )
        MemberPaymentProvider.UNKNOWN__ -> {}
      }
    }
  }
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewSelectPayoutMethodScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SelectPayoutMethodDestination(
        availableProviders = listOf(
          MemberPaymentProvider.SWISH,
          MemberPaymentProvider.TRUSTLY,
          MemberPaymentProvider.NORDEA,
        ),
        onTrustlySelected = {},
        onNordeaSelected = {},
        onSwishSelected = {},
        navigateUp = {},
      )
    }
  }
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewPayoutMethodRow(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) isLocked: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary,
      modifier =Modifier.padding(16.dp)) {
      PayoutMethodRow(
        provider =  MemberPaymentProvider.SWISH,
        title = stringResource(string.swish),
        subtitle = "123456789",
        onClick = {},
        isLocked = isLocked
      )
    }
  }
}
