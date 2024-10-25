package com.hedvig.android.feature.insurances.insurancedetail.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.stringWithShiftedLabel
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.ArrowNorthEast
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken

@Composable
internal fun DocumentsTab(
  documents: List<InsuranceVariantDocument>,
  onDocumentClicked: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Spacer(Modifier.height(16.dp))
    for ((index, document) in documents.withIndex()) {
      DocumentCard(
        onClick = { onDocumentClicked(document.url) },
        title = document.displayName,
        subtitle = null,
      )
      if (index != documents.lastIndex) {
        Spacer(Modifier.height(4.dp))
      }
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
private fun DocumentCard(onClick: () -> Unit, title: String, subtitle: String?) {
  HedvigCard(
    onClick = onClick,
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .heightIn(min = 72.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Column {
            Text(
              text = stringWithShiftedLabel(
                text = title,
                labelText = "PDF",
                labelFontSize = MaterialTheme.typography.bodySmall.fontSize,
                textColor = LocalContentColor.current,
                textFontSize = LocalTextStyle.current.fontSize,
              ),
            )
            if (!subtitle.isNullOrBlank()) {
              Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        },
        endSlot = {
          Row(horizontalArrangement = Arrangement.End) {
            Icon(
              imageVector = Icons.Hedvig.ArrowNorthEast,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          }
        },
        spaceBetween = 8.dp,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDocumentsScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DocumentsTab(
        documents = listOf(
          InsuranceVariantDocument("", "test", InsuranceVariantDocument.InsuranceDocumentType.GENERAL_TERMS),
          InsuranceVariantDocument("", "", InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO),
        ),
        onDocumentClicked = {},
      )
    }
  }
}
