package com.hedvig.app.authenticate

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hedvig.app.databinding.DialogAuthenticateBinding
import com.hedvig.app.feature.genericauth.GenericAuthActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.QR
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.authlib.LoginStatusResult
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import hedvig.resources.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import slimber.log.i

class BankIdLoginDialog : DialogFragment(com.hedvig.app.R.layout.dialog_authenticate) {

  val binding by viewBinding(DialogAuthenticateBinding::bind)
  private val viewModel: BankIdLoginViewModel by viewModel()

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return super.onCreateDialog(savedInstanceState).apply {
      window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      setCanceledOnTouchOutside(false)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    i { "BankIdLoginDialog started" }
    binding.authTitle.setText(R.string.BANK_ID_AUTH_TITLE_INITIATED)

    binding.login.setOnClickListener {
      i { "Login with OTP clicked" }
      requireActivity().startActivity(GenericAuthActivity.newInstance(requireActivity()))
    }
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.viewState.collect {
          bindViewState(it)
        }
      }
    }
  }

  private fun bindViewState(viewState: BankIdLoginViewState) {
    i { "BankIdLoginDialog viewState updated to:$viewState" }
    when (viewState) {
      is BankIdLoginViewState.Error -> {
        binding.authTitle.text = viewState.message
        dialog?.setCanceledOnTouchOutside(true)
      }
      BankIdLoginViewState.Loading -> {}
      is BankIdLoginViewState.HandlingBankId -> {
        if (!viewState.processedAutoStartToken) {
          openBankIdOrShowQrCode(viewState.autoStartToken)
          viewModel.didProcessAutoStartToken()
        }
        when (val authStatus = viewState.authStatus) {
          is LoginStatusResult.Pending -> binding.authTitle.text = authStatus.statusMessage
          is LoginStatusResult.Failed -> {
            binding.authTitle.text = authStatus.message
            dialog?.setCanceledOnTouchOutside(true)
          }
          is LoginStatusResult.Completed -> {
            binding.authTitle.setText(R.string.BANK_ID_LOG_IN_TITLE_SUCCESS)
            if (!viewState.processedNavigationToLoggedIn) {
              viewModel.didNavigateToLoginScreen()
              startLoggedInActivity()
            }
          }
          is LoginStatusResult.Exception -> {
            binding.authTitle.text = authStatus.message
            dialog?.setCanceledOnTouchOutside(true)
          }
        }
      }
    }
  }

  private fun openBankIdOrShowQrCode(autoStartToken: String) {
    val autoStartUrl = "bankid:///?autostarttoken=$autoStartToken"
    val bankIdUri = Uri.parse("$autoStartUrl&redirect=null")
    if (requireContext().canOpenUri(bankIdUri)) {
      startActivity(
        Intent(
          Intent.ACTION_VIEW,
          bankIdUri,
        ),
      )
      i { "Openned BankID to handle login" }
    } else {
      QR
        .with(requireContext())
        .load(autoStartUrl)
        .into(binding.qrCode)
      i { "BankID not found, showing QR code instead" }
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
