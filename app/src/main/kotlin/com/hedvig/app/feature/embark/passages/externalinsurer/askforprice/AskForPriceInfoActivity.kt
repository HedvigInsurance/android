package com.hedvig.app.feature.embark.passages.externalinsurer.askforprice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity.Companion.REFERENCE_RESULT
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity.Companion.SSN_RESULT
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AskForPriceInfoActivity : AppCompatActivity() {
  private val parameter by lazy {
    intent.getParcelableExtra<InsuranceProviderParameter>(PARAMETER)
      ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")
  }

  private val viewModel: AskForPriceInfoViewModel by viewModel {
    parametersOf(parameter.selectedInsuranceProviderCollectionId)
  }

  private val retrievePriceActivityResultLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
      if (result.resultCode == RESULT_CONTINUE) {
        finishWithResult(
          result.data?.getStringExtra(REFERENCE_RESULT),
          result.data?.getStringExtra(SSN_RESULT),
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
              title = stringResource(hedvig.resources.R.string.insurely_title),
            )
          },
        ) { paddingValues ->
          AskForPriceScreen(
            parameter.selectedInsuranceProviderName,
            onSkipRetrievePriceInfo = {
              viewModel.onSkipRetrievePriceInfo()
              finishWithResult(null, null)
            },
            onNavigateToRetrievePrice = ::startRetrievePriceActivity,
            modifier = Modifier.padding(paddingValues),
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
      parameter: InsuranceProviderParameter,
    ) = Intent(context, AskForPriceInfoActivity::class.java).apply {
      putExtra(PARAMETER, parameter)
    }
  }
}

@Composable
fun AskForPriceScreen(
  selectedInsurance: String,
  onSkipRetrievePriceInfo: () -> Unit,
  onNavigateToRetrievePrice: () -> Unit,
  modifier: Modifier = Modifier,
) {
  IntroContent(
    selectedInsurance = selectedInsurance,
    onNavigateToRetrievePriceInfo = { onNavigateToRetrievePrice() },
    onSkipRetrievePriceInfo = onSkipRetrievePriceInfo,
    modifier = modifier,
  )
}

@Preview(showBackground = true)
@Composable
fun AskForPriceScreenPreview() {
  HedvigTheme {
    AskForPriceScreen(
      "Test",
      onSkipRetrievePriceInfo = { },
      onNavigateToRetrievePrice = { },
    )
  }
}
