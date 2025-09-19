package com.hedvig.android.tiersandaddons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.compose.ui.stringWithShiftedLabel
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractGroup.DOG
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButtonGhostWithBorder
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.ripple
import com.hedvig.android.design.system.hedvig.show
import hedvig.resources.R
import kotlinx.serialization.Serializable

@Serializable
data class QuoteDisplayItem(
  val title: String,
  val value: String,
  val subtitle: String?,
)

@Composable
fun QuoteCard(
  productVariant: ProductVariant,
  subtitle: String,
  premium: UiMoney,
  previousPremium: UiMoney?,
  costBreakdown: List<CostBreakdownEntry>,
  displayItems: List<QuoteDisplayItem>,
  modifier: Modifier = Modifier,
) {
  QuoteCard(
    displayName = productVariant.displayName,
    contractGroup = productVariant.contractGroup,
    insurableLimits = productVariant.insurableLimits,
    documents = productVariant.documents.map {
      DisplayDocument(
        displayName = it.displayName,
        url = it.url,
      )
    },
    subtitle = subtitle,
    premium = premium,
    previousPremium = previousPremium,
    costBreakdown = costBreakdown,
    displayItems = displayItems,
    modifier = modifier,
  )
}

@Composable
fun QuoteCard(
  displayName: String,
  contractGroup: ContractGroup?,
  insurableLimits: List<InsurableLimit>,
  documents: List<DisplayDocument>,
  subtitle: String?,
  premium: UiMoney,
  previousPremium: UiMoney?,
  isExcluded: Boolean,
  costBreakdown: List<CostBreakdownEntry>,
  displayItems: List<QuoteDisplayItem>,
  modifier: Modifier = Modifier,
) {
  val quoteDetailsBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  QuoteDetailsBottomSheet(
    quoteDetailsBottomSheetState,
    displayItems,
    insurableLimits,
    documents,
  )
  HedvigCard(
    modifier = modifier
      .shadow(elevation = 2.dp, shape = HedvigTheme.shapes.cornerXLarge)
      .border(
        shape = HedvigTheme.shapes.cornerXLarge,
        color = HedvigTheme.colorScheme.borderPrimary,
        width = 1.dp,
      ),
    color = HedvigTheme.colorScheme.backgroundPrimary,
    shape = HedvigTheme.shapes.cornerXLarge,
    interactionSource = null,
    indication = ripple(bounded = true, radius = 1000.dp),
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .semantics {
          isTraversalGroup = true
        },
    ) {
      QuoteIconAndTitle(
        contractGroup = contractGroup,
        displayName = displayName,
        subtitle = subtitle,
        modifier = Modifier.semantics(mergeDescendants = true) {},
      )
      Spacer(Modifier.height(16.dp))
      HedvigButtonGhostWithBorder(
        text = stringResource(R.string.TIER_FLOW_SUMMARY_SHOW_DETAILS),
        onClick = { quoteDetailsBottomSheetState.show() },
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
      )
      if (costBreakdown.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        DiscountCostBreakdown(
          costBreakdown,
          Modifier.semantics(mergeDescendants = true) {},
        )
      }
      Spacer(Modifier.height(16.dp))
      HorizontalDivider()
      Spacer(Modifier.height(16.dp))
      TotalPriceRow(premium, previousPremium, Modifier.semantics(mergeDescendants = true) {})
    }
  }
}

