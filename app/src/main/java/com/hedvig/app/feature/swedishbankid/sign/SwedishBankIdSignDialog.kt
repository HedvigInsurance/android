package com.hedvig.app.feature.swedishbankid.sign

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.flowWithLifecycle
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogSignBinding
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SwedishBankIdSignDialog : DialogFragment() {
    private val model: SwedishBankIdSignViewModel by viewModel {
        parametersOf(
            requireArguments().getString(AUTO_START_TOKEN)
                ?: throw IllegalArgumentException(
                    "Programmer error: Missing AUTO_START_TOKEN in ${this.javaClass.name}"
                )
        )
    }
    private val binding by viewBinding(DialogSignBinding::bind)
    private val marketManager: MarketManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.dialog_sign, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model
            .events
            .flowWithLifecycle(viewLifecycle)
            .onEach { event ->
                when (event) {
                    is SwedishBankIdSignViewModel.Event.StartBankID -> {
                        val bankIdUri = bankIdUri(event.autoStartToken)
                        if (requireActivity().canOpenUri(bankIdUri)) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW, bankIdUri
                                )
                            )
                        }
                    }
                    SwedishBankIdSignViewModel.Event.StartDirectDebit -> {
                        marketManager
                            .market
                            ?.connectPayin(requireContext(), isPostSign = true)
                            ?.let { startActivity(it) }
                    }
                }
            }
            .launchIn(viewLifecycleScope)
        model
            .viewState
            .flowWithLifecycle(viewLifecycle)
            .onEach { viewState ->
                dialog?.setCanceledOnTouchOutside(viewState is SwedishBankIdSignViewModel.ViewState.Error)
                binding.signStatus.setText(
                    when (viewState) {
                        is SwedishBankIdSignViewModel.ViewState.StartClient -> R.string.SIGN_START_BANKID
                        SwedishBankIdSignViewModel.ViewState.Cancelled -> R.string.SIGN_CANCELED
                        SwedishBankIdSignViewModel.ViewState.InProgress -> R.string.SIGN_IN_PROGRESS
                        SwedishBankIdSignViewModel.ViewState.Success -> R.string.SIGN_SUCCESSFUL
                        SwedishBankIdSignViewModel.ViewState.Error -> R.string.SIGN_FAILED_REASON_UNKNOWN
                    }
                )
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

    companion object {
        private fun bankIdUri(autoStartToken: String): Uri =
            Uri.parse("bankid:///?autostarttoken=$autoStartToken&redirect=null")

        private const val AUTO_START_TOKEN = "AUTO_START_TOKEN"
        const val TAG = "OfferSignDialog"
        fun newInstance(autoStartToken: String) = SwedishBankIdSignDialog().apply {
            arguments = bundleOf(
                AUTO_START_TOKEN to autoStartToken,
            )
        }
    }
}
