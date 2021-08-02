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
import com.hedvig.android.owldroid.type.AuthState
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogAuthenticateBinding
import com.hedvig.app.feature.chat.viewmodel.UserViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.app.util.QR
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthenticateDialog : DialogFragment() {
    private val model: UserViewModel by viewModel()
    private val binding by viewBinding(DialogAuthenticateBinding::bind)
    private val pushTokenManager: PushTokenManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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

    private fun bindNewStatus(state: AuthState): Any? = when (state) {
        AuthState.INITIATED -> {
            binding.authTitle.setText(R.string.BANK_ID_AUTH_TITLE_INITIATED)
        }
        AuthState.IN_PROGRESS -> {
            binding.authTitle.setText(R.string.BANK_ID_LOG_IN_TITLE_IN_PROGRESS)
        }
        AuthState.UNKNOWN__,
        AuthState.FAILED,
        -> {
            binding.authTitle.setText(R.string.BANK_ID_LOG_IN_TITLE_FAILED)
            dialog?.setCanceledOnTouchOutside(true)
        }
        AuthState.SUCCESS -> {
            binding.authTitle.setText(R.string.BANK_ID_LOG_IN_TITLE_SUCCESS)
            model.onAuthSuccess()
            viewLifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    runCatching {
                        pushTokenManager.refreshToken()
                    }
                    withContext(Dispatchers.Main) {
                        dismiss()
                        startActivity(
                            LoggedInActivity.newInstance(requireContext(), withoutHistory = true)
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "AuthenticateDialog"
    }
}
