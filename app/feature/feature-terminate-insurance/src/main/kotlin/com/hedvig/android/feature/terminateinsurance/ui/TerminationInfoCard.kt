package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ChevronDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Lock
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun TerminationInfoCardInsurance(displayName: String, exposureName: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = HedvigTheme.shapes.cornerLarge,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        HedvigText(
          text = displayName,
          style = HedvigTheme.typography.bodySmall,
        )
        HedvigText(
          text = exposureName,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }
  }
}

@Composable
internal fun TerminationInfoCardDate(
  dateValue: String?,
  onClick: (() -> Unit)?,
  isLocked: Boolean,
  modifier: Modifier = Modifier,
) {
  val modifierWithClick = if (onClick != null) {
    modifier
      .clip(HedvigTheme.shapes.cornerLarge)
      .clickable(
        onClick = onClick,
      )
  } else {
    modifier
  }

  Surface(
    modifier = modifierWithClick,
    shape = HedvigTheme.shapes.cornerLarge,
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      spaceBetween = 8.dp,
      startSlot = {
        Column {
          HedvigText(
            text = stringResource(id = R.string.TERMINATION_FLOW_DATE_FIELD_TEXT),
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondary,
          )
          HedvigText(
            text = dateValue ?: stringResource(R.string.TERMINATION_FLOW_DATE_FIELD_PLACEHOLDER),
            style = HedvigTheme.typography.bodySmall,
            color = if (dateValue == null) {
              HedvigTheme.colorScheme.textSecondary
            } else {
              Color.Unspecified
            },
          )
        }
      },
      endSlot = {
        Row(
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (isLocked) {
            Icon(
              imageVector = HedvigIcons.Lock,
              contentDescription = null,
              tint = HedvigTheme.colorScheme.fillPrimary,
            )
          } else {
            Icon(
              imageVector = HedvigIcons.ChevronDown,
              modifier = Modifier.size(16.dp),
              contentDescription = null,
              tint = HedvigTheme.colorScheme.fillPrimary,
            )
          }
        }
      },
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationInfoCardDate() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationInfoCardDate(null, {}, false)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationInfoCardDateNotLocked() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationInfoCardDate(LocalDate(2023, 9, 8).toString(), {}, true)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationInfoCardInsurance() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationInfoCardInsurance("HomeownerInsurance", "Bellmansgatan 19")
    }
  }
}
