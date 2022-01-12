package com.hedvig.app.feature.embark.passages.externalinsurer.askforprice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity.Companion.REFERENCE_RESULT
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity.Companion.SSN_RESULT
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme

class AskForPriceInfoActivity : BaseActivity() {

    private val parameter by lazy {
        intent.getParcelableExtra<InsuranceProviderParameter>(PARAMETER)
            ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")
    }

    private val retrievePriceActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_CONTINUE) {
                finishWithResult(
                    result.data?.getStringExtra(REFERENCE_RESULT),
                    result.data?.getStringExtra(SSN_RESULT)
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HedvigTheme {
                Scaffold(
                    topBar = {
                        TopAppBarWithBack(
                            onClick = { onBackPressed() },
                            title = stringResource(R.string.insurely_title)
                        )
                    }
                ) {
                    AskForPriceScreen(
                        parameter.selectedInsuranceProviderName,
                        onSkipRetrievePriceInfo = { finishWithResult(null, null) },
                        onNavigateToRetrievePrice = ::startRetrievePriceActivity
                    )
                }
            }
        }
    }

    private fun startRetrievePriceActivity() {
        retrievePriceActivityResultLauncher.launch(RetrievePriceInfoActivity.createIntent(this, parameter))
    }

    private fun finishWithResult(reference: String?, ssn: String?) {
        val intent = Intent()
        intent.putExtra(REFERENCE_RESULT, reference)
        intent.putExtra(SSN_RESULT, ssn)
        setResult(RESULT_CONTINUE, intent)
        finish()
    }

    companion object {
        const val RESULT_CONTINUE = 1242
        private const val PARAMETER = "parameter"

        fun createIntent(
            context: Context,
            parameter: InsuranceProviderParameter
        ) = Intent(context, AskForPriceInfoActivity::class.java).apply {
            putExtra(PARAMETER, parameter)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
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

@Preview
@Composable
fun AskForPriceScreenPreview() {
    HedvigTheme {
        AskForPriceScreen(
            "Test",
            onSkipRetrievePriceInfo = { },
            onNavigateToRetrievePrice = { }
        )
    }
}
