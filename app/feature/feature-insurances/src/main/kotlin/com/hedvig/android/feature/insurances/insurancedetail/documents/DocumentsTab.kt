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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.insurances.data.Addon
import hedvig.resources.R

@Composable
internal fun DocumentsTab(
  documents: List<InsuranceVariantDocument>,
  addons: List<Addon>?,
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
    if (!addons.isNullOrEmpty()) {
      addons.forEach {
        HighlightLabel(
          modifier = Modifier.padding(horizontal = 16.dp),
          labelText = it.addonVariant.displayName,
          size = HighlightLabelDefaults.HighLightSize.Medium,
          color = HighlightLabelDefaults.HighlightColor.Blue(HighlightLabelDefaults.HighlightShade.LIGHT),
        )
        Spacer(Modifier.height(8.dp))
        it.addonVariant.documents.forEachIndexed { index, doc ->
          DocumentCard(
            onClick = { onDocumentClicked(doc.url) },
            title = doc.displayName,
            subtitle = null,
          )
          if (index != documents.lastIndex) {
            Spacer(Modifier.height(4.dp))
          }
        }
        Spacer(Modifier.height(16.dp))
      }
    }
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
private fun DocumentCard(onClick: () -> Unit, title: String, subtitle: String?, modifier: Modifier = Modifier) {
  HedvigCard(
    onClick = onClick,
    onClickLabel = stringResource(R.string.TALKBACK_OPEN_EXTERNAL_LINK),
    modifier = modifier
      .padding(horizontal = 16.dp)
      .heightIn(min = 56.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 17.dp),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Column {
            HedvigText(
              text = title,
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
          InsuranceVariantDocument("terms", "test", InsuranceVariantDocument.InsuranceDocumentType.GENERAL_TERMS),
          InsuranceVariantDocument("other doc", "", InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO),
        ),
        onDocumentClicked = {},
        addons = listOf(
          Addon(
            AddonVariant(
              perils = listOf(),
              termsVersion = "",
              displayName = "Travel plus 60",
              product = "Product",
              documents = listOf(
                InsuranceVariantDocument(
                  "terms",
                  "test",
                  InsuranceVariantDocument.InsuranceDocumentType.GENERAL_TERMS,
                ),
              ),
            ),
            UiMoney(19.0, UiCurrencyCode.SEK),
          ),
        ),
      )
    }
  }
}
