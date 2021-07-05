package com.hedvig.app.feature.offer.ui.checkout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityCheckoutBinding
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CheckoutActivity : BaseActivity(R.layout.activity_checkout) {

    private val viewModel: CheckoutViewModel by viewModel()

    private val binding by viewBinding(ActivityCheckoutBinding::bind)
    private val parameter by lazy {
        intent.getParcelableExtra<CheckoutParameter>(PARAMETER)
            ?: throw IllegalArgumentException("No parameter found for ${this.javaClass.simpleName}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }
            title.text = parameter.title
            subtitle.text = parameter.subtitle
            val link = getString(R.string.OFFER_FOOTER_GDPR_INFO, parameter.gdprUrl)
            text.setMarkdownText(link)

            emailEditText.addTextChangedListener {
                viewModel.onEmailChanged(it?.toString() ?: "")
            }
            identityNumberEditText.addTextChangedListener {
                viewModel.onIdentityNumberChanged(it?.toString() ?: "")
            }
        }

        lifecycleScope.launch {
            viewModel.viewState
                .flowWithLifecycle(lifecycle)
                .collect { viewState ->
                    when (viewState) {
                        CheckoutViewModel.ViewState.Loading -> {
                        }
                        is CheckoutViewModel.ViewState.Input -> setInputState(viewState)
                    }
                }
        }
    }

    private fun setInputState(viewState: CheckoutViewModel.ViewState.Input) {
        binding.signButton.isEnabled = viewState.allValid
        setContainerInputState(binding.emailInputContainer, viewState.emailInputState)
        setContainerInputState(binding.identityNumberInputContainer, viewState.identityInputState)
    }

    private fun setContainerInputState(
        textInputLayout: TextInputLayout,
        state: CheckoutViewModel.ViewState.InputState
    ) {
        when (state) {
            is CheckoutViewModel.ViewState.InputState.Invalid -> {
                textInputLayout.error = getString(state.stringRes ?: R.string.component_error)
            }
            CheckoutViewModel.ViewState.InputState.NoInput -> {
                textInputLayout.error = null
            }
            is CheckoutViewModel.ViewState.InputState.Valid -> {
                textInputLayout.error = null
            }
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
