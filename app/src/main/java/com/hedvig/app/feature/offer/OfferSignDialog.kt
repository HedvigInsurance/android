package com.hedvig.app.feature.offer

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.type.BankIdStatus
import com.hedvig.android.owldroid.type.SignState
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.payment.TrustlyActivity
import com.hedvig.app.service.LoginStatusService.Companion.IS_VIEWING_OFFER
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.storeBoolean
import kotlinx.android.synthetic.main.dialog_sign.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OfferSignDialog : DialogFragment() {
    private val offerViewModel: OfferViewModel by sharedViewModel()
    private val tracker: OfferTracker by inject()

    val handler = Handler()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sign, null)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(false)

        offerViewModel.clearPreviousErrors()
        offerViewModel.signError.observe(lifecycleOwner = this) { err ->
            if (err == true) {
                dialog.signStatus?.text = getString(R.string.SIGN_FAILED_REASON_UNKNOWN)
                dialog.setCanceledOnTouchOutside(true)
            } else if (err == false) {
                dialog.signStatus.text = getString(R.string.SIGN_START_BANKID)
                dialog.setCanceledOnTouchOutside(false)
            }
        }
        offerViewModel.autoStartToken.observe(lifecycleOwner = this) { data ->
            data?.signOfferV2?.autoStartToken?.let { autoStartToken -> startBankId(autoStartToken) }
        }
        offerViewModel.signStatus.observe(lifecycleOwner = this) { data ->
            data?.let { d ->
                bindStatus(d)
            }
        }
        offerViewModel.startSign()

        return dialog
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
            dialog?.signStatus?.text = getString(R.string.SIGN_START_BANKID)
        }
    }

    private fun bindStatus(d: SignStatusFragment) {
        when (d.collectStatus?.status) {
            BankIdStatus.PENDING -> {
                when (d.collectStatus.code) {
                    "noClient" -> {
                        dialog?.signStatus?.text = getString(R.string.SIGN_START_BANKID)
                    }
                    "unknown", "userSign" -> {
                        dialog?.signStatus?.text = getString(R.string.SIGN_IN_PROGRESS)
                    }
                }
            }
            BankIdStatus.FAILED -> {
                when (d.collectStatus.code) {
                    "userCancel", "cancelled" -> {
                        dialog?.signStatus?.text = getString(R.string.SIGN_CANCELED)
                    }
                    else -> {
                        dialog?.signStatus?.text = getString(R.string.SIGN_FAILED_REASON_UNKNOWN)
                    }
                }
                dialog?.setCanceledOnTouchOutside(true)
            }
            BankIdStatus.COMPLETE -> {
                when (d.signState) {
                    SignState.IN_PROGRESS, SignState.INITIATED -> {
                    }
                    SignState.COMPLETED -> {
                        dialog?.signStatus?.text = getString(R.string.SIGN_SUCCESSFUL)
                        tracker.userDidSign(
                            offerViewModel.data.value?.insurance?.cost?.fragments?.costFragment?.monthlyNet?.amount?.toBigDecimal()?.toDouble()
                                ?: 0.0
                        )
                        goToDirectDebit()
                    }
                    else -> {
                        dialog?.signStatus?.text = getString(R.string.SIGN_FAILED_REASON_UNKNOWN)
                    }
                }
            }
            else -> {
            }
        }
    }

    private fun goToDirectDebit() {
        requireContext().storeBoolean(IS_VIEWING_OFFER, false)
        handler.postDelayed({
            startActivity(TrustlyActivity.newInstance(requireContext(), withExplainer = true, withoutHistory = true))
        }, 1000)
    }

    override fun onPause() {
        handler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        offerViewModel.manuallyRecheckSignStatus()
    }

    companion object {
        const val TAG = "OfferSignDialog"
        fun newInstance() = OfferSignDialog()
    }
}
