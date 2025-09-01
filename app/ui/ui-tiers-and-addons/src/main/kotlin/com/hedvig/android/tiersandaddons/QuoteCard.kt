package com.hedvig.android.tiersandaddons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.compose.ui.preview.TripleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.preview.TripleCase
import com.hedvig.android.compose.ui.stringWithShiftedLabel
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractGroup.DOG
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.InsuranceVariantDocument.InsuranceDocumentType.GENERAL_TERMS
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.HedvigButtonGhostWithBorder
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Small
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Grey
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.ripple
import hedvig.resources.R
import kotlinx.serialization.Serializable

@Serializable
data class QuoteDisplayItem(
  val title: String,
  val subtitle: String?,
  val value: String,
)

@Stable
interface QuoteCardState {
  var showDetails: Boolean
  val isEnabled: Boolean

  fun toggleState() {
    showDetails = !showDetails
  }

  companion object {
    val Saver = Saver<QuoteCardState, Boolean>(
      { it.showDetails },
      { QuoteCardStateImpl(it) },
    )
  }
}

private class QuoteCardStateImpl(initialShowDetails: Boolean) : QuoteCardState {
  override var showDetails by mutableStateOf(initialShowDetails)
  override val isEnabled: Boolean = true
}

@Composable
fun rememberQuoteCardState(showDetails: Boolean = false): QuoteCardState {
  return rememberSaveable(saver = QuoteCardState.Saver) { QuoteCardStateImpl(showDetails) }
}

@Composable
fun QuoteCard(
  productVariant: ProductVariant,
  subtitle: String,
  premium: UiMoney,
  previousPremium: UiMoney?,
  costBreakdown: List<Pair<String, String>>,
  displayItems: List<QuoteDisplayItem>,
  modifier: Modifier = Modifier,
  quoteCardState: QuoteCardState = rememberQuoteCardState(),
) {
  QuoteCard(
    quoteCardState = quoteCardState,
    subtitle = subtitle,
    premium = premium,
    previousPremium = previousPremium,
    isExcluded = false,
    costBreakdown = costBreakdown,
    displayItems = displayItems,
    modifier = modifier,
    displayName = productVariant.displayName,
    contractGroup = productVariant.contractGroup,
    insurableLimits = productVariant.insurableLimits,
    documents = productVariant.documents,
  )
}

@Composable
fun QuoteCard(
  quoteCardState: QuoteCardState,
  displayName: String,
  contractGroup: ContractGroup?,
  insurableLimits: List<InsurableLimit>,
  documents: List<InsuranceVariantDocument>,
  subtitle: String?,
  premium: UiMoney,
  previousPremium: UiMoney?,
  isExcluded: Boolean,
  costBreakdown: List<Pair<String, String>>,
  displayItems: List<QuoteDisplayItem>,
  modifier: Modifier = Modifier,
  betweenDetailsAndDocumentsContent: @Composable () -> Unit = {},
  excludedCollapsedStateButtonContent: @Composable () -> Unit = {},
) {
  QuoteCard(
    quoteCardState = quoteCardState,
    subtitle = subtitle,
    premium = premium,
    previousPremium = previousPremium,
    isExcluded = isExcluded,
    costBreakdown = costBreakdown,
    displayItems = displayItems,
    modifier = modifier,
    displayName = displayName,
    contractGroup = contractGroup,
    insurableLimits = insurableLimits,
    documents = documents,
    titleEndSlot = {
      Crossfade(
        targetState = isExcluded,
        modifier = Modifier.wrapContentSize(Alignment.TopEnd),
      ) { show ->
        if (show) {
          HighlightLabel(
            labelText = stringResource(R.string.CONTRACT_STATUS_TERMINATED),
            size = Small,
            color = Grey(MEDIUM),
          )
        }
      }
    },
    betweenDetailsAndDocumentsContent = betweenDetailsAndDocumentsContent,
    excludedCollapsedStateButtonContent = excludedCollapsedStateButtonContent,
  )
}

@Composable
fun QuoteCard(
  quoteCardState: QuoteCardState,
  displayName: String,
  contractGroup: ContractGroup?,
  insurableLimits: List<InsurableLimit>,
  documents: List<InsuranceVariantDocument>,
  subtitle: String?,
  premium: UiMoney,
  previousPremium: UiMoney?,
  isExcluded: Boolean,
  costBreakdown: List<Pair<String, String>>,
  displayItems: List<QuoteDisplayItem>,
  modifier: Modifier = Modifier,
  betweenDetailsAndDocumentsContent: @Composable () -> Unit = {},
) {
  QuoteCard(
    quoteCardState = quoteCardState,
    subtitle = subtitle,
    premium = premium,
    previousPremium = previousPremium,
    isExcluded = isExcluded,
    costBreakdown = costBreakdown,
    displayItems = displayItems,
    modifier = modifier,
    displayName = displayName,
    contractGroup = contractGroup,
    insurableLimits = insurableLimits,
    documents = documents,
    titleEndSlot = {
      Crossfade(
        targetState = isExcluded,
        modifier = Modifier.wrapContentSize(Alignment.TopEnd),
      ) { show ->
        if (show) {
          HighlightLabel(
            labelText = stringResource(R.string.CONTRACT_STATUS_TERMINATED),
            size = Small,
            color = Grey(MEDIUM),
          )
        }
      }
    },
    betweenDetailsAndDocumentsContent = betweenDetailsAndDocumentsContent,
  )
}

