package com.hedvig.android.feature.changeaddress.ui.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.PreviewImageLoader
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import hedvig.resources.R

@Composable
internal fun QuoteCard(
  movingDate: String?,
  quote: MoveQuote,
  onExpandClicked: () -> Unit,
  isExpanded: Boolean,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = { onExpandClicked() },
    modifier = modifier,
  ) {
    Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
      PillAndBasicInfo(quote, movingDate, imageLoader)
      Spacer(Modifier.height(16.dp))
      Divider()
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
private fun PillAndBasicInfo(quote: MoveQuote, movingDate: String?, imageLoader: ImageLoader) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    AsyncImage(
      model = quote.productVariant.product.pillowImageUrl,
      contentDescription = "pillow",
      imageLoader = imageLoader,
      modifier = Modifier.size(48.dp),
    )
    Spacer(modifier = Modifier.width(16.dp))
    Column {
      Text(
        text = quote.insuranceName,
        style = MaterialTheme.typography.titleMedium,
        fontSize = 18.sp,
      )
      CompositionLocalProvider(LocalContentColor.provides(MaterialTheme.colorScheme.onSurfaceVariant)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = stringResource(id = R.string.CHANGE_ADDRESS_ACTIVATION_DATE, movingDate.toString()),
            fontSize = 18.sp,
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            painter = painterResource(id = com.hedvig.android.core.design.system.R.drawable.ic_info),
            contentDescription = null,
            modifier = Modifier
              .size(16.dp)
              .padding(1.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun QuoteDetailsAndPrice(
  isExpanded: Boolean,
  quote: MoveQuote,
) {

  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = stringResource(id = R.string.CHANGE_ADDRESS_DETAILS_LABEL),
          style = MaterialTheme.typography.titleMedium,
          fontSize = 18.sp,
        )
        Spacer(Modifier.width(8.dp))
        val angle = animateFloatAsState(
          targetValue = if (isExpanded) {
            0f
          } else {
            -180f
          },
          label = "",
        )
        Icon(
          painter = painterResource(com.hedvig.android.core.design.system.R.drawable.ic_drop_down_indicator),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.outlineVariant,
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
        style = MaterialTheme.typography.titleMedium,
        fontSize = 18.sp,
        textAlign = TextAlign.End,
      )
    },
  )
}

@Composable
private fun ExpandedInformation(
  quote: MoveQuote,
) {
  Column {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = { Text(stringResource(id = R.string.CHANGE_ADDRESS_NEW_ADDRESS_LABEL)) },
      endSlot = { Text(quote.address.street, textAlign = TextAlign.End) },
      spaceBetween = 4.dp,
    )
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = { Text(stringResource(id = R.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL)) },
      endSlot = { Text(quote.address.postalCode, textAlign = TextAlign.End) },
      spaceBetween = 4.dp,
    )
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = { Text(stringResource(id = R.string.CHANGE_ADDRESS_CO_INSURED_LABEL)) },
      endSlot = { Text(quote.numberCoInsured.toString(), textAlign = TextAlign.End) },
      spaceBetween = 4.dp,
    )
  }
}

@HedvigPreview
@Composable
fun PreviewQuoteCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      QuoteCard(
        movingDate = "2021-01-02",
        quote = MoveQuote.PreviewData(),
        onExpandClicked = {},
        isExpanded = true,
        modifier = Modifier.padding(16.dp),
        imageLoader = PreviewImageLoader(LocalContext.current),
      )
    }
  }
}
