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
import com.hedvig.android.owldroid.graphql.SignStatusSubscription
import com.hedvig.android.owldroid.type.BankIdStatus
import com.hedvig.android.owldroid.type.SignState
import com.hedvig.app.LoggedInActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.observe
import kotlinx.android.synthetic.main.dialog_sign.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class OfferSignDialog : DialogFragment() {
    private val offerViewModel: OfferViewModel by sharedViewModel()

    val handler = Handler()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sign, null)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(false)

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
            dialog.signStatus.text = getString(R.string.SIGN_START_BANKID)
        }
    }

    private fun bindStatus(d: SignStatusSubscription.Data) {
        when (d.signStatus?.status?.collectStatus?.status) {
            BankIdStatus.PENDING -> {
                when (d.signStatus?.status?.collectStatus?.code) {
                    "noClient" -> {
                        dialog.signStatus.text = getString(R.string.SIGN_START_BANKID)
                    }
                    "unknown", "userSign" -> {
                        dialog.signStatus.text = getString(R.string.SIGN_IN_PROGRESS)
                    }
                }
            }
            BankIdStatus.FAILED -> {
                when (d.signStatus?.status?.collectStatus?.code) {
                    "userCancel", "cancelled" -> {
                        dialog.signStatus.text = getString(R.string.SIGN_CANCELED)
                    }
                    else -> {
                        dialog.signStatus.text = getString(R.string.SIGN_FAILED_REASON_UNKNOWN)
                    }
                }
                dialog.setCanceledOnTouchOutside(true)
            }
            BankIdStatus.COMPLETE -> {
                when (d.signStatus?.status?.signState) {
                    SignState.IN_PROGRESS, SignState.INITIATED -> {
                    }
                    SignState.COMPLETED -> {
                        dialog.signStatus.text = getString(R.string.SIGN_SUCCESSFUL)
                        goToOffer()
                    }
                    else -> {
                        dialog.signStatus.text = getString(R.string.SIGN_FAILED_REASON_UNKNOWN)
                    }
                }
                if (d.signStatus?.status?.signState == SignState.COMPLETED) {
                }
            }
            else -> {
            }
        }
    }

    private fun goToOffer() {
        handler.postDelayed({
            startActivity(Intent(requireContext(), LoggedInActivity::class.java).apply {
                putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }, 1000)
    }

    override fun onPause() {
        handler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    companion object {
        const val TAG = "OfferSignDialog"
        fun newInstance() = OfferSignDialog()
    }
}
