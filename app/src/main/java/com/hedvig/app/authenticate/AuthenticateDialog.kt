package com.hedvig.app.authenticate

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.firebase.iid.FirebaseInstanceId
import com.hedvig.android.owldroid.type.AuthState
import com.hedvig.app.LoggedInActivity
import com.hedvig.app.R
import com.hedvig.app.feature.chat.UserViewModel
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setIsLoggedIn
import kotlinx.android.synthetic.main.dialog_authenticate.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class AuthenticateDialog : DialogFragment() {

    private val userViewModel: UserViewModel by viewModel()
    private val tracker: AuthTracker by inject()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_authenticate, null)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setContentView(view)

        userViewModel.autoStartToken.observe(lifecycleOwner = this) { data ->
            data?.bankIdAuth?.autoStartToken?.let { autoStartToken ->
                val bankIdUri = Uri.parse("bankid:///?autostarttoken=$autoStartToken&redirect=null")
                if (requireContext().canOpenUri(bankIdUri)) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            bankIdUri
                        )
                    )
                } else {
                    dialog.authTitle.text = getString(R.string.BANK_ID_NOT_INSTALLED)
                }
            }
        }
        userViewModel.authStatus.observe(lifecycleOwner = this) { data ->
            data?.authStatus?.status?.let { bindNewStatus(it) }
        }
        userViewModel.fetchBankIdStartToken()

        return dialog
    }

    private fun bindNewStatus(state: AuthState) = when (state) {
        AuthState.INITIATED -> {
            dialog.authTitle.text = getString(R.string.BANK_ID_AUTH_TITLE_INITIATED)
        }
        AuthState.IN_PROGRESS -> {
            dialog.authTitle.text = getString(R.string.BANK_ID_LOG_IN_TITLE_IN_PROGRESS)
        }
        AuthState.`$UNKNOWN`,
        AuthState.FAILED -> {
            dialog.authTitle.text = getString(R.string.BANK_ID_LOG_IN_TITLE_FAILED)
        }
        AuthState.SUCCESS -> {
            dialog.authTitle.text = getString(R.string.BANK_ID_LOG_IN_TITLE_SUCCESS)
            tracker.login()
            requireContext().setIsLoggedIn(true)
            FirebaseInstanceId.getInstance().deleteInstanceId()
            dismiss()
            startActivity(Intent(this.context, LoggedInActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }
    }

    companion object {
        const val TAG = "AuthenticateDialog"
    }
}
