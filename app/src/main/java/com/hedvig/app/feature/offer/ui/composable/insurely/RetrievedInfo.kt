package com.hedvig.app.feature.offer.ui.composable.insurely

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewData
import javax.money.MonetaryAmount

@Composable
fun RetrievedInfo(data: OfferModel.InsurelyCard.Retrieved) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        if (data.savedWithHedvig != null) {
            SavedWithHedvigChip(data.savedWithHedvig)
            Spacer(Modifier.height(6.dp))
        }
        Spacer(Modifier.height(8.dp))
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = if (data.currentInsurances.size > 1) {
                    LocalContext.current.resources.getQuantityString(
                        R.plurals.offer_switcher_title,
                        data.currentInsurances.size
                    )
                } else {
                    "YOUR INSURANCE${data.insuranceProvider?.let { " WITH $it" }}"
                },
                style = MaterialTheme.typography.caption
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "${data.totalNetPremium}", // todo locale formatting
            style = MaterialTheme.typography.h4,
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = stringResource(R.string.OFFER_PRICE_PER_MONTH),
                style = MaterialTheme.typography.body2
            )
        }
        if (data.currentInsurances.size <= 1) {
            Spacer(Modifier.height(8.dp))
        } else {
            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))
            data.currentInsurances.forEach { insurance ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = insurance.name,
                        style = MaterialTheme.typography.subtitle1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = insurance.amount.toString(), // todo locale formatting
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedWithHedvigChip(savedWithHedvig: MonetaryAmount) {
    Card(
        shape = RoundedCornerShape(4.dp),
        backgroundColor = MaterialTheme.colors.secondary,
    ) {
        Text(
            text = "SAVE $savedWithHedvig/MONTH WITH HEDVIG", // todo format locale
            style = MaterialTheme.typography.overline,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
        )
    }
}

@Preview
@Composable
fun RetrievedInfoPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            RetrievedInfo(
                OfferModel.InsurelyCard.Retrieved.previewData()
            )
        }
    }
}
