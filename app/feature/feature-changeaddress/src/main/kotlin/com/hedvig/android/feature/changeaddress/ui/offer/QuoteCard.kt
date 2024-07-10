package com.hedvig.android.feature.changeaddress.ui.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronDown
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.hedvigDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.design.system.hedvig.ripple
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun QuoteCard(
  movingDate: LocalDate,
  quote: MoveQuote,
  onExpandClicked: () -> Unit,
  isExpanded: Boolean,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    modifier = modifier
      .clip(MaterialTheme.shapes.squircleMedium)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(
          bounded = true,
          radius = 1000.dp,
        ),
        onClick = { onExpandClicked() },
      ),
  ) {
    Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
      PillAndBasicInfo(quote, movingDate)
      Spacer(Modifier.height(16.dp))
      HorizontalDivider()
      Spacer(Modifier.height(16.dp))
      QuoteDetailsAndPrice(isExpanded, quote)
      Spacer(Modifier.height(16.dp))
      CompositionLocalProvider(LocalContentColor.provides(MaterialTheme.colorScheme.onSurfaceVariant)) {
        AnimatedVisibility(
          visible = isExpanded,
          enter = fadeIn() + expandVertically(clip = false, expandFrom = Alignment.Top),
          exit = fadeOut() + shrinkVertically(clip = false, shrinkTowards = Alignment.Top),
        ) {
          Column {
            ExpandedInformation(quote)
            Spacer(Modifier.height(16.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun PillAndBasicInfo(quote: MoveQuote, movingDate: LocalDate) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Image(
      painter = painterResource(id = quote.productVariant.contractGroup.toPillow()),
      contentDescription = null,
      modifier = Modifier.size(48.dp),
    )
    Spacer(modifier = Modifier.width(16.dp))
    Column {
      Text(
        text = quote.insuranceName,
        style = MaterialTheme.typography.bodyLarge,
      )
      CompositionLocalProvider(LocalContentColor.provides(MaterialTheme.colorScheme.onSurfaceVariant)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = stringResource(
              id = R.string.CHANGE_ADDRESS_ACTIVATION_DATE,
              movingDate.toJavaLocalDate().format(hedvigDateTimeFormatter(getLocale())),
            ),
            style = MaterialTheme.typography.bodyLarge,
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Hedvig.InfoFilled,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun QuoteDetailsAndPrice(isExpanded: Boolean, quote: MoveQuote) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = stringResource(id = R.string.CHANGE_ADDRESS_DETAILS_LABEL),
          style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.width(8.dp))
        val angle = animateFloatAsState(
          targetValue = if (isExpanded) {
            -180f
          } else {
            0f
          },
          label = "",
        )
        Icon(
          imageVector = Icons.Hedvig.ChevronDown,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier
            .size(16.dp)
            .graphicsLayer {
              rotationZ = angle.value
            },
        )
      }
    },
    endSlot = {
      Text(
        text = stringResource(
          id = R.string.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL,
          quote.premium.toString(),
        ),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.End,
      )
    },
  )
}

@Composable
private fun ExpandedInformation(quote: MoveQuote) {
  Column {
    quote.displayItems.forEach {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { Text(it.first) },
        endSlot = { Text(it.second, textAlign = TextAlign.End) },
        spaceBetween = 4.dp,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewQuoteCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      QuoteCard(
        movingDate = LocalDate.fromEpochDays(3000),
        quote = MoveQuote.PreviewData(),
        onExpandClicked = {},
        isExpanded = true,
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}
