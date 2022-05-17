package com.hedvig.app.authenticate

import android.os.Bundle
import android.view.View
import com.hedvig.android.owldroid.type.AuthState
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.app.util.extensions.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginDialog : AuthenticateDialog() {

    private val model: UserViewModel by viewModel()
    private val pushTokenManager: PushTokenManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.authStatus.observe(viewLifecycleOwner) { data ->
            data.authStatus?.status?.let(::bindNewStatus)
        }

        model.autoStartToken.observe(viewLifecycleOwner) { data ->
            handleAutoStartToken(data.swedishBankIdAuth.autoStartToken)
        }

        model.fetchBankIdStartToken()
    }

    private fun bindNewStatus(state: AuthState): Any? = when (state) {
        AuthState.INITIATED -> binding.authTitle.setText(R.string.BANK_ID_AUTH_TITLE_INITIATED)
        AuthState.IN_PROGRESS -> binding.authTitle.setText(R.string.BANK_ID_LOG_IN_TITLE_IN_PROGRESS)
        AuthState.UNKNOWN__,
        AuthState.FAILED -> {
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
                        dismissAllowingStateLoss()
                        startActivity(
                            LoggedInActivity.newInstance(requireContext(), withoutHistory = true)
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "LoginDialog"
    }
}
