package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.X
import com.hedvig.android.core.icons.hedvig.small.hedvig.Lock
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import hedvig.resources.R

@Composable
internal fun InsuredRow(
  displayName: String,
  identifier: String,
  hasMissingInfo: Boolean,
  allowEdit: Boolean,
  isMember: Boolean,
  onRemove: () -> Unit,
  onEdit: () -> Unit,
) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 16.dp),
      ) {
        Column {
          Text(
            text = displayName,
            color = if (isMember) {
              MaterialTheme.colorScheme.onSurfaceVariant
            } else {
              Color.Unspecified
            },
          )

          Text(
            text = identifier,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    endSlot = {
      Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 16.dp),
      ) {
        when {
          isMember -> {
            Icon(
              imageVector = Icons.Hedvig.Lock,
              contentDescription = "Locked",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp),
            )
          }

          allowEdit && hasMissingInfo -> {
            Text(stringResource(id = R.string.CONTRACT_EDIT_INFO))
          }

          !allowEdit -> Icon(
            imageVector = Icons.Hedvig.X,
            contentDescription = "Remove",
            modifier = Modifier.size(16.dp),
          )

          else -> {}
        }
      }
    },
    spaceBetween = 8.dp,
    modifier = Modifier
      .clickable(enabled = !isMember) {
        if (allowEdit && hasMissingInfo) {
          onEdit()
        } else {
          onRemove()
        }
      }
      .padding(horizontal = 16.dp),
  )
}

@Composable
@HedvigPreview
private fun InsuredRowPreviewEditable() {
  HedvigTheme {
    Surface {
      InsuredRow(
        displayName = "Test testersson",
        identifier = "182312041933",
        hasMissingInfo = false,
        isMember = false,
        onRemove = {},
        onEdit = {},
        allowEdit = true,
      )
    }
  }
}

@Composable
@HedvigPreview
private fun InsuredRowPreviewMissingInfo() {
  HedvigTheme {
    Surface {
      InsuredRow(
        displayName = "Test testersson",
        identifier = "182312041933",
        hasMissingInfo = true,
        isMember = false,
        onRemove = {},
        onEdit = {},
        allowEdit = false,
      )
    }
  }
}

@Composable
@HedvigPreview
private fun InsuredRowPreviewMember() {
  HedvigTheme {
    Surface {
      InsuredRow(
        displayName = "Test testersson",
        identifier = "182312041933",
        hasMissingInfo = false,
        isMember = true,
        onRemove = {},
        onEdit = {},
        allowEdit = false,
      )
    }
  }
}
