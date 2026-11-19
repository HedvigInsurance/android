package com.hedvig.android.feature.insurances.insurancedetail.coverage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ExpandablePlusCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.insurances.data.InsurableLimitsUsage
import com.hedvig.android.feature.insurances.data.InsurableLimitsUsage.LimitUsage
import kotlinx.datetime.LocalDate

@Composable
internal fun InsurableLimitsUsageSection(
  usage: InsurableLimitsUsage,
  modifier: Modifier = Modifier,
  initiallyExpanded: Boolean = false,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
  ExpandablePlusCard(
    isExpanded = isExpanded,
    onClick = { isExpanded = !isExpanded },
    contentPadding = PaddingValues(16.dp),
    content = {
      Column {
        // TODO: Add "This contract year" / "Detta försäkringsår" to Lokalise
        HedvigText("This contract year")
        HedvigText(
          text = "${dateTimeFormatter.format(usage.periodStartDate)} – " +
            dateTimeFormatter.format(usage.periodEndDate),
          color = HedvigTheme.colorScheme.textSecondary,
          style = HedvigTheme.typography.label,
        )
      }
    },
    expandedContent = { UsageDetails(usage) },
    modifier = modifier,
  )
}

@Composable
private fun UsageDetails(usage: InsurableLimitsUsage) {
  Column {
    Spacer(Modifier.height(16.dp))
    if (usage.deductible != null) {
      // TODO: Add "Deductible" / "Självrisk" to Lokalise
      LimitUsageRow(label = "Deductible", usage = usage.deductible)
      HorizontalDivider(Modifier.padding(vertical = 16.dp))
    }
    // TODO: Add "Insurable limit" / "Ersättningstak" to Lokalise
    LimitUsageRow(label = "Insurable limit", usage = usage.total)
    if (usage.medicine != null) {
      Spacer(Modifier.height(16.dp))
      // TODO: Add "Of which medicine" / "Varav läkemedel" to Lokalise
      LimitUsageRow(
        label = "Of which medicine",
        usage = usage.medicine,
        barHeight = 4.dp,
        labelColor = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(start = 16.dp),
      )
    }
  }
}

@Composable
private fun LimitUsageRow(
  label: String,
  usage: LimitUsage,
  modifier: Modifier = Modifier,
  barHeight: Dp = 8.dp,
  labelColor: Color = HedvigTheme.colorScheme.textPrimary,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = { HedvigText(label, color = labelColor) },
      endSlot = {
        // TODO: Add "%1$s left" / "%1$s kvar" to Lokalise
        HedvigText(
          text = "${usage.remaining} left",
          textAlign = TextAlign.End,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      },
      spaceBetween = 8.dp,
    )
    UsageBar(fraction = usage.usedFraction, height = barHeight)
    // TODO: Add "%1$s of %2$s used" / "%1$s av %2$s använt" to Lokalise
    HedvigText(
      text = "${usage.used} of ${usage.limit} used",
      color = HedvigTheme.colorScheme.textSecondary,
      style = HedvigTheme.typography.label,
    )
  }
}

@Composable
private fun UsageBar(fraction: Float, height: Dp, modifier: Modifier = Modifier) {
  Box(
    modifier
      .fillMaxWidth()
      .height(height)
      .clip(CircleShape)
      .background(HedvigTheme.colorScheme.fillTertiaryTransparent)
      .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) },
  ) {
    if (fraction > 0f) {
      Box(
        Modifier
          .fillMaxWidth(fraction)
          .fillMaxHeight()
          .clip(CircleShape)
          .background(HedvigTheme.colorScheme.fillPrimary),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsurableLimitsUsageSection() {
  PreviewUsage(previewInsurableLimitsUsage)
}

@HedvigPreview
@Composable
private fun PreviewInsurableLimitsUsageSectionNoMedicine() {
  PreviewUsage(previewInsurableLimitsUsage.copy(medicine = null))
}

@HedvigPreview
@Composable
private fun PreviewInsurableLimitsUsageSectionNoDeductible() {
  PreviewUsage(previewInsurableLimitsUsage.copy(deductible = null))
}

@HedvigPreview
@Composable
private fun PreviewInsurableLimitsUsageSectionNothingUsed() {
  PreviewUsage(
    previewInsurableLimitsUsage.copy(
      total = previewLimitUsage(used = 0.0, limit = 100_000.0),
      medicine = previewLimitUsage(used = 0.0, limit = 10_000.0),
      deductible = previewLimitUsage(used = 0.0, limit = 2_000.0),
    ),
  )
}

@HedvigPreview
@Composable
private fun PreviewInsurableLimitsUsageSectionAlmostExhausted() {
  PreviewUsage(
    previewInsurableLimitsUsage.copy(
      total = previewLimitUsage(used = 97_500.0, limit = 100_000.0),
      medicine = previewLimitUsage(used = 10_000.0, limit = 10_000.0),
      deductible = previewLimitUsage(used = 2_000.0, limit = 2_000.0),
    ),
  )
}

@HedvigPreview
@Composable
private fun PreviewInsurableLimitsUsageSectionCollapsed() {
  PreviewUsage(previewInsurableLimitsUsage, initiallyExpanded = false)
}

@Composable
private fun PreviewUsage(usage: InsurableLimitsUsage, initiallyExpanded: Boolean = true) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InsurableLimitsUsageSection(usage, Modifier.padding(16.dp), initiallyExpanded)
    }
  }
}

private fun previewLimitUsage(used: Double, limit: Double): LimitUsage = LimitUsage(
  used = UiMoney(used, UiCurrencyCode.SEK),
  limit = UiMoney(limit, UiCurrencyCode.SEK),
  remaining = UiMoney((limit - used).coerceAtLeast(0.0), UiCurrencyCode.SEK),
)

internal val previewInsurableLimitsUsage = InsurableLimitsUsage(
  periodStartDate = LocalDate(2026, 3, 1),
  periodEndDate = LocalDate(2027, 2, 28),
  total = previewLimitUsage(used = 34_200.0, limit = 100_000.0),
  medicine = previewLimitUsage(used = 6_800.0, limit = 10_000.0),
  deductible = previewLimitUsage(used = 1_500.0, limit = 2_000.0),
)
