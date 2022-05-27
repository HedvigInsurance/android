package com.hedvig.app.feature.swedishbankid.sign

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.flowWithLifecycle
import com.hedvig.app.R
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SwedishBankIdSignDialog : DialogFragment() {
    private val model: SwedishBankIdSignViewModel by viewModel {
        parametersOf(
            requireArguments().getParcelable(QUOTE_CART_ID)
        )
    }
    private val marketManager: MarketManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))
        setContent {
            val viewState by model.viewState.collectAsState()
            HedvigTheme {
                SwedishBankIdSignDialog(
                    text = stringResource(getTextByViewState(viewState))
                )
            }
        }
    }

    private fun getTextByViewState(viewState: SwedishBankIdSignViewModel.ViewState) = when (viewState) {
        is SwedishBankIdSignViewModel.ViewState.StartClient -> R.string.SIGN_START_BANKID
        SwedishBankIdSignViewModel.ViewState.Cancelled -> R.string.SIGN_CANCELED
        SwedishBankIdSignViewModel.ViewState.InProgress -> R.string.SIGN_IN_PROGRESS
        SwedishBankIdSignViewModel.ViewState.Success -> R.string.SIGN_SUCCESSFUL
        SwedishBankIdSignViewModel.ViewState.Error -> R.string.SIGN_FAILED_REASON_UNKNOWN
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model
            .events
            .flowWithLifecycle(viewLifecycle)
            .onEach { event ->
                when (event) {
                    SwedishBankIdSignViewModel.Event.StartBankID -> {
                        val bankIdUri = bankIdUri()
                        if (requireActivity().canOpenUri(bankIdUri)) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW, bankIdUri
                                )
                            )
                        }
                    }
                    is SwedishBankIdSignViewModel.Event.StartDirectDebit -> {
                        val market = marketManager.market ?: return@onEach
                        startActivity(
                            connectPayinIntent(
                                requireContext(),
                                event.payinType,
                                market,
                                true,
                            )
                        )
                    }
                }
            }
            .launchIn(viewLifecycleScope)
        model
            .viewState
            .flowWithLifecycle(viewLifecycle)
            .onEach { viewState ->
                dialog?.setCanceledOnTouchOutside(viewState is SwedishBankIdSignViewModel.ViewState.Error)
            }
            .launchIn(viewLifecycleScope)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
        }

    override fun onResume() {
        super.onResume()
        model.manuallyRecheckSignStatus()
    }

    override fun onPause() {
        super.onPause()
        model.cancelSignStatusPolling()
    }

    companion object {
        private fun bankIdUri() = Uri.parse("bankid:///?redirect=hedvig://")

        private const val QUOTE_CART_ID = "QUOTE_CART_ID"
        const val TAG = "OfferSignDialog"
        fun newInstance(
            quoteCartId: QuoteCartId?
        ) =
            SwedishBankIdSignDialog().apply {
                arguments = bundleOf(
                    QUOTE_CART_ID to quoteCartId
                )
            }
    }
}

@Composable
fun SwedishBankIdSignDialog(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.animateContentSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.subtitle1,
            )
            Image(
                painter = painterResource(id = R.drawable.ic_bank_id),
                contentDescription = null,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
@Preview(
    name = "Swedish BankID Sign-Dialog",
    group = "Offer Screen",
)
fun Preview() {
    HedvigTheme {
        SwedishBankIdSignDialog(text = stringResource(id = R.string.SIGN_IN_PROGRESS))
    }
}
