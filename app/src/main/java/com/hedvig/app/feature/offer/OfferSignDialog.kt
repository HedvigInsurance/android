package com.hedvig.app.feature.offer

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.type.BankIdStatus
import com.hedvig.android.owldroid.type.SignState
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogSignBinding
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.canOpenUri
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OfferSignDialog : DialogFragment() {
    private val model: OfferViewModel by sharedViewModel()
    private val binding by viewBinding(DialogSignBinding::bind)
    private val marketManager: MarketManager by inject()

    val handler = Handler(getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.dialog_sign, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            model.clearPreviousErrors()
            model.signError.observe(viewLifecycleOwner) { err ->
                if (err == true) {
                    signStatus.setText(R.string.SIGN_FAILED_REASON_UNKNOWN)
                    dialog?.setCanceledOnTouchOutside(true)
                } else if (err == false) {
                    signStatus.setText(R.string.SIGN_START_BANKID)
                    dialog?.setCanceledOnTouchOutside(false)
                }
            }
            model.autoStartToken.observe(viewLifecycleOwner) { data ->
                data.signOfferV2.autoStartToken?.let { autoStartToken -> startBankId(autoStartToken) }
            }
            model.signStatus.observe(viewLifecycleOwner) { data ->
                bindStatus(data)
            }
            model.startSign()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
        }

    private fun startBankId(autoStartToken: String) {
        val bankIdUri = Uri.parse("bankid:///?autostarttoken=$autoStartToken&redirect=null")
        if (requireActivity().canOpenUri(bankIdUri)) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, bankIdUri
                )
            )
        } else {
            binding.signStatus.setText(R.string.SIGN_START_BANKID)
        }
    }

    private fun bindStatus(d: SignStatusFragment) {
        when (d.collectStatus?.status) {
            BankIdStatus.PENDING -> {
                when (d.collectStatus!!.code) {
                    "noClient" -> {
                        binding.signStatus.setText(R.string.SIGN_START_BANKID)
                    }
                    "unknown", "userSign" -> {
                        binding.signStatus.setText(R.string.SIGN_IN_PROGRESS)
                    }
                }
            }
            BankIdStatus.FAILED -> {
                when (d.collectStatus!!.code) {
                    "userCancel", "cancelled" -> {
                        binding.signStatus.setText(R.string.SIGN_CANCELED)
                    }
                    else -> {
                        binding.signStatus.setText(R.string.SIGN_FAILED_REASON_UNKNOWN)
                    }
                }
                dialog?.setCanceledOnTouchOutside(true)
            }
            BankIdStatus.COMPLETE -> {
                when (d.signState) {
                    SignState.IN_PROGRESS, SignState.INITIATED -> {
                    }
                    SignState.COMPLETED -> {
                        binding.signStatus.setText(R.string.SIGN_SUCCESSFUL)
                        goToDirectDebit()
                    }
                    else -> {
                        binding.signStatus.setText(R.string.SIGN_FAILED_REASON_UNKNOWN)
                    }
                }
            }
            else -> {
            }
        }
    }

    private fun goToDirectDebit() {
        model.onGoToDirectDebit()

        val market = marketManager.market
        if (market == null) {
            startActivity(MarketingActivity.newInstance(requireContext()))
        }

        handler.postDelayed(
            {
                market?.connectPayin(requireContext(), isPostSign = true)
                    ?.let { requireContext().startActivity(it) }
            },
            1000
        )
    }

    override fun onPause() {
        handler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        model.manuallyRecheckSignStatus()
    }

    companion object {
        const val TAG = "OfferSignDialog"
        fun newInstance() = OfferSignDialog()
    }
}
