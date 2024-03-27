package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronDown
import com.hedvig.android.core.icons.hedvig.small.hedvig.Lock
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken

@Composable
internal fun TerminationInfoCardInsurance(displayName: String, exposureName: String, modifier: Modifier = Modifier) {
  HedvigCard(
    onClick = null,
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .heightIn(72.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = displayName,
          style = MaterialTheme.typography.bodyLarge,
        )
        Text(
          text = exposureName,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
internal fun TerminationInfoCardDate(
  dateValue: String?,
  onClick: () -> Unit,
  isLocked: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigCard(
      onClick = onClick,
      colors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Column {
            Text(
              text = "Termination date", // todo: real copy here
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = dateValue ?: "Select date...", // todo: real copy here
              style = MaterialTheme.typography.bodyLarge,
              color = if (dateValue == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
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
                imageVector = Icons.Hedvig.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
            } else {
              Icon(
                imageVector = Icons.Hedvig.ChevronDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationInfoCardDate() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationInfoCardDate("Today", {}, true)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationInfoCardInsurance() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationInfoCardInsurance("HomeownerInsurance", "Bellmansgatan 19")
    }
  }
}
