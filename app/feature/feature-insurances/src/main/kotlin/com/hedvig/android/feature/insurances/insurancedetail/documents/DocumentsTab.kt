package com.hedvig.android.feature.insurances.insurancedetail.documents

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.stringWithShiftedLabel
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

@Composable
internal fun DocumentsTab(
  documents: List<InsuranceVariantDocument>,
  onDocumentClicked: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
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
            HedvigText(
              text = stringWithShiftedLabel(
                text = title,
                labelText = "PDF",
                labelFontSize = HedvigTheme.typography.bodySmall.fontSize,
                textColor = LocalContentColor.current,
                textFontSize = LocalTextStyle.current.fontSize,
              ),
            )
            if (!subtitle.isNullOrBlank()) {
              HedvigText(
                text = subtitle,
                color = HedvigTheme.colorScheme.textSecondary,
              )
            }
          }
        },
        endSlot = {
          Icon(
            imageVector = HedvigIcons.ArrowNorthEast,
            contentDescription = null,
            modifier = Modifier
              .wrapContentSize(Alignment.CenterEnd)
              .size(24.dp),
          )
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
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
