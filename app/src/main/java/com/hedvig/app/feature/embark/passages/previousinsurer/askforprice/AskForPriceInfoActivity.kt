package com.hedvig.app.feature.embark.passages.previousinsurer.askforprice

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.PrimaryTextButton
import com.hedvig.app.ui.compose.composables.SecondaryTextButton
import com.hedvig.app.ui.compose.composables.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AskForPriceInfoActivity : ComponentActivity() {

    private val parameter by lazy {
        intent.getParcelableExtra<AskForPriceInfoParameter>(PARAMETER)
            ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")
    }

    private val viewModel: AskForPriceViewModel by viewModel {
        parametersOf(parameter)
    }

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
                    AskForPriceContent(viewModel)
                }
            }
        }
    }

    companion object {
        private const val PARAMETER = "parameter"

        fun createIntent(
            context: Context,
            parameter: AskForPriceInfoParameter
        ) = Intent(context, AskForPriceInfoActivity::class.java).apply {
            putExtra(PARAMETER, parameter)
        }
    }
}

@Preview
@Composable
fun AskForPriceContent(viewModel: AskForPriceViewModel = viewModel()) {
    val baseMargin = dimensionResource(R.dimen.base_margin)
    val baseMarginDouble = dimensionResource(R.dimen.base_margin_double)
    val baseMarginQuadruple = dimensionResource(R.dimen.base_margin_quadruple)

    val selectedInsurance: String by viewModel.selectedInsurance.collectAsState("")

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
            onClick = { /*TODO*/ }
        )
        SecondaryTextButton(
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
