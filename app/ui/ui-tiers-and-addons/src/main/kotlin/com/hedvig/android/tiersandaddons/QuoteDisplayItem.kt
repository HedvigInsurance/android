package com.hedvig.android.tiersandaddons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.data.contract.ContractGroup.DOG
import com.hedvig.android.data.contract.ContractType.SE_HOUSE
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.ripple
import hedvig.resources.R

data class QuoteDisplayItem(
  val title: String,
  val subtitle: String?,
  val value: String,
)

@Composable
fun QuoteCard(
  productVariant: ProductVariant,
  subtitle: String,
  premium: String,
  displayItems: List<QuoteDisplayItem>,
  modifier: Modifier = Modifier,
) {
  var showDetails by rememberSaveable { mutableStateOf(false) }
  QuoteCard(
    showDetails = showDetails,
    setShowDetails = { showDetails = it },
    productVariant = productVariant,
    subtitle = subtitle,
    premium = premium,
    displayItems = displayItems,
    modifier = modifier,
  )
}

@Composable
private fun QuoteCard(
  showDetails: Boolean,
  setShowDetails: (Boolean) -> Unit,
  productVariant: ProductVariant,
  subtitle: String,
  premium: String,
  displayItems: List<QuoteDisplayItem>,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    modifier = modifier,
    onClick = { setShowDetails(!showDetails) },
    interactionSource = null,
    indication = ripple(bounded = true, radius = 1000.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row {
        Image(
          painter = painterResource(productVariant.contractGroup.toPillow()),
          contentDescription = null,
          modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          HedvigText(
            text = productVariant.displayName,
          )
          HedvigText(
            text = subtitle,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(stringResource(R.string.TIER_FLOW_TOTAL))
        },
        endSlot = {
          HedvigText(
            text = stringResource(
              R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              premium,
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.wrapContentWidth(Alignment.End),
          )
        },
      )
      AnimatedVisibility(
        visible = showDetails,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
      ) {
        Column {
          Spacer(Modifier.height(16.dp))
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            HorizontalDivider()
            if (displayItems.isNotEmpty()) {
              Column {
                HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_OVERVIEW_SUBTITLE))
                for (displayItem in displayItems) {
                  InfoRow(displayItem.title, displayItem.value)
                }
              }
            }
            if (productVariant.insurableLimits.isNotEmpty()) {
              Column {
                HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_COVERAGE_SUBTITLE))
                for (insurableLimit in productVariant.insurableLimits) {
                  InfoRow(
                    insurableLimit.label,
                    insurableLimit.limit,
                  )
                }
              }
            }
            if (productVariant.documents.isNotEmpty()) {
              Column {
                HedvigText(stringResource(R.string.TIER_FLOW_SUMMARY_DOCUMENTS_SUBTITLE))
                for (document in productVariant.documents) {
                  val uriHandler = LocalUriHandler.current
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(HedvigTheme.shapes.cornerExtraSmall)
                      .clickable {
                        uriHandler.openUri(document.url)
                      },
                  ) {
                    HedvigText(
                      text = document.displayName,
                      color = HedvigTheme.colorScheme.textSecondary,
                      modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                      imageVector = HedvigIcons.ArrowNorthEast,
                      contentDescription = null,
                      tint = HedvigTheme.colorScheme.fillPrimary,
                      modifier = Modifier.wrapContentWidth(),
                    )
                  }
                }
              }
            }
          }
        }
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = if (showDetails) {
          stringResource(R.string.TIER_FLOW_SUMMARY_HIDE_DETAILS_BUTTON)
        } else {
          stringResource(R.string.TIER_FLOW_SUMMARY_SHOW_DETAILS)
        },
        onClick = { setShowDetails(!showDetails) },
        enabled = true,
        buttonStyle = Secondary,
        buttonSize = Medium,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
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
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) showDetails: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      QuoteCard(
        showDetails = showDetails,
        setShowDetails = {},
        productVariant = ProductVariant(
          displayName = "displayName",
          contractGroup = DOG,
          contractType = SE_HOUSE,
          partner = "partner",
          perils = emptyList(),
          insurableLimits = emptyList(),
          documents = emptyList(),
          displayTierName = "displayTierName",
          tierDescription = "tierDescription",
        ),
        subtitle = "subtitle",
        premium = "premium",
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