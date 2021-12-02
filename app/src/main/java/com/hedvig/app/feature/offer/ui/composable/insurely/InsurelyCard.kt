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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.offer.ui.OfferModel.InsurelyCard.FailedToRetrieve
import com.hedvig.app.feature.offer.ui.OfferModel.InsurelyCard.Loading
import com.hedvig.app.feature.offer.ui.OfferModel.InsurelyCard.Retrieved
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.hedvigBlack12percent
import com.hedvig.app.ui.compose.theme.hedvigContentColorFor
import com.hedvig.app.util.compose.preview.previewData

// TODO string resources
@Composable
fun InsurelyCard(
    data: OfferModel.InsurelyCard,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (data is FailedToRetrieve) {
            colorResource(R.color.colorWarning)
        } else {
            MaterialTheme.colors.surface
        }
    )
    Card(
        border = BorderStroke(1.dp, hedvigBlack12percent),
        backgroundColor = backgroundColor,
        contentColor = hedvigContentColorFor(backgroundColor),
        elevation = 0.dp,
        modifier = modifier,
    ) {
        Box(Modifier.animateContentSize()) {
            when (data) {
                is FailedToRetrieve -> FailedToRetrieveInfo(data.insuranceProvider)
                is Loading -> LoadingRetrieval(data.insuranceProvider)
                is Retrieved -> RetrievedInfo(data)
            }
        }
    }
}

@Preview
@Composable
fun InsurelyCardPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            Column {
                val insuranceProvider = "insuranceProvider"
                listOf(
                    Loading(insuranceProvider),
                    FailedToRetrieve(insuranceProvider),
                    Retrieved.previewData(),
                ).forEach {
                    InsurelyCard(it)
                }
            }
        }
    }
}
