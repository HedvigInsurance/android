package com.hedvig.app.feature.embark.passages.externalinsurer.askforprice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedTextButton
import com.hedvig.app.ui.compose.composables.buttons.LargeOutlinedTextButton

@Composable
fun IntroContent(
    selectedInsurance: String,
    onNavigateToRetrievePriceInfo: () -> Unit,
    onSkipRetrievePriceInfo: () -> Unit,
) {
    val baseMargin = dimensionResource(R.dimen.base_margin)
    val baseMarginDouble = dimensionResource(R.dimen.base_margin_double)

    Column(
        modifier = Modifier
            .padding(baseMarginDouble)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(baseMarginDouble)) {
            Text(
                modifier = Modifier.padding(top = baseMargin),
                text = stringResource(R.string.insurely_intro_title, selectedInsurance),
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )
            Text(
                text = stringResource(R.string.insurely_intro_description),
                style = MaterialTheme.typography.body1
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(baseMarginDouble)) {
            LargeContainedTextButton(
                text = stringResource(R.string.insurely_intro_continue_button_text),
                onClick = onNavigateToRetrievePriceInfo
            )
            LargeOutlinedTextButton(
                text = stringResource(R.string.insurely_intro_skip_button_text),
                onClick = onSkipRetrievePriceInfo
            )
            Text(
                text = stringResource(R.string.insurely_intro_footer, selectedInsurance),
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.Center
            )
        }
    }
}
