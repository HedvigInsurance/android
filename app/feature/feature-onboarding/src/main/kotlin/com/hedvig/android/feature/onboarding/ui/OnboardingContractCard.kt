package com.hedvig.android.feature.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.contract.pillowResource
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import hedvig.resources.ONBOARDING_ADDED_LABEL
import hedvig.resources.ONBOARDING_ADD_BUTTON
import hedvig.resources.Res
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingContractCard(
  displayName: String,
  exposureName: String,
  typeOfContract: String,
  isComplete: Boolean,
  onAddClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    shape = HedvigTheme.shapes.cornerLarge,
    color = HedvigTheme.colorScheme.surfacePrimary,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
      Image(
        painter = painterResource(typeOfContract.toContractGroup().pillowResource()),
        contentDescription = null,
        modifier = Modifier.size(40.dp),
      )
      Spacer(Modifier.width(12.dp))
      Column(Modifier.weight(1f)) {
        HedvigText(
          text = displayName,
          style = HedvigTheme.typography.bodySmall,
        )
        HedvigText(
          text = exposureName,
          style = HedvigTheme.typography.bodySmall,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      Spacer(Modifier.width(12.dp))
      if (isComplete) {
        HedvigText(
          text = stringResource(Res.string.ONBOARDING_ADDED_LABEL),
          style = HedvigTheme.typography.bodySmall,
          color = HedvigTheme.colorScheme.textSecondary,
        )
        Spacer(Modifier.width(8.dp))
        Icon(imageVector = HedvigIcons.Checkmark, contentDescription = null)
      } else {
        HedvigButton(
          text = stringResource(Res.string.ONBOARDING_ADD_BUTTON),
          onClick = onAddClick,
          enabled = true,
          buttonStyle = ButtonDefaults.ButtonStyle.Primary,
          buttonSize = ButtonDefaults.ButtonSize.Small,
          modifier = Modifier.clip(CircleShape),
        )
      }
    }
  }
}
