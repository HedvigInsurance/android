package com.hedvig.app.authenticate.insurely

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.util.extensions.showErrorDialog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.lang.IllegalArgumentException

class InsurelyDialog : AuthenticateDialog() {

    private val reference: String by lazy {
        arguments?.getString(REFERENCE_KEY) ?: throw IllegalArgumentException("No reference found")
    }

    private val viewModel: InsurelyAuthViewModel by viewModel {
        parametersOf(reference)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                        viewState.autoStartToken?.let(::handleAutoStartToken)
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
                Pair(RESULT_KEY, success),
                Pair(RESULT_REFERENCE, if (success) reference else null)
            )
        )
        dismiss()
    }

    companion object {
        const val TAG = "LoginDialog"
        const val REQUEST_KEY = "2452"
        const val RESULT_KEY = "2454"
        const val RESULT_REFERENCE = "2455"
        private const val REFERENCE_KEY = "reference_key"

        fun newInstance(reference: String) = InsurelyDialog().apply {
            arguments = bundleOf(Pair(REFERENCE_KEY, reference))
        }
    }
}
