package com.hedvig.app.feature.offer.ui.composable.insurely

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.hedvigBlack12percent

@Composable
fun InsurelyCard(data: OfferModel.InsurelyCard) {
    Card(
        border = BorderStroke(1.dp, hedvigBlack12percent),
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            when (data) {
                is OfferModel.InsurelyCard.FailedToRetrieve -> Text(text = "OfferModel.InsurelyCard.FailedToRetrieve")
                is OfferModel.InsurelyCard.Loading -> Text(text = "OfferModel.InsurelyCard.Loading")
                is OfferModel.InsurelyCard.Retrieved -> Text(text = "OfferModel.InsurelyCard.Retrieved")
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
                    OfferModel.InsurelyCard.Loading(insuranceProvider),
                    OfferModel.InsurelyCard.FailedToRetrieve(insuranceProvider),
                    OfferModel.InsurelyCard.Retrieved(insuranceProvider, emptyList(), null),
                ).forEach {
                    InsurelyCard(it)
                }
            }
        }
    }
}
