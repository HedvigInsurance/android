package com.hedvig.app.feature.embark.passages.previousinsurer.askforprice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.app.ui.compose.composables.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme

class AskForPriceInfoActivity : ComponentActivity() {

    private val parameter by lazy {
        intent.getParcelableExtra<AskForPriceInfoParameter>(PARAMETER)
            ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")
    }

    @ExperimentalAnimationApi
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
                    AskForPriceScreen(
                        parameter.selectedInsuranceProvider,
                        onSkipRetrievePriceInfo = ::setResult,
                        onNavigateToRetrievePrice = ::startRetrievePriceActivity
                    )
                }
            }
        }
    }

    private fun startRetrievePriceActivity() {
        // startActivity(RetrievePriceInfoActivity.createIntent(this))
    }

    private fun setResult() {
        setResult(Activity.RESULT_CANCELED)
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

@ExperimentalAnimationApi
@Preview
@Composable
fun AskForPriceScreenPreview() {
    AskForPriceScreen(
        "Test",
        onSkipRetrievePriceInfo = { },
        onNavigateToRetrievePrice = { }
    )
}

@ExperimentalAnimationApi
@Composable
fun AskForPriceScreen(
    selectedInsurance: String,
    onSkipRetrievePriceInfo: () -> Unit,
    onNavigateToRetrievePrice: () -> Unit
) {
    IntroContent(
        selectedInsurance = selectedInsurance,
        onNavigateToRetrievePriceInfo = { onNavigateToRetrievePrice() },
        onSkipRetrievePriceInfo = onSkipRetrievePriceInfo
    )
}
