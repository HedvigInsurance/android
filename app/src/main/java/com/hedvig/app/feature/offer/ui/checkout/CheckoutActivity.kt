package com.hedvig.app.feature.offer.ui.checkout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityCheckoutBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.showErrorDialog
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.minus
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CheckoutActivity : BaseActivity(R.layout.activity_checkout) {
    private val parameter by lazy {
        intent.getParcelableExtra<CheckoutParameter>(PARAMETER)
            ?: throw IllegalArgumentException("No parameter found for ${this.javaClass.simpleName}")
    }
    private val viewModel: CheckoutViewModel by viewModel { parametersOf(parameter.quoteIds) }
    private val binding by viewBinding(ActivityCheckoutBinding::bind)
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }
            val link = getString(
                R.string.OFFER_FOOTER_GDPR_INFO,
                getString(R.string.CHECKOUT_BUTTON),
                getString(R.string.PRIVACY_POLICY_URL),
            )
            text.setMarkdownText(link)

            emailEditText.addTextChangedListener {
                viewModel.onEmailChanged(
                    input = it?.toString() ?: "",
                    hasError = emailInputContainer.error != null
                )
            }
            emailEditText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.validateInput()
                }
            }
            identityNumberEditText.addTextChangedListener {
                viewModel.onIdentityNumberChanged(
                    input = it?.toString() ?: "",
                    hasError = identityNumberInputContainer.error != null
                )
            }
            identityNumberEditText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.validateInput()
                }
            }

            signButton.setHapticClickListener {
                viewModel.validateInput()
                viewModel.onTrySign(
                    emailEditText.text.toString(),
                    identityNumberEditText.text.toString()
                )
            }
        }

        viewModel.inputViewState
            .flowWithLifecycle(lifecycle)
            .onEach(::setInputState)
            .launchIn(lifecycleScope)

        viewModel.titleViewState
            .flowWithLifecycle(lifecycle)
            .onEach(::setTitleState)
            .launchIn(lifecycleScope)

        viewModel.events
            .flowWithLifecycle(lifecycle)
            .onEach(::handleEvent)
            .launchIn(lifecycleScope)

        progressDialog = MaterialAlertDialogBuilder(this)
            .setView(R.layout.progress_dialog)
            .setCancelable(false)
            .create()
    }

    private fun setInputState(viewState: CheckoutViewModel.InputViewState) {
        binding.signButton.isEnabled = viewState.enableSign
        setContainerInputState(binding.emailInputContainer, viewState.emailInputState)
        setContainerInputState(binding.identityNumberInputContainer, viewState.identityInputState)
    }

    private fun setContainerInputState(
        textInputLayout: TextInputLayout,
        state: CheckoutViewModel.InputViewState.InputState
    ) {
        when (state) {
            is CheckoutViewModel.InputViewState.InputState.Invalid -> {
                textInputLayout.error = getString(state.stringRes ?: R.string.component_error)
            }
            CheckoutViewModel.InputViewState.InputState.NoInput,
            is CheckoutViewModel.InputViewState.InputState.Valid -> textInputLayout.error = null
        }
    }

    private fun setTitleState(titleState: CheckoutViewModel.TitleViewState) {
        when (titleState) {
            is CheckoutViewModel.TitleViewState.Loaded -> {
                TransitionManager.beginDelayedTransition(binding.root)

                binding.title.show()
                binding.cost.show()
                binding.originalCost.isVisible = !(titleState.netAmount - titleState.grossAmount).isZero
                binding.emailEditText.setText(titleState.email)
                binding.title.text = titleState.bundleName
                val netAmount = titleState.netAmount.format(this, titleState.market)
                val netString = getString(R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, netAmount)
                binding.cost.text = netString
                binding.originalCost.text = titleState.grossAmount.format(this, titleState.market)
                binding.originalCost.setStrikethrough(true)
            }
            CheckoutViewModel.TitleViewState.Loading -> {
            }
        }
    }

    private fun handleEvent(event: CheckoutViewModel.Event) {
        when (event) {
            is CheckoutViewModel.Event.Error -> {
                progressDialog.dismiss()
                showErrorDialog(event.message ?: getString(R.string.home_tab_error_body)) { }
            }
            CheckoutViewModel.Event.CheckoutSuccess -> startActivity(
                LoggedInActivity.newInstance(
                    context = this,
                    isFromOnboarding = true,
                    withoutHistory = true
                )
            )
            CheckoutViewModel.Event.Loading -> progressDialog.show()
        }
    }

    companion object {
        private const val PARAMETER = "PARAMETER"

        fun newInstance(context: Context, parameter: CheckoutParameter): Intent {
            return Intent(context, CheckoutActivity::class.java)
                .putExtra(PARAMETER, parameter)
        }
    }
}
