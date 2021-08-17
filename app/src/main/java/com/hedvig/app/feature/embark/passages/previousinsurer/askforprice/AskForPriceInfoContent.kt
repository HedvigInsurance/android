package com.hedvig.app.feature.embark.passages.previousinsurer.askforprice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.PrimaryTextButton
import com.hedvig.app.ui.compose.composables.SecondaryTextButton

@Composable
fun IntroContent(
    selectedInsurance: String,
    onNavigateToRetrievePriceInfo: () -> Unit,
    onSkipRetrievePriceInfo: () -> Unit,
) {
    val baseMargin = dimensionResource(R.dimen.base_margin)
    val baseMarginDouble = dimensionResource(R.dimen.base_margin_double)
    val baseMarginQuadruple = dimensionResource(R.dimen.base_margin_quadruple)

    Column(
        modifier = Modifier
            .padding(baseMarginDouble)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(baseMarginDouble)
    ) {
        Text(
            modifier = Modifier.padding(top = baseMargin),
            text = "Do you want to retrieve price info from $selectedInsurance?",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary
        )
        Text(
            text = "This will allow you to compare your current plan with Hedvig’s.",
            style = MaterialTheme.typography.body1
        )
        PrimaryTextButton(
            modifier = Modifier.padding(top = baseMarginQuadruple),
            text = "Yes, retrieve info",
            onClick = onNavigateToRetrievePriceInfo
        )
        SecondaryTextButton(
            text = "No, skip",
            onClick = onSkipRetrievePriceInfo
        )
        Text(
            text = "Genom att klicka på \"Ja, hämta info\" godkänner du att Hedvig hämtar data från Trygg Hansa " +
                "i enlighet med vår Privacy Policy. Detta kan komma att inkludera “känslig data” i enlighet med GDPR",
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center
        )
    }
}
