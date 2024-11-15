package com.hedvig.android.feature.insurances.insurancedetail.coverage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.ProductVariantPeril
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.PerilData
import com.hedvig.android.design.system.hedvig.PerilDefaults.PerilSize.Small
import com.hedvig.android.design.system.hedvig.PerilList
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState

@Composable
internal fun CoverageTab(
  insurableLimits: List<InsurableLimit>,
  perils: List<ProductVariantPeril>,
  modifier: Modifier = Modifier,
) {
  val bottomSheetState = rememberHedvigBottomSheetState<InsurableLimit>()
  HedvigBottomSheet(bottomSheetState) { selectedInsurableLimit ->
    HedvigText(stringResource(hedvig.resources.R.string.CONTRACT_COVERAGE_MORE_INFO))
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = selectedInsurableLimit.description,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(hedvig.resources.R.string.general_close_button),
      buttonSize = Large,
      onClick = { bottomSheetState.dismiss() },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }

  Column(modifier = modifier) {
    InsurableLimitSection(
      insurableLimits = insurableLimits,
      onInsurableLimitClick = { insurableLimit: InsurableLimit ->
        bottomSheetState.show(insurableLimit)
      },
    )
    Spacer(Modifier.height(16.dp))
    if (perils.isNotEmpty()) {
      PerilList(
        perilItems = perils.map {
          PerilData(
            title = it.title,
            description = it.description,
            covered = it.covered,
            colorCode = it.colorCode,
          )
        },
        size = Small,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
    }
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.InsurableLimitSection(
  insurableLimits: List<InsurableLimit>,
  onInsurableLimitClick: (InsurableLimit) -> Unit,
) {
  val dividerColor = HedvigTheme.colorScheme.borderSecondary
  insurableLimits.mapIndexed { index, insurableLimitItem ->
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 16.dp),
        ) {
          HedvigText(insurableLimitItem.label)
        }
      },
      endSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.padding(vertical = 16.dp),
        ) {
          HedvigText(
            text = insurableLimitItem.limit,
            textAlign = TextAlign.End,
            color = HedvigTheme.colorScheme.textSecondary,
            modifier = Modifier.align(Alignment.CenterVertically),
          )
          Spacer(Modifier.width(8.dp))
          Icon(
            imageVector = HedvigIcons.InfoFilled,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = HedvigTheme.colorScheme.fillSecondary,
          )
        }
      },
      spaceBetween = 8.dp,
      modifier = Modifier
        .heightIn(min = 56.dp)
        .fillMaxWidth()
        .clickable { onInsurableLimitClick(insurableLimitItem) }
        .padding(horizontal = 16.dp)
        .then(
          if (index != 0) {
            Modifier.drawWithContent {
              drawContent()
              if (index != insurableLimits.lastIndex) {
                drawLine(dividerColor, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx())
              }
            }
          } else {
            Modifier
          },
        ),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewCoverageTab() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CoverageTab(
        previewInsurableLimits,
        previewPerils,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsurableLimitSection() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column {
        InsurableLimitSection(
          previewInsurableLimits,
          {},
        )
      }
    }
  }
}

private val previewPerils: List<ProductVariantPeril> = List(4) { index ->
  ProductVariantPeril(
    id = index.toString(),
    title = "Eldsvåda",
    description = "description$index",
    covered = listOf("Covered#$index"),
    colorCode = "0xFFC45D4F",
    exceptions = listOf(),
  )
}

private val previewInsurableLimits: List<InsurableLimit> = listOf(
  InsurableLimit("Insured amount".repeat(2), "1 000 000 kr", ""),
  InsurableLimit("Deductible", "1 500 kr", ""),
  InsurableLimit("Travel insurance", "45 days", ""),
)
