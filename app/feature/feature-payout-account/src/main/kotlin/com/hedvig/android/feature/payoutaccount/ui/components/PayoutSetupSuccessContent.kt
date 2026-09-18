package com.hedvig.android.feature.payoutaccount.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LoadingState
import com.hedvig.android.design.system.hedvig.PaymentMethodTileBadge
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

/**
 * How every payout setup ends: the method has landed, so the handover illustration is badged with a
 * checkmark and the only thing left to do is leave the flow.
 */
@Composable
internal fun ColumnScope.PayoutSetupSuccessContent(provider: PaymentProvider, title: String, onContinue: () -> Unit) {
  Spacer(Modifier.height(8.dp))
  FlowHeading(
    title = title,
    description = null,
    baseStyle = HedvigTheme.typography.bodySmall,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.weight(1f))
  PayoutMethodHandoverIllustration(
    provider,
    modifier = Modifier.align(Alignment.CenterHorizontally),
    loadingState = LoadingState.ACTIVE,
    destinationBadge = {
      PaymentMethodTileBadge(
        icon = HedvigIcons.Checkmark,
        containerColor = HedvigTheme.colorScheme.signalGreenElement,
        contentColor = HedvigTheme.colorScheme.fillWhite,
      )
    },
  )
  Spacer(Modifier.weight(1f))
  HedvigButton(
    text = stringResource(Res.string.general_continue_button),
    onClick = onContinue,
    enabled = true,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
}
