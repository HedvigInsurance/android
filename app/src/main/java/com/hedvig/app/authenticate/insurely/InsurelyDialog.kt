package com.hedvig.app.authenticate.insurely

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.util.extensions.showErrorDialog
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class InsurelyDialog : AuthenticateDialog() {

    private val parameter: InsurelyDialogParameter
        get() = arguments?.getParcelable(PARAMETER)
            ?: throw IllegalArgumentException("Missing PARAMETER in ${this.javaClass.name}")

    private val viewModel: InsurelyAuthViewModel by viewModel {
        parametersOf(parameter.reference, parameter.providerId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.events
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach { event ->
                when (event) {
                    is InsurelyAuthViewModel.Event.Auth -> {
                        event.token?.let(::handleAutoStartToken) ?: redirect()
                    }
                }
            }
            .launchIn(lifecycleScope)

        viewModel.viewState
            .flowWithLifecycle(lifecycle)
            .onEach { viewState ->
                when (viewState) {
                    is InsurelyAuthViewModel.ViewState.Error -> {
                        binding.progress.hide()
                        context?.showErrorDialog(getString(R.string.OFFER_COMPARISION_ERROR)) {
                            setResult(success = false)
                        }
                    }
                    is InsurelyAuthViewModel.ViewState.Success -> {
                        binding.progress.hide()
                        bindNewStatus(viewState.authStatus)
                    }
                    InsurelyAuthViewModel.ViewState.Loading -> binding.progress.show()
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun bindNewStatus(state: DataCollectionResult.Success.CollectionStatus): Any? = when (state) {
        DataCollectionResult.Success.CollectionStatus.LOGIN -> {
            binding.authTitle.setText(R.string.BANK_ID_AUTH_TITLE_INITIATED)
        }
        DataCollectionResult.Success.CollectionStatus.COMPLETED,
        DataCollectionResult.Success.CollectionStatus.COLLECTING -> setResult(success = true)
        DataCollectionResult.Success.CollectionStatus.UNKNOWN,
        DataCollectionResult.Success.CollectionStatus.FAILED -> {
            binding.authTitle.setText(R.string.OFFER_COMPARISION_ERROR)
            dialog?.setCanceledOnTouchOutside(true)
        }
        DataCollectionResult.Success.CollectionStatus.NONE -> {
        }
    }

    private fun setResult(success: Boolean) {
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                RESULT_KEY to success,
                RESULT_REFERENCE to if (success) {
                    parameter.reference
                } else {
                    null
                },
            )
        )
        dismiss()
    }

    companion object {
        const val TAG = "LoginDialog"
        const val REQUEST_KEY = "2452"
        const val RESULT_KEY = "2454"
        const val RESULT_REFERENCE = "2455"
        private const val PARAMETER = "PARAMETER"

        fun newInstance(
            reference: String,
            providerId: String,
        ) = InsurelyDialog().apply {
            arguments = bundleOf(
                PARAMETER to InsurelyDialogParameter(
                    reference,
                    providerId,
                )
            )
        }
    }
}

@Parcelize
data class InsurelyDialogParameter(
    val reference: String,
    val providerId: String
) : Parcelable
