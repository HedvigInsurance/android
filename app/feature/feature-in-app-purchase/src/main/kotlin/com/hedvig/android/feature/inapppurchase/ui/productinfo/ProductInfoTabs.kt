package com.hedvig.android.feature.inapppurchase.ui.productinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariantPeril
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ExpandablePlusCard
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigMarkdownText
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.PerilData
import com.hedvig.android.design.system.hedvig.PerilDefaults.PerilSize
import com.hedvig.android.design.system.hedvig.PerilList
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFaqItem
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseMocks
import hedvig.resources.HC_COMMON_QUESTIONS_TITLE
import hedvig.resources.Res
import hedvig.resources.TALKBACK_OPEN_EXTERNAL_LINK
import hedvig.resources.general_close_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OverviewTab(faq: List<InAppPurchaseFaqItem>, modifier: Modifier = Modifier) {
  Column(modifier.padding(horizontal = 16.dp)) {
    if (faq.isNotEmpty()) {
      HedvigText(
        text = stringResource(Res.string.HC_COMMON_QUESTIONS_TITLE),
        modifier = Modifier
          .padding(horizontal = 2.dp)
          .semantics { heading() },
      )
      Spacer(Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (item in faq) {
          FaqItem(item)
        }
      }
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun FaqItem(item: InAppPurchaseFaqItem) {
  var isExpanded by rememberSaveable(item.question) { mutableStateOf(false) }
  ExpandablePlusCard(
    isExpanded = isExpanded,
    onClick = { isExpanded = !isExpanded },
    contentPadding = PaddingValues(start = 16.dp, top = 15.dp, end = 16.dp, bottom = 17.dp),
    content = { HedvigText(text = item.question, modifier = Modifier.weight(1f)) },
    expandedContent = {
      Column {
        Spacer(Modifier.height(16.dp))
        HedvigMarkdownText(
          content = item.answerMarkdown,
          style = HedvigTheme.typography.bodySmall.copy(color = HedvigTheme.colorScheme.textSecondary),
        )
      }
    },
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
internal fun CoverageTab(
  insurableLimits: List<InsurableLimit>,
  perils: List<ProductVariantPeril>,
  modifier: Modifier = Modifier,
) {
  val bottomSheetState = rememberHedvigBottomSheetState<InsurableLimit>()
  HedvigBottomSheet(bottomSheetState) { selectedInsurableLimit ->
    HedvigText(selectedInsurableLimit.label, Modifier.semantics { heading() })
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = selectedInsurableLimit.description,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_close_button),
      buttonSize = Large,
      onClick = { bottomSheetState.dismiss() },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
  Column(modifier) {
    InsurableLimitRows(insurableLimits = insurableLimits, onClick = { bottomSheetState.show(it) })
    if (insurableLimits.isNotEmpty()) {
      Spacer(Modifier.height(16.dp))
    }
    if (perils.isNotEmpty()) {
      PerilList(
        perilItems = perils.map {
          PerilData(title = it.title, description = it.description, covered = it.covered, colorCode = it.colorCode)
        },
        size = PerilSize.Small,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun InsurableLimitRows(insurableLimits: List<InsurableLimit>, onClick: (InsurableLimit) -> Unit) {
  val dividerColor = HedvigTheme.colorScheme.borderSecondary
  insurableLimits.forEachIndexed { index, insurableLimit ->
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigText(insurableLimit.label, Modifier.padding(vertical = 16.dp))
      },
      endSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.padding(vertical = 16.dp),
        ) {
          HedvigText(
            text = insurableLimit.limit,
            textAlign = TextAlign.End,
            color = HedvigTheme.colorScheme.textSecondary,
          )
          Spacer(Modifier.width(8.dp))
          Icon(
            imageVector = HedvigIcons.InfoFilled,
            contentDescription = null,
            tint = HedvigTheme.colorScheme.fillSecondary,
            modifier = Modifier.size(24.dp),
          )
        }
      },
      spaceBetween = 8.dp,
      modifier = Modifier
        .heightIn(min = 56.dp)
        .fillMaxWidth()
        .clickable { onClick(insurableLimit) }
        .padding(horizontal = 16.dp)
        .then(
          if (index != 0) {
            Modifier.drawWithContent {
              drawContent()
              drawLine(dividerColor, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx())
            }
          } else {
            Modifier
          },
        ),
    )
  }
}

@Composable
internal fun DocumentsTab(
  documents: List<InsuranceVariantDocument>,
  onDocumentClick: (url: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    for (document in documents) {
      DocumentCard(title = document.displayName, onClick = { onDocumentClick(document.url) })
    }
    Spacer(Modifier.height(12.dp))
  }
}

@Composable
private fun DocumentCard(title: String, onClick: () -> Unit) {
  HedvigCard(
    onClick = onClick,
    onClickLabel = stringResource(Res.string.TALKBACK_OPEN_EXTERNAL_LINK),
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .heightIn(min = 56.dp),
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = { HedvigText(text = title) },
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
      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 17.dp),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewProductInfoTabs() {
  val product = InAppPurchaseMocks.paymentProtectionForm.product
  val variant = product.variants.first()
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column {
        OverviewTab(product.faq)
        CoverageTab(variant.insurableLimits, variant.perils)
        DocumentsTab(variant.documents, {})
      }
    }
  }
}
