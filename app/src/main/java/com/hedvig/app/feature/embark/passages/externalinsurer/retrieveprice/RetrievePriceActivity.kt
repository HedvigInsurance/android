package com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.authenticate.insurely.InsurelyDialog
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.InsuranceProviderParameter
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.ui.compose.composables.CenteredProgressIndicator
import com.hedvig.app.ui.compose.composables.ErrorDialog
import com.hedvig.app.ui.compose.composables.FadeWhen
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme
import d
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class RetrievePriceInfoActivity : BaseActivity() {

    private val parameter by lazy {
        intent.getParcelableExtra(PARAMETER)
            ?: InsuranceProviderParameter("Test")
            ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")
    }

    private val viewModel: RetrievePriceViewModel by viewModel { parametersOf(parameter) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.setFragmentResultListener(
            InsurelyDialog.REQUEST_KEY,
            this,
            { _, result ->
                val success = result.getBoolean(InsurelyDialog.RESULT_KEY)
                if (success) {
                    // Show collection started view
                    d { "auth: SUCCESS" }
                } else {
                    // No-op
                    d { "auth: FAILURE" }
                }
            }
        )

        setContent {
            HedvigTheme {
                Scaffold(
                    topBar = {
                        TopAppBarWithBack(
                            onClick = ::onBackPressed,
                            title = stringResource(R.string.insurely_title)
                        )
                    }
                ) {
                    RetrievePriceScreen(
                        viewModel = viewModel,
                        fragmentManager = supportFragmentManager
                    )
                }
            }
        }
    }

    companion object {
        private const val PARAMETER = "parameter"

        fun createIntent(context: Context, parameter: InsuranceProviderParameter) =
            Intent(context, RetrievePriceInfoActivity::class.java).apply {
                putExtra(PARAMETER, parameter)
            }
    }
}

@Composable
fun RetrievePriceScreen(
    viewModel: RetrievePriceViewModel = viewModel(),
    fragmentManager: FragmentManager
) {
    val viewState by viewModel.viewState.collectAsState()

    viewState.authInformation?.let { authInformation ->
        InsurelyDialog.newInstance(authInformation.reference)
            .show(fragmentManager, AuthenticateDialog.TAG)
    }

    viewState.error?.getStringResource()?.let {
        ErrorDialog(onDismiss = viewModel::onDismissError, message = stringResource(id = it))
    }

    FadeWhen(visible = viewState.isLoading) {
        CenteredProgressIndicator()
    }

    FadeWhen(visible = !viewState.isLoading) {
        RetrievePriceContent(
            onRetrievePriceInfo = viewModel::onRetrievePriceInfo,
            onIdentityInput = { viewModel.onIdentityInput(it) },
            input = viewState.input,
            title = viewState.market?.titleRes()?.let { stringResource(it) } ?: "",
            placeholder = viewState.market?.placeHolderRes()?.let { stringResource(it) } ?: "",
            label = viewState.market?.labelRes()?.let { stringResource(it) } ?: "",
            inputErrorMessage = viewState.inputError?.errorTextKey?.let { stringResource(it) },
        )
    }
}

private fun Market.titleRes() = when (this) {
    Market.SE -> R.string.insurely_se_ssn_title
    Market.NO -> R.string.insurely_no_ssn_title
    Market.FR,
    Market.DK -> throw IllegalArgumentException("No string resource for $this")
}

private fun Market.placeHolderRes() = when (this) {
    Market.SE -> R.string.insurely_se_ssn_assistive_text
    Market.NO -> R.string.insurely_no_ssn_assistive_text
    Market.FR,
    Market.DK -> throw IllegalArgumentException("No string resource for $this")
}

private fun Market.labelRes() = when (this) {
    Market.SE -> R.string.insurely_se_ssn_input_label
    Market.NO -> R.string.insurely_no_ssn_input_label
    Market.FR,
    Market.DK -> throw IllegalArgumentException("No string resource for $this")
}

private fun DataCollectionResult.Error.getStringResource() = when (this) {
    is DataCollectionResult.Error.NetworkError -> R.string.NETWORK_ERROR_ALERT_MESSAGE
    DataCollectionResult.Error.NoData -> R.string.general_unknown_error
    DataCollectionResult.Error.QueryError -> R.string.insurely_failure_title
}
