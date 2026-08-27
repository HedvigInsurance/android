package com.hedvig.android.feature.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.data.contract.pillowResource
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import hedvig.resources.ONBOARDING_ADD_BUTTON
import hedvig.resources.Res
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingContractCard(
  displayName: String,
  secondaryText: String,
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
        )
        HedvigText(
          text = secondaryText,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        )
      }
      Spacer(Modifier.width(12.dp))
      // The checkmark is only animated in when the row becomes complete while it is on screen. A row that is
      // already complete on its first composition renders it without motion.
      // Aligned to the end so that the width change between the button and the checkmark is absorbed on the
      // trailing edge, leaving the text column beside it untouched.
      AnimatedContent(
        targetState = isComplete,
        contentAlignment = Alignment.Center,
        label = "contract card completion",
      ) { complete ->
        if (complete) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(24.dp)
              .clip(HedvigTheme.shapes.cornerXSmall)
              .background(HedvigTheme.colorScheme.signalGreenElement),
          ) {
            Icon(
              imageVector = HedvigIcons.Checkmark,
              contentDescription = null,
              tint = HedvigTheme.colorScheme.fillWhite,
              modifier = Modifier.size(16.dp),
            )
          }
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
}

@HedvigPreview
@Composable
private fun PreviewOnboardingContractCard(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) isComplete: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingContractCard(
        displayName = "displayName",
        secondaryText = "secondaryText",
        typeOfContract = "SE_HOUSE",
        isComplete = isComplete,
        onAddClick = {},
        modifier = Modifier,
      )
    }
  }
}
