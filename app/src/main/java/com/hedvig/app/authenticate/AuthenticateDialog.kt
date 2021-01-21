package com.hedvig.app.authenticate

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.iid.FirebaseInstanceId
import com.hedvig.android.owldroid.type.AuthState
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogAuthenticateBinding
import com.hedvig.app.feature.chat.viewmodel.UserViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.QR
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class AuthenticateDialog : DialogFragment() {
    private val model: UserViewModel by viewModel()
    private val binding by viewBinding(DialogAuthenticateBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_authenticate, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model.autoStartToken.observe(viewLifecycleOwner) { data ->
            val autoStartToken = data.swedishBankIdAuth.autoStartToken
            val autoStartUrl = "bankid:///?autostarttoken=$autoStartToken"
            val bankIdUri = Uri.parse("$autoStartUrl&redirect=null")
            if (requireContext().canOpenUri(bankIdUri)) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        bankIdUri
                    )
                )
            } else {
                QR
                    .with(requireContext())
                    .load(autoStartUrl)
                    .into(binding.qrCode)
            }
        }
        model.authStatus.observe(viewLifecycleOwner) { data ->
            data.authStatus?.status?.let(::bindNewStatus)
        }
        model.fetchBankIdStartToken()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
        }

    private fun bindNewStatus(state: AuthState) = when (state) {
        AuthState.INITIATED -> {
            binding.authTitle.text = getString(R.string.BANK_ID_AUTH_TITLE_INITIATED)
        }
        AuthState.IN_PROGRESS -> {
            binding.authTitle.text = getString(R.string.BANK_ID_LOG_IN_TITLE_IN_PROGRESS)
        }
        AuthState.UNKNOWN__,
        AuthState.FAILED -> {
            binding.authTitle.text = getString(R.string.BANK_ID_LOG_IN_TITLE_FAILED)
            dialog?.setCanceledOnTouchOutside(true)
        }
        AuthState.SUCCESS -> {
            binding.authTitle.text = getString(R.string.BANK_ID_LOG_IN_TITLE_SUCCESS)
            requireContext().setIsLoggedIn(true)
            GlobalScope.launch(Dispatchers.IO) {
                runCatching { FirebaseInstanceId.getInstance().deleteInstanceId() }
            }
            dismiss()
            startActivity(
                Intent(this.context, LoggedInActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            )
        }
    }

    companion object {
        const val TAG = "AuthenticateDialog"
    }
}
