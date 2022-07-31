package com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.market.Market
import com.hedvig.app.BaseActivity
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.authenticate.insurely.InsurelyDialog
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.AskForPriceInfoActivity
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.InsuranceProviderParameter
import com.hedvig.app.ui.compose.composables.CenteredProgressIndicator
import com.hedvig.app.ui.compose.composables.FadeWhen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class RetrievePriceInfoActivity : BaseActivity() {

  private val parameter by lazy {
    intent.getParcelableExtra<InsuranceProviderParameter>(PARAMETER)
      ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")
  }

  private val viewModel: RetrievePriceViewModel by viewModel { parametersOf(parameter) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    supportFragmentManager.setFragmentResultListener(
      InsurelyDialog.REQUEST_KEY,
      this,
    ) { _, result ->
      val success = result.getBoolean(InsurelyDialog.RESULT_KEY)
      val reference = result.getString(InsurelyDialog.RESULT_REFERENCE) ?: ""
      if (success) {
        viewModel.onCollectionStarted(reference)
      } else {
        viewModel.onCollectionFailed()
      }
    }

    viewModel.events
      .flowWithLifecycle(lifecycle)
      .onEach { event ->
        when (event) {
          is RetrievePriceViewModel.Event.AuthInformation -> {
            InsurelyDialog
              .newInstance(event.reference)
              .show(supportFragmentManager, AuthenticateDialog.TAG)
          }
        }
      }
      .launchIn(lifecycleScope)

    setContent {
      HedvigTheme {
        Scaffold(
          topBar = {
            TopAppBarWithBack(
              onClick = ::onBackPressed,
              title = stringResource(hedvig.resources.R.string.insurely_title),
            )
          },
        ) { paddingValues ->
          RetrievePriceScreen(
            modifier = Modifier.padding(paddingValues),
            viewModel = viewModel,
            onContinue = ::onContinue,
          )
        }
      }
    }
  }

  private fun onContinue(reference: String?, input: String?) {
    val intent = Intent()
    intent.putExtra(REFERENCE_RESULT, reference)
    intent.putExtra(SSN_RESULT, input)
    setResult(AskForPriceInfoActivity.RESULT_CONTINUE, intent)
    finish()
  }

  companion object {
    const val REFERENCE_RESULT = "reference_result"
    const val SSN_RESULT = "ssn_result"
    private const val PARAMETER = "parameter"

    fun createIntent(context: Context, parameter: InsuranceProviderParameter) =
      Intent(context, RetrievePriceInfoActivity::class.java).apply {
        putExtra(PARAMETER, parameter)
      }
  }
}

@Composable
fun RetrievePriceScreen(
  onContinue: (referenceResult: String?, ssn: String?) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: RetrievePriceViewModel = viewModel(),
) {
  val viewState by viewModel.viewState.collectAsState()

  when {
    viewState.collectionStarted != null -> RetrievePriceSuccess(
      onContinue = { onContinue(viewState.collectionStarted!!.reference, viewState.input) },
    )
    viewState.collectionFailed != null -> RetrievePriceFailed(
      onRetry = viewModel::onRetry,
      onSkip = { onContinue(null, null) },
      viewState.collectionFailed!!.insurerName,
    )
    else -> {
      FadeWhen(visible = viewState.isLoading) {
        CenteredProgressIndicator(
          modifier = modifier,
        )
      }

      FadeWhen(visible = !viewState.isLoading) {
        RetrievePriceContent(
          onRetrievePriceInfo = viewModel::onRetrievePriceInfo,
          onIdentityInput = viewModel::onIdentityInput,
          onDismissError = viewModel::onDismissError,
          input = viewState.input,
          title = viewState.market?.titleRes()?.let { stringResource(it) } ?: "",
          placeholder = viewState.market?.placeHolderRes()?.let { stringResource(it) } ?: "",
          label = viewState.market?.labelRes()?.let { stringResource(it) } ?: "",
          inputErrorMessage = viewState.inputError?.errorTextKey?.let { stringResource(it) },
          errorMessage = viewState.inputError?.errorTextKey?.let { stringResource(it) },
          modifier = modifier,
        )
      }
    }
  }
}

private fun Market.titleRes() = when (this) {
  Market.SE -> hedvig.resources.R.string.insurely_se_ssn_title
  Market.NO -> hedvig.resources.R.string.insurely_no_ssn_title
  Market.FR,
  Market.DK,
  -> throw IllegalArgumentException("No string resource for $this")
}

private fun Market.placeHolderRes() = when (this) {
  Market.SE -> hedvig.resources.R.string.insurely_se_ssn_assistive_text
  Market.NO -> hedvig.resources.R.string.insurely_no_ssn_assistive_text
  Market.FR,
  Market.DK,
  -> throw IllegalArgumentException("No string resource for $this")
}

private fun Market.labelRes() = when (this) {
  Market.SE -> hedvig.resources.R.string.insurely_se_ssn_input_label
  Market.NO -> hedvig.resources.R.string.insurely_no_ssn_input_label
  Market.FR,
  Market.DK,
  -> throw IllegalArgumentException("No string resource for $this")
}
