package com.hedvig.app.authenticate

import android.os.Bundle
import android.view.View
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.auth.LoginStatusResult
import com.hedvig.app.feature.genericauth.GenericAuthActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import hedvig.resources.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginDialog : AuthenticateDialog() {
  private val viewModel: UserViewModel by viewModel()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.authTitle.setText(R.string.BANK_ID_AUTH_TITLE_INITIATED)

    viewModel.viewState
      .flowWithLifecycle(lifecycle)
      .onEach(::bindViewState)
      .launchIn(lifecycleScope)

    viewModel.fetchBankIdStartToken()

    binding.login.setOnClickListener {
      requireActivity().startActivity(GenericAuthActivity.newInstance(requireActivity()))
    }
  }

  private fun bindViewState(viewState: UserViewModel.ViewState) {
    viewState.authStatus?.let(::bindNewStatus)
    viewState.autoStartToken?.let(::handleAutoStartToken)
    if (viewState.navigateToLoggedIn) {
      startLoggedInActivity()
    }
  }

  private fun bindNewStatus(state: LoginStatusResult) {
    when (state) {
      is LoginStatusResult.Pending -> binding.authTitle.text = state.statusMessage
      is LoginStatusResult.Failed -> {
        binding.authTitle.text = state.message
        dialog?.setCanceledOnTouchOutside(true)
      }
      is LoginStatusResult.Completed -> {
        binding.authTitle.setText(R.string.BANK_ID_LOG_IN_TITLE_SUCCESS)
      }
    }
  }

  private fun startLoggedInActivity() {
    dismissAllowingStateLoss()
    val loggedInActivity = LoggedInActivity.newInstance(requireContext(), withoutHistory = true)
    startActivity(loggedInActivity)
  }

  companion object {
    const val TAG = "LoginDialog"
  }
}
