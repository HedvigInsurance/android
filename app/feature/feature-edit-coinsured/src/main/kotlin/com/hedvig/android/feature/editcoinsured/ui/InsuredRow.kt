package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Small
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Amber
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Red
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Lock
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun InsuredRow(
  displayName: String,
  identifier: String,
  hasMissingInfo: Boolean,
  allowEdit: Boolean,
  isMember: Boolean,
  activatesOn: LocalDate?,
  terminatesOn: LocalDate?,
  onRemove: () -> Unit,
  onEdit: () -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 16.dp),
      ) {
        Column {
          HedvigText(
            text = displayName,
            color = if (isMember) {
              HedvigTheme.colorScheme.textSecondary
            } else {
              Color.Unspecified
            },
          )

          HedvigText(
            text = identifier,
            color = HedvigTheme.colorScheme.textSecondary,
          )
          val dateTimeFormatter = rememberHedvigDateTimeFormatter()
          if (activatesOn != null) {
            val dateTimeFormatter = rememberHedvigDateTimeFormatter()
            Spacer(Modifier.height(4.dp))
            HighlightLabel(
              labelText = stringResource(
                id = R.string.CONTRACT_ADD_COINSURED_ACTIVE_FROM,
                dateTimeFormatter.format(activatesOn.toJavaLocalDate()),
              ),
              size = Small,
              color = Amber(MEDIUM),
            )
          }
          if (terminatesOn != null) {
            Spacer(Modifier.height(4.dp))
            HighlightLabel(
              labelText = stringResource(
                id = R.string.CONTRACT_ADD_COINSURED_ACTIVE_UNTIL,
                dateTimeFormatter.format(terminatesOn.toJavaLocalDate()),
              ),
              size = Small,
              color = Red(LIGHT),
            )
          }
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
              imageVector = HedvigIcons.Lock,
              contentDescription = "Locked",
              tint = HedvigTheme.colorScheme.fillSecondary,
              modifier = Modifier.size(16.dp),
            )
          }

          allowEdit && hasMissingInfo -> {
            HedvigText(stringResource(id = R.string.CONTRACT_EDIT_INFO))
          }

          !allowEdit -> Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = stringResource(R.string.GENERAL_REMOVE),
            modifier = Modifier.size(16.dp),
          )

          else -> {}
        }
      }
    },
    spaceBetween = 8.dp,
    modifier = modifier
      .clickable(enabled = !isMember) {
        if (allowEdit && hasMissingInfo) {
          onEdit()
        } else {
          onRemove()
        }
      }
      .padding(contentPadding),
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
        contentPadding = PaddingValues(horizontal = 0.dp),
        activatesOn = null,
        terminatesOn = LocalDate(2025, 12, 1),
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
        contentPadding = PaddingValues(horizontal = 0.dp),
        activatesOn = null,
        terminatesOn = null,
      )
    }
  }
}

@Composable
@HedvigPreview
private fun InsuredRowPreviewMember() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InsuredRow(
        displayName = "Test testersson",
        identifier = "182312041933",
        hasMissingInfo = false,
        isMember = true,
        onRemove = {},
        onEdit = {},
        allowEdit = false,
        contentPadding = PaddingValues(horizontal = 0.dp),
        activatesOn = LocalDate(2025, 10, 1),
        terminatesOn = LocalDate(2026, 10, 1),
      )
    }
  }
}
