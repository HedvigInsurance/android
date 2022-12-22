package com.hedvig.app.authenticate

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.databinding.DialogAuthenticateBinding
import com.hedvig.app.feature.genericauth.GenericAuthActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.QR
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.authlib.LoginStatusResult
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import hedvig.resources.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class BankIdLoginDialog : DialogFragment(com.hedvig.app.R.layout.dialog_authenticate) {

  val binding by viewBinding(DialogAuthenticateBinding::bind)
  private val viewModel: UserViewModel by viewModel()

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return super.onCreateDialog(savedInstanceState).apply {
      window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      setCanceledOnTouchOutside(false)
    }
  }

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

  private fun handleAutoStartToken(autoStartToken: String) {
    val autoStartUrl = "bankid:///?autostarttoken=$autoStartToken"
    val bankIdUri = Uri.parse("$autoStartUrl&redirect=null")
    if (requireContext().canOpenUri(bankIdUri)) {
      startActivity(
        Intent(
          Intent.ACTION_VIEW,
          bankIdUri,
        ),
      )
    } else {
      QR
        .with(requireContext())
        .load(autoStartUrl)
        .into(binding.qrCode)
    }
  }

  fun redirect() {
    val bankIdUri = Uri.parse("bankid://?redirectUrl=hedvig://")
    if (requireContext().canOpenUri(bankIdUri)) {
      val intent = Intent(Intent.ACTION_VIEW, bankIdUri)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      startActivity(intent)
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