@Composable
fun AddonQuoteCardDocumentsSection(documentsDisplayNameUrls: List<Pair<String, String>>) {
  Column(Modifier.semantics(true) {}) {
    HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_DOCUMENTS_SUBTITLE))
    Column(
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      for (document in documentsDisplayNameUrls) {
        val uriHandler = LocalUriHandler.current
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(HedvigTheme.shapes.cornerXSmall)
            .clickable {
              uriHandler.openUri(document.second)
            },
        ) {
          HedvigText(
            text = document.first,
            style = HedvigTheme.typography.bodySmall,
            color = HedvigTheme.colorScheme.textSecondary,
            modifier = Modifier.weight(1f),
          )
          Spacer(Modifier.width(8.dp))
          LayoutWithoutPlacement(
            sizeAdjustingContent = {
              HedvigText(
                "H",
                style = HedvigTheme.typography.bodySmall,
              )
            },
          ) {
            val density = LocalDensity.current
            Icon(
              imageVector = HedvigIcons.ArrowNorthEast,
              contentDescription = stringResource(R.string.TALKBACK_OPEN_EXTERNAL_LINK),
              tint = HedvigTheme.colorScheme.fillPrimary,
              modifier = Modifier
                .wrapContentSize(Alignment.Center)
                .then(with(density) { Modifier.size(24.sp.toDp()) }),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun QuoteDetailsBottomSheet(
  quoteDetailsBottomSheetState: HedvigBottomSheetState<Unit>,
  quoteDisplayItems: List<QuoteDisplayItem>,
  insurableLimits: List<InsurableLimit>,
  insuranceVariantDocuments: List<InsuranceVariantDocument>,
) {
  HedvigBottomSheet(
    hedvigBottomSheetState = quoteDetailsBottomSheetState,
  ) {
    QuoteDetails(
      displayItems = quoteDisplayItems,
      insurableLimits = insurableLimits,
      documents = insuranceVariantDocuments,
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
      onClick = quoteDetailsBottomSheetState::dismiss,
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

data class CostBreakdownEntry(
  val displayName: String,
  val displayValue: String,
  val hasStrikethrough: Boolean,
)

@Composable
private fun QuoteIconAndTitle(
  contractGroup: ContractGroup?,
  displayName: String,
  subtitle: String?,
  modifier: Modifier = Modifier,
) {
  Row(modifier) {
    if (contractGroup != null) {
      Image(
        painter = painterResource(contractGroup.toPillow()),
        contentDescription = null, // CHECKED
        modifier = Modifier.size(48.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
    }
    Column(
      Modifier
        .weight(1f)
        .semantics(mergeDescendants = true) {},
    ) {
      HedvigText(
        text = displayName,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier,
      )
      AnimatedContent(
        targetState = subtitle,
        transitionSpec = {
          (fadeIn() + expandVertically(expandFrom = Alignment.Top))
            .togetherWith(fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top))
        },
        modifier = Modifier.fillMaxWidth(),
      ) { subtitle ->
        if (subtitle != null) {
          HedvigText(
            text = subtitle,
            color = HedvigTheme.colorScheme.textSecondary,
            modifier = Modifier.fillMaxWidth(),
          )
        } else {
          Box(Modifier.fillMaxWidth())
        }
      }
    }
  }
}

@Composable
private fun QuoteDetails(
  displayItems: List<QuoteDisplayItem>,
  insurableLimits: List<InsurableLimit>,
  documents: List<DisplayDocument>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.semantics(true) {},
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    if (displayItems.isNotEmpty()) {
      Column {
        HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_OVERVIEW_SUBTITLE))
        for (displayItem in displayItems) {
          InfoRow(displayItem.title, displayItem.value)
        }
      }
    }
    if (insurableLimits.isNotEmpty()) {
      Column(
        modifier = Modifier.semantics(true) {},
      ) {
        HedvigText(
          stringResource(R.string.TIER_FLOW_SUMMARY_COVERAGE_SUBTITLE),
        )
        Column {
          for (insurableLimit in insurableLimits) {
            InfoRow(
              insurableLimit.label,
              insurableLimit.limit,
            )
          }
        }
      }
    }
    if (documents.isNotEmpty()) {
      Column {
        HedvigText(
          stringResource(R.string.TIER_FLOW_SUMMARY_DOCUMENTS_SUBTITLE),
          modifier = Modifier.semantics(true) {},
        )
        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.semantics(true) {},
        ) {
          for (document in documents) {
            val uriHandler = LocalUriHandler.current
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(HedvigTheme.shapes.cornerXSmall)
                .clickable {
                  uriHandler.openUri(document.url)
                },
            ) {
              val voiceoverDescription = stringResource(R.string.TALKBACK_DOCUMENT, document.displayName)
              HedvigText(
                text = stringWithShiftedLabel(
                  text = document.displayName,
                  labelText = "PDF",
                  labelFontSize = HedvigTheme.typography.label.fontSize,
                  textColor = HedvigTheme.colorScheme.textSecondary,
                  textFontSize = LocalTextStyle.current.fontSize,
                ),
                style = HedvigTheme.typography.bodySmall,
                modifier = Modifier
                  .weight(1f)
                  .semantics {
                    contentDescription = voiceoverDescription
                  },
              )
              Spacer(Modifier.width(8.dp))
              LayoutWithoutPlacement(
                sizeAdjustingContent = {
                  HedvigText(
                    "H",
                    style = HedvigTheme.typography.bodySmall,
                  )
                },
              ) {
                val density = LocalDensity.current
                Icon(
                  imageVector = HedvigIcons.ArrowNorthEast,
                  contentDescription = stringResource(R.string.TALKBACK_OPEN_EXTERNAL_LINK),
                  tint = HedvigTheme.colorScheme.fillPrimary,
                  modifier = Modifier
                    .wrapContentSize(Alignment.Center)
                    .then(with(density) { Modifier.size(24.sp.toDp()) }),
                )
              }
            }
          }
        }
      }
    }
  }
}

data class DisplayDocument(
  val displayName: String,
  val url: String,
)

@Composable
fun DiscountCostBreakdown(costBreakdown: List<CostBreakdownEntry>, modifier: Modifier = Modifier) {
  ProvideTextStyle(HedvigTheme.typography.label.copy(color = HedvigTheme.colorScheme.textSecondary)) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
      for (item in costBreakdown) {
        val style = if (item.hasStrikethrough) {
          LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough)
        } else {
          LocalTextStyle.current
        }
        val strikeThroughDescription = stringResource(
          R.string.TALKBACK_PREVIOUSLY,
          "${item.displayName}, ${item.displayValue}",
        )
        HorizontalItemsWithMaximumSpaceTaken(
          { HedvigText(item.displayName, style = style) },
          {
            HedvigText(
              text = item.displayValue,
              textAlign = TextAlign.End,
              style = style,
            )
          },
          spaceBetween = 8.dp,
          modifier = Modifier.semantics(mergeDescendants = true) {
            if (item.hasStrikethrough) {
              contentDescription = strikeThroughDescription
            }
          },
        )
      }
    }
  }
}