@Composable
fun QuoteCardDocumentsSection(documents: List<InsuranceVariantDocument>) {
  Column(Modifier.semantics(true) {}) {
    HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_DOCUMENTS_SUBTITLE))
    Column(
      verticalArrangement = Arrangement.spacedBy(6.dp),
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
          HedvigText(
            text = document.displayName,
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
private fun QuoteCard(
  quoteCardState: QuoteCardState,
  subtitle: String?,
  premium: UiMoney,
  previousPremium: UiMoney?,
  isExcluded: Boolean,
  costBreakdown: List<Pair<String, String>>,
  displayItems: List<QuoteDisplayItem>,
  displayName: String,
  contractGroup: ContractGroup?,
  insurableLimits: List<InsurableLimit>,
  documents: List<InsuranceVariantDocument>,
  modifier: Modifier = Modifier,
  titleEndSlot: @Composable () -> Unit = {},
  betweenDetailsAndDocumentsContent: @Composable () -> Unit = {},
  excludedCollapsedStateButtonContent: @Composable (() -> Unit)? = null,
) {
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
    onClick = quoteCardState::toggleState,
    enabled = quoteCardState.isEnabled,
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
        quoteCardState = quoteCardState,
        titleEndSlot = titleEndSlot,
        subtitle = subtitle,
        modifier = Modifier.semantics(mergeDescendants = true) {},
      )
      Spacer(Modifier.height(16.dp))
      if (excludedCollapsedStateButtonContent != null &&
        !quoteCardState.showDetails &&
        isExcluded
      ) {
        excludedCollapsedStateButtonContent()
      } else {
        HedvigButtonGhostWithBorder(
          text = if (quoteCardState.showDetails) {
            stringResource(R.string.TIER_FLOW_SUMMARY_HIDE_DETAILS_BUTTON)
          } else {
            stringResource(R.string.TIER_FLOW_SUMMARY_SHOW_DETAILS)
          },
          onClick = quoteCardState::toggleState,
          enabled = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      AnimatedVisibility(
        visible = quoteCardState.showDetails,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
      ) {
        QuoteDetails(
          displayItems = displayItems,
          insurableLimits = insurableLimits,
          documents = documents,
          betweenDetailsAndDocumentsContent = betweenDetailsAndDocumentsContent,
          modifier = Modifier.padding(top = 16.dp),
        )
      }
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
      TotalPriceRow(isExcluded, premium, previousPremium, Modifier.semantics(mergeDescendants = true) {})
    }
  }
}

@Composable
private fun QuoteIconAndTitle(
  contractGroup: ContractGroup?,
  displayName: String,
  quoteCardState: QuoteCardState,
  titleEndSlot: @Composable (() -> Unit),
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
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(
            text = displayName,
            maxLines = if (quoteCardState.showDetails) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier,
          )
        },
        endSlot = { titleEndSlot() },
        spaceBetween = 8.dp,
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
  documents: List<InsuranceVariantDocument>,
  betweenDetailsAndDocumentsContent: @Composable () -> Unit,
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
    betweenDetailsAndDocumentsContent.invoke()
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

@Composable
fun DiscountCostBreakdown(costBreakdown: List<Pair<String, String>>, modifier: Modifier = Modifier) {
  ProvideTextStyle(HedvigTheme.typography.label.copy(color = HedvigTheme.colorScheme.textSecondary)) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
      for (item in costBreakdown) {
        HorizontalItemsWithMaximumSpaceTaken(
          { HedvigText(item.first) },
          {
            HedvigText(
              text = item.second,
              textAlign = TextAlign.End,
            )
          },
          spaceBetween = 8.dp,
        )
      }
    }
  }
}

@Composable
private fun TotalPriceRow(
  isExcluded: Boolean,
  premium: UiMoney,
  previousPremium: UiMoney?,
  modifier: Modifier = Modifier,
) {
  HorizontalItemsWithMaximumSpaceTaken(
    modifier = modifier,
    spaceBetween = 8.dp,
    startSlot = {
      HedvigText(
        text = stringResource(R.string.TIER_FLOW_TOTAL),
        color = if (isExcluded) {
          HedvigTheme.colorScheme.textSecondaryTranslucent
        } else {
          Color.Unspecified
        },
      )
    },
    endSlot = {
      val premiumPerMonthDescription = premium.getPerMonthDescription()
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
        modifier = Modifier.semantics { contentDescription = premiumPerMonthDescription },
      ) {
        if (previousPremium != null && !isExcluded) {
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
          style = if (isExcluded) {
            LocalTextStyle.current.copy(
              color = HedvigTheme.colorScheme.textSecondaryTranslucent,
              textDecoration = TextDecoration.LineThrough,
            )
          } else {
            LocalTextStyle.current
          },
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
private fun PreviewQuoteCard(
  @PreviewParameter(TripleBooleanCollectionPreviewParameterProvider::class) triple: TripleCase,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      QuoteCard(
        quoteCardState = rememberQuoteCardState(triple == TripleCase.FIRST),
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
          InsuranceVariantDocument(
            displayName = "displayName#$it",
            url = "url#$it",
            type = GENERAL_TERMS,
          )
        },
        subtitle = "subtitle",
        premium = UiMoney(281.0, UiCurrencyCode.SEK),
        previousPremium = UiMoney(381.0, UiCurrencyCode.SEK),
        isExcluded = triple == TripleCase.SECOND,
        costBreakdown = List(3) { "#$it" to "discount#$it" },
        displayItems = List(5) {
          QuoteDisplayItem(
            title = "title$it",
            subtitle = "subtitle$it",
            value = "value$it",
          )
        },
      )
    }
  }
}
