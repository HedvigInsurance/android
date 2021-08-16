package com.hedvig.app.feature.embark.passages.previousinsurer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.SecondaryTextButton
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedTextButton
import com.hedvig.app.ui.compose.composables.buttons.LargeOutlinedTextButton
import com.hedvig.app.ui.compose.theme.HedvigTheme

class AskForPriceInfoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HedvigTheme {
                Scaffold(
                    topBar = {
                        TopAppBarWithBack(
                            onClick = { onBackPressed() },
                            title = "Retrieve price info"
                        )
                    }
                ) {
                    AskForPriceContent()
                }
            }
        }
    }
}

@Preview
@Composable
fun AskForPriceContent() {
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
            text = "Do you want to retrieve price info from If?",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary
        )
        Text(
            text = "This will allow you to compare your current plan with Hedvig’s.",
            style = MaterialTheme.typography.body1
        )
        LargeContainedTextButton(
            modifier = Modifier.padding(top = baseMarginQuadruple),
            text = "Yes, retrieve info",
            onClick = { /*TODO*/ }
        )
        LargeOutlinedTextButton(
            text = "No, skip",
            onClick = { /*TODO*/ }
        )
        Text(
            text = "Genom att klicka på \"Ja, hämta info\" godkänner du att Hedvig hämtar data från Trygg Hansa " +
                "i enlighet med vår Privacy Policy. Detta kan komma att inkludera “känslig data” i enlighet med GDPR",
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center
        )
    }
}