@Composable
private fun TotalPriceRow(
  premium: UiMoney,
  previousPremium: UiMoney?,
  modifier: Modifier = Modifier,
) {
  HorizontalItemsWithMaximumSpaceTaken(
    modifier = modifier,
    spaceBetween = 8.dp,
    startSlot = {
      HedvigText(text = stringResource(R.string.TIER_FLOW_TOTAL))
    },
    endSlot = {
      val premiumPerMonthDescription = premium.getPerMonthDescription()
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
        modifier = Modifier.semantics { contentDescription = premiumPerMonthDescription },
      ) {
        if (previousPremium != null) {
          HedvigText(
            text = stringResource(
              R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              previousPremium,
            ),
            textAlign = TextAlign.End,
            style = LocalTextStyle.current.copy(
              color = HedvigTheme.colorScheme.textSecondaryTranslucent,
              textDecoration = TextDecoration.LineThrough,
            ),
          )
        }
        HedvigText(
          text = stringResource(
            R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
            premium,
          ),
          textAlign = TextAlign.End,
          modifier = Modifier.wrapContentWidth(Alignment.End),
        )
      }
    },
  )
}

@Composable
private fun InfoRow(leftText: String, rightText: String, modifier: Modifier = Modifier) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      HedvigText(leftText, color = HedvigTheme.colorScheme.textSecondary)
    },
    endSlot = {
      HedvigText(
        text = rightText,
        color = HedvigTheme.colorScheme.textSecondary,
        textAlign = TextAlign.End,
        modifier = Modifier.wrapContentWidth(Alignment.End),
      )
    },
    modifier = modifier,
    spaceBetween = 8.dp,
  )
}

@HedvigPreview
@Composable
private fun PreviewQuoteCard() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      QuoteCard(
        displayName = "displayName",
        contractGroup = DOG,
        insurableLimits = List(3) {
          InsurableLimit(
            label = "label#$it",
            limit = "limit#$it",
            description = "description#$it",
          )
        },
        documents = List(3) {
          DisplayDocument(
            displayName = "displayName#$it",
            url = "url#$it",
          )
        },
        subtitle = "subtitle",
        premium = UiMoney(281.0, UiCurrencyCode.SEK),
        previousPremium = UiMoney(381.0, UiCurrencyCode.SEK),
        isExcluded = false,
        costBreakdown = List(3) {
          CostBreakdownEntry(
            "#$it",
            "discount#$it",
            false,
          )
        },
        displayItems = List(5) {
          QuoteDisplayItem(
            title = "title$it",
            value = "value$it",
            subtitle = "subtitle$it",
          )
        },
      )
    }
  }
}
