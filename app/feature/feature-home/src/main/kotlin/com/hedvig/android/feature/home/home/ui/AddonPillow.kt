package com.hedvig.android.feature.home.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.addons.data.FlowType
import com.hedvig.android.data.contract.PillowType
import com.hedvig.android.data.contract.pillowResource
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Plus
import org.jetbrains.compose.resources.painterResource

/**
 * The pillow shown for a home add-on banner, overlaid with the plus badge from the design. Add-ons
 * carry no imagery from the backend, so each add-on flow reuses the pillow of the insurance it
 * relates to.
 */
@Composable
internal fun AddonPillow(flowType: FlowType, modifier: Modifier = Modifier) {
  Box(modifier.size(48.dp)) {
    Image(
      painter = painterResource(flowType.pillowType().pillowResource()),
      contentDescription = null,
      modifier = Modifier.size(48.dp),
    )
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .hedvigDropShadow(CircleShape)
        .size(17.dp)
        .background(HedvigTheme.colorScheme.fillNegative, CircleShape)
        .border(1.dp, HedvigTheme.colorScheme.borderPrimary, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = HedvigIcons.Plus,
        contentDescription = null,
        tint = HedvigTheme.colorScheme.fillPrimary,
        modifier = Modifier.size(10.dp),
      )
    }
  }
}

private fun FlowType.pillowType(): PillowType = when (this) {
  FlowType.APP_CAR_PLUS -> PillowType.CAR

  FlowType.APP_TRAVEL_PLUS_SELL_ONLY,
  FlowType.APP_TRAVEL_PLUS_SELL_OR_UPGRADE,
  -> PillowType.VACATION
}
