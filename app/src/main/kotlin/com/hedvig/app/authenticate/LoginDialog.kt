package com.hedvig.app.authenticate

import android.os.Bundle
import android.view.View
import com.hedvig.android.apollo.graphql.type.AuthState
import com.hedvig.app.feature.genericauth.GenericAuthActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginDialog : AuthenticateDialog() {
  private val viewModel: UserViewModel by viewModel()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.authStatus.observe(viewLifecycleOwner) { data ->
      data.authStatus?.status?.let(::bindNewStatus)
    }

    viewModel.autoStartToken.observe(viewLifecycleOwner) { data ->
      handleAutoStartToken(data.swedishBankIdAuth.autoStartToken)
    }

    viewModel.fetchBankIdStartToken()

    binding.login.setOnClickListener {
      requireActivity().startActivity(GenericAuthActivity.newInstance(requireActivity()))
    }
  }

  private fun bindNewStatus(state: AuthState) {
    when (state) {
      AuthState.INITIATED -> binding.authTitle.setText(hedvig.resources.R.string.BANK_ID_AUTH_TITLE_INITIATED)
      AuthState.IN_PROGRESS -> binding.authTitle.setText(hedvig.resources.R.string.BANK_ID_LOG_IN_TITLE_IN_PROGRESS)
      is AuthState.UNKNOWN__,
      AuthState.FAILED,
      -> {
        binding.authTitle.setText(hedvig.resources.R.string.BANK_ID_LOG_IN_TITLE_FAILED)
        dialog?.setCanceledOnTouchOutside(true)
      }
      AuthState.SUCCESS -> {
        binding.authTitle.setText(hedvig.resources.R.string.BANK_ID_LOG_IN_TITLE_SUCCESS)
        dismissAllowingStateLoss()
        startActivity(
          LoggedInActivity.newInstance(requireContext(), withoutHistory = true),
        )
      }
    }
  }

  companion object {
    const val TAG = "LoginDialog"
  }
}
