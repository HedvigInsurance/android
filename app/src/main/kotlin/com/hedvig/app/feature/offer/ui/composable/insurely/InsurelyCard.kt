package com.hedvig.app.feature.offer.ui.composable.insurely

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvigContentColorFor
import com.hedvig.android.core.designsystem.theme.hedvig_black12percent
import com.hedvig.android.core.designsystem.theme.warning
import com.hedvig.app.feature.offer.ui.OfferItems
import com.hedvig.app.feature.offer.ui.OfferItems.InsurelyCard.FailedToRetrieve
import com.hedvig.app.feature.offer.ui.OfferItems.InsurelyCard.Loading
import com.hedvig.app.feature.offer.ui.OfferItems.InsurelyCard.Retrieved
import com.hedvig.app.util.compose.preview.previewData
import java.util.Locale
import java.util.UUID

@Composable
fun InsurelyCard(
  data: OfferItems.InsurelyCard,
  locale: Locale,
  modifier: Modifier = Modifier,
) {
  val backgroundColor by animateColorAsState(
    targetValue = if (data is FailedToRetrieve) {
      MaterialTheme.colors.warning
    } else {
      MaterialTheme.colors.surface
    },
    label = "backgroundColor",
  )
  Card(
    border = BorderStroke(1.dp, hedvig_black12percent),
    backgroundColor = backgroundColor,
    contentColor = hedvigContentColorFor(backgroundColor),
    elevation = 0.dp,
    modifier = modifier,
  ) {
    Box(Modifier.animateContentSize()) {
      when (data) {
        is FailedToRetrieve -> FailedToRetrieveInfo(data.insuranceProviderDisplayName)
        is Loading -> LoadingRetrieval(locale)
        is Retrieved -> RetrievedInfo(data, locale)
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsurelyCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      Column {
        val insuranceProvider = "insuranceProvider"
        listOf(
          Loading(UUID.randomUUID().toString(), insuranceProvider),
          FailedToRetrieve(UUID.randomUUID().toString(), insuranceProvider),
          Retrieved.previewData(),
        ).forEach {
          InsurelyCard(it, Locale.ENGLISH)
        }
      }
    }
  }
}
